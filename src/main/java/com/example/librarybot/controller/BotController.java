package com.example.librarybot.controller;

import com.example.librarybot.model.Book;
import com.example.librarybot.service.BotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("/ask")
    public List<Book> askBot(@RequestParam String msg) {
        return botService.getRecommendation(msg);
    }
}