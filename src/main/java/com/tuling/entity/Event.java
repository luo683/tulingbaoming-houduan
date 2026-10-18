package com.tuling.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private String eventTime;

    @Column(length = 200)
    private String location;

    @Column(length = 10)
    private String status;

    @Column(name = "cover_emoji", length = 10)
    private String coverEmoji;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "max_participants")
    private Integer maxParticipants = 0;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "allow_registration", nullable = false)
    private Boolean allowRegistration = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (maxParticipants == null) maxParticipants = 0;
        if (allowRegistration == null) allowRegistration = true;
    }

    public Event() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCoverEmoji() { return coverEmoji; }
    public void setCoverEmoji(String coverEmoji) { this.coverEmoji = coverEmoji; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Boolean getAllowRegistration() { return allowRegistration; }
    public void setAllowRegistration(Boolean allowRegistration) {
        this.allowRegistration = allowRegistration;
    }
}
