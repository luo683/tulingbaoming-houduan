package com.tuling.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "tech_tags", length = 300)
    private String techTags;

    @Column(name = "github_link", length = 500)
    private String githubLink;

    @Column(name = "demo_link", length = 500)
    private String demoLink;

    @Column(name = "cover_emoji", length = 10)
    private String coverEmoji;

    @Column(name = "member_count", nullable = false)
    private int memberCount = 1;

    @Column(name = "star_count", nullable = false)
    private int starCount = 0;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (memberCount < 1) memberCount = 1;
        if (starCount < 0) starCount = 0;
    }

    public Project() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechTags() { return techTags; }
    public void setTechTags(String techTags) { this.techTags = techTags; }

    public String getGithubLink() { return githubLink; }
    public void setGithubLink(String githubLink) { this.githubLink = githubLink; }

    public String getDemoLink() { return demoLink; }
    public void setDemoLink(String demoLink) { this.demoLink = demoLink; }

    public String getCoverEmoji() { return coverEmoji; }
    public void setCoverEmoji(String coverEmoji) { this.coverEmoji = coverEmoji; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getStarCount() { return starCount; }
    public void setStarCount(int starCount) { this.starCount = starCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
