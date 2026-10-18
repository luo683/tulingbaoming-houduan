package com.tuling.dto;

/**
 * 报名统计DTO
 */
public class RegistrationStatsDTO {
    private Long eventId;
    private long totalRegistered;
    private long totalAttended;
    private long totalCancelled;
    private Integer maxParticipants;
    private boolean isFull;

    public RegistrationStatsDTO() {}

    public RegistrationStatsDTO(Long eventId, long totalRegistered, long totalAttended,
                               long totalCancelled, Integer maxParticipants) {
        this.eventId = eventId;
        this.totalRegistered = totalRegistered;
        this.totalAttended = totalAttended;
        this.totalCancelled = totalCancelled;
        this.maxParticipants = maxParticipants;
        this.isFull = maxParticipants != null && maxParticipants > 0
                      && totalRegistered >= maxParticipants;
    }

    // Getters and Setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public long getTotalRegistered() { return totalRegistered; }
    public void setTotalRegistered(long totalRegistered) {
        this.totalRegistered = totalRegistered;
    }

    public long getTotalAttended() { return totalAttended; }
    public void setTotalAttended(long totalAttended) {
        this.totalAttended = totalAttended;
    }

    public long getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(long totalCancelled) {
        this.totalCancelled = totalCancelled;
    }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isFull() { return isFull; }
    public void setFull(boolean full) { isFull = full; }
}
