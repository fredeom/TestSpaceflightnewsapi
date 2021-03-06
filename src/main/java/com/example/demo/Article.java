package com.example.demo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Article {
  @Id
  @GeneratedValue
  private Long uid;
  @Column(name="id")
  private String id;
  @Column(name="title")
  private String title;
  @Column(name="news_site")
  private String newsSite;
  @Column(name="published_date")
  private String publishedDate;
  @Lob
  @Column(length=100000,name="article")
  private byte[] article;

  public Article() {}

  public void setId(String id) { this.id = id; }
  public String getId() { return this.id; }
  
  public void setTitle(String title) { this.title = title; }
  public String getTitle() { return this.title; }

  public void setNewsSite(String newsSite) { this.newsSite = newsSite; }
  public String getNewsSite() { return this.newsSite; }

  public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
  public String getPublishedDate() { return this.publishedDate; }

  public void setArticle(byte[] article) { this.article = article; }
  public byte[] getArticle() { return this.article; }

  @Override
  public String toString() {
    return String.format("Article{id='%s', title='%s', newsSite='%s', publishedDate='%s', article='%s'}", id, title, newsSite, publishedDate, article);
  }
}