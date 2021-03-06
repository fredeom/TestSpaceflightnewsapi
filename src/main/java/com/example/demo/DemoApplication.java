package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;

import com.example.demo.Article;
import com.example.demo.ArticleRepository;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@RestController
public class DemoApplication implements CommandLineRunner {

  @Autowired
  private ArticleRepository articleRepository;
  
  @Autowired
  private Environment env;
  
  ExecutorService downloadService;
  
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @GetMapping("/allArticles")
  public String allArticles() {
    List<JSONArticle> jsonArticles = new ArrayList<JSONArticle>();
    for (Article a : articleRepository.findAll()) {
      JSONArticle jsonArticle = new JSONArticle();
      jsonArticle.setId(a.getId());
      jsonArticle.setTitle(a.getTitle());
      jsonArticle.setNewsSite(a.getNewsSite());
      jsonArticle.setPublishedAt(a.getPublishedDate());
      jsonArticles.add(jsonArticle);
    }
    return new Gson().toJson(jsonArticles);
  }

  @GetMapping("/article/{id}")
  public String article(@PathVariable String id) {
    for (Article article : articleRepository.findAll()) {
      if (article.getId().equals(id)) {
        return new String(article.getArticle());
      }
    };
    return "not found";
  }

  @GetMapping("/articlesByNewsSite/{newsSite}")
  public String articlesByNewsSite(@PathVariable String newsSite) {
    List<JSONArticle> jsonArticles = new ArrayList<JSONArticle>();
    for (Article a : articleRepository.findAll()) {
      if (a.getNewsSite().equals(newsSite)) {
        JSONArticle jsonArticle = new JSONArticle();
        jsonArticle.setId(a.getId());
        jsonArticle.setTitle(a.getTitle());
        jsonArticle.setNewsSite(a.getNewsSite());
        jsonArticle.setPublishedAt(a.getPublishedDate());
        jsonArticles.add(jsonArticle);
      }
    };
    return new Gson().toJson(jsonArticles);
  }

  private void downloadArticles(final List<JSONArticle> articles) {
    downloadService.submit(() -> {
      for (JSONArticle article : articles) {
        WebClient webClient = WebClient.create();
        String responseArticle = webClient.get()
            .uri(article.getUrl())
            .retrieve()
            .bodyToMono(String.class)
            .block();
        Article a = new Article();
        a.setId(article.getId());
        a.setArticle(responseArticle.getBytes());
        a.setTitle(article.getTitle());
        a.setNewsSite(article.getNewsSite());
        a.setPublishedDate(article.getPublishedAt());
        articleRepository.save(a);
      }
    });
  }
  
  @Override
  public void run(String... args) throws Exception {
    int totalThreads = Integer.parseInt(env.getProperty("threadpool.thread_count"));
    int articlesPerThread = Integer.parseInt(env.getProperty("threadpool.articles_per_thread"));
    int articlesTotal = Integer.parseInt(env.getProperty("articles.total"));
    int bufferLimit = Integer.parseInt(env.getProperty("jsonarticlebuffer.limit"));
    List<String> blacklist = Arrays.asList(env.getProperty("blacklistwords").split(" "));

    final Map<String, List<JSONArticle>> buffer = new HashMap<String, List<JSONArticle>>();
    
    ExecutorService service = Executors.newFixedThreadPool(totalThreads);
    downloadService = Executors.newFixedThreadPool(totalThreads);
    for (int i = 0; i < articlesTotal / articlesPerThread + 1; i++) {
      final int skipped_articles = i * articlesPerThread, articles_limit = articlesPerThread;
      service.submit(() -> {
        int sa = skipped_articles, al = articles_limit;
        if (sa >= articlesTotal) return;
        if (sa + al >= articlesTotal) al = articlesTotal - sa;
        System.out.println("skipped_articles = " + sa + " articles_limit = " + al);
        WebClient webClient = WebClient.create();
        String responseJson = webClient.get()
            .uri("https://test.spaceflightnewsapi.net/api/v2/articles?_limit=" + al + "&_start=" + sa)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        Gson g = new Gson();
        JSONArticle[] jsonArticles = g.fromJson(responseJson, JSONArticle[].class);
        for (JSONArticle jsonArticle : jsonArticles) {
          if (!jsonArticle.hasWords(blacklist)) {
            List<JSONArticle> articlesToProceed = new ArrayList<JSONArticle>();
            synchronized (jsonArticle) {
              if (!buffer.containsKey(jsonArticle.getNewsSite())) {
                buffer.put(jsonArticle.getNewsSite(), new ArrayList<JSONArticle>());
              }
              buffer.get(jsonArticle.getNewsSite()).add(jsonArticle);
              if (buffer.get(jsonArticle.getNewsSite()).size() >= bufferLimit) {
                articlesToProceed.addAll(buffer.get(jsonArticle.getNewsSite()));
                buffer.remove(jsonArticle.getNewsSite());
              }
            }
            if (articlesToProceed.size() > 0) {
              Collections.sort(articlesToProceed);
              downloadArticles(articlesToProceed);
            }
          }
        }
      });
    }
    service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    for (String newsSite : buffer.keySet()) {
      List<JSONArticle> articlesToProceed = buffer.get(newsSite);
      Collections.sort(articlesToProceed);
      downloadArticles(articlesToProceed);
    }
  }
}
