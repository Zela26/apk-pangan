package com.pangan.kotatomohon;

public class User {
    private String userId;
    private String nama;
    private String email;

    private String role;

    public User() {
        // Diperlukan konstruktor kosong untuk Firebase Realtime Database.
    }

    public User(String userId, String nama, String email, String role) {
        this.userId = userId;
        this.nama = nama;
        this.email = email;

        this.role = role;
    }
    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
    public String getUserId() {
        return userId;
    }
}

