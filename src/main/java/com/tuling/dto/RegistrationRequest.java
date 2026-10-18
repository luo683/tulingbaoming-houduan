package com.tuling.dto;

/**
 * 报名请求DTO
 */
public class RegistrationRequest {
    private String realName;
    private String phone;
    private String email;
    private String notes;

    public RegistrationRequest() {}

    // Getters and Setters
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
