package com.example.librarybot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime sentAt;

    public Notification(AppUser user, String subject, String message) {
        this.user = user;
        this.subject = subject;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }
}