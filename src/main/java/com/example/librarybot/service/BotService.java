package com.example.librarybot.service;

import com.example.librarybot.model.Book;
import com.example.librarybot.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BotService {

    private final BookRepository bookRepository;

    public BotService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getRecommendation(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();

        return bookRepository.findAll().stream()
            .filter(book -> isBookMatching(book, lowerCaseMessage))
            .collect(Collectors.toList());
    }

    private boolean isBookMatching(Book book, String message) {
        String msg = message.toLowerCase();

        if (book.getTitle().toLowerCase().contains(msg)) return true;
        if (book.getAuthor().toLowerCase().contains(msg)) return true;

        String genre = book.getGenre().toLowerCase();

        if (genre.contains(msg)) return true;

        if ((msg.contains("фантастика") || msg.contains("научна")) &&
            (genre.contains("sci-fi") || genre.contains("scifi") || genre.contains("science"))) return true;
        if ((msg.contains("фентъзи") || msg.contains("магия")) && genre.contains("fantasy")) return true;

        if ((msg.contains("ужас") || msg.contains("страш")) && genre.contains("horror")) return true;
        if ((msg.contains("трилър") || msg.contains("напрежение")) && genre.contains("thriller")) return true;
        if ((msg.contains("крими") || msg.contains("убийств") || msg.contains("детектив")) &&
            (genre.contains("crime") || genre.contains("mystery") || genre.contains("noir"))) return true;

        if ((msg.contains("романти") || msg.contains("любов")) && genre.contains("romance")) return true;
        if (msg.contains("драма") && genre.contains("drama")) return true;

        if (msg.contains("класика") && genre.contains("classic")) return true;
        if ((msg.contains("епос") || msg.contains("митология")) && genre.contains("epic")) return true;

        if (msg.contains("приключен") && genre.contains("adventure")) return true;
        if ((msg.contains("истор") && !msg.contains("наука")) && genre.contains("historical"))
            return true;

        if ((msg.contains("хумор") || msg.contains("комед") || msg.contains("смеш")) &&
            (genre.contains("comedy") || genre.contains("satire"))) return true;
        if ((msg.contains("комикс") || msg.contains("графич")) && genre.contains("graphic")) return true;
        if (msg.contains("реализъм") && genre.contains("magic")) return true; // за Magic Realism

        if ((msg.contains("биограф") || msg.contains("живот")) &&
            (genre.contains("biography") || genre.contains("memoir"))) return true;

        if ((msg.contains("програм") || msg.contains("код") || msg.contains("компют") || msg.contains("it")) &&
            (genre.contains("it") || genre.contains("programming") || genre.contains("code"))) return true;
        if (msg.contains("наука") && genre.contains("science")) return true;
        if ((msg.contains("природа") || msg.contains("еколог") || msg.contains("животни")) && genre.contains("nature"))
            return true;
        if ((msg.contains("психолог") || msg.contains("мислене")) && genre.contains("psychology")) return true;
        if ((msg.contains("философ") || msg.contains("смисъл")) && genre.contains("philosophy")) return true;

        if ((msg.contains("готв") || msg.contains("кулин") || msg.contains("храна") || msg.contains("рецепт")) &&
            genre.contains("cooking")) return true;
        if ((msg.contains("бизнес") || msg.contains("пари") || msg.contains("финанс")) &&
            genre.contains("finance")) return true;
        if ((msg.contains("развитие") || msg.contains("успех") || msg.contains("мотивац")) &&
            genre.contains("self")) return true;
        if ((msg.contains("спорт") || msg.contains("тренир")) && genre.contains("sport")) return true;

        if ((msg.contains("млади") || msg.contains("тинейдж") || msg.contains("юнош")) &&
            (genre.contains("young"))) return true;

        if (book.getKeywords() != null) {
            for (String keyword : book.getKeywords()) {
                if (msg.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}