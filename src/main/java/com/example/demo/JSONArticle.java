package com.example.demo;

import java.util.List;

public class JSONArticle implements Comparable<JSONArticle> {
  private String id;
  private String title;
  private String newsSite;
  private String publishedAt;
  private String url;
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getNewsSite() { return newsSite; }
  public void setNewsSite(String newsSite) { this.newsSite = newsSite; }
  public String getPublishedAt() { return publishedAt; }
  public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }
  @Override
  public String toString() {
    return "JSONArticle [id=" + id + ", title=" + title + ", newsSite=" + newsSite + ", publishedAt=" + publishedAt + ", url=" + url + "]";
  }
  public boolean hasWords(List<String> words) {
    for (String word : words) {
      if (title.indexOf(word) >= 0) {
        return true;
      }
    }
    return false;
  }
  @Override
  public int compareTo(JSONArticle ja) {
    if (publishedAt == null || ja.publishedAt == null) return 0;
    return publishedAt.compareTo(ja.publishedAt);
  }
}
