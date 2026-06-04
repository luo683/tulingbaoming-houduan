package com.tuling.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String title;

    @Column(length = 200)
    private String dept;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String quote;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String research;

    @Column(length = 300)
    private String tags;

    @Column(name = "avatar_gradient", length = 100)
    private String avatarGradient;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Teacher() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }

    public String getResearch() { return research; }
    public void setResearch(String research) { this.research = research; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getAvatarGradient() { return avatarGradient; }
    public void setAvatarGradient(String avatarGradient) { this.avatarGradient = avatarGradient; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
