package com.tuling.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "Resource")
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "file_size", length = 20)
    private String fileSize;

    @Column(length = 500)
    private String link;

    @Column(name = "extract_code", length = 20)
    private String extractCode;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(length = 10)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "published";
        if (likeCount < 0) likeCount = 0;
    }

    public Resource() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getExtractCode() { return extractCode; }
    public void setExtractCode(String extractCode) { this.extractCode = extractCode; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
