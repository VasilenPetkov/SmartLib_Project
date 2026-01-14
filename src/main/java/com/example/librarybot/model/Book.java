package com.example.librarybot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser borrower;

    @ElementCollection
    private List<String> keywords;

    public Book(String title, String author, Category category, List<String> keywords) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.keywords = keywords;
        this.dueDate = null;
    }

    public String getGenre() {
        return category != null ? category.getName() : "Общи";
    }

    public boolean isBorrowed() {
        return borrower != null;
    }
}