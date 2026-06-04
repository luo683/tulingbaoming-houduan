package com.tuling.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String role;

    @Column(length = 100)
    private String department;

    @Column(length = 50)
    private String grade;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 300)
    private String tags;

    @Column(name = "avatar_gradient", length = 100)
    private String avatarGradient;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_lead", nullable = false)
    private Boolean isLead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isLead == null) isLead = false;
    }

    public Member() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getAvatarGradient() { return avatarGradient; }
    public void setAvatarGradient(String avatarGradient) { this.avatarGradient = avatarGradient; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsLead() { return isLead; }
    public void setIsLead(Boolean isLead) { this.isLead = isLead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
