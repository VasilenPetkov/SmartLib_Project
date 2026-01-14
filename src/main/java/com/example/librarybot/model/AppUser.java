package com.example.librarybot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private LocalDate subscriptionEndDate;

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.subscriptionEndDate = null;
    }

    public void setSubscriptionEnd(LocalDate endDate) {
        this.subscriptionEndDate = endDate;
    }
}