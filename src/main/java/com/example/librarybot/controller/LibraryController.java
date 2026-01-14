package com.example.librarybot.controller;

import com.example.librarybot.dtapayment.PaymentRequest;
import com.example.librarybot.dtapayment.PaymentResponse;
import com.example.librarybot.model.AppUser;
import com.example.librarybot.model.Book;
import com.example.librarybot.model.CartItem;
import com.example.librarybot.model.Notification;
import com.example.librarybot.dtapayment.StripeService;
import com.example.librarybot.service.LibraryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final StripeService stripeService;

    public LibraryController(LibraryService libraryService, StripeService stripeService) {
        this.libraryService = libraryService;
        this.stripeService = stripeService;
    }

    @PostMapping("/login")
    public AppUser login(@RequestParam String username, @RequestParam String password) {
        return libraryService.loginOrRegister(username, password);
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam Long userId) {
        return libraryService.getUserStatus(userId);
    }

    @PostMapping("/borrow/{bookId}")
    public String borrow(@PathVariable Long bookId, @RequestParam Long userId) {
        return libraryService.borrowBook(bookId, userId);
    }

    @PostMapping("/return/{bookId}")
    public String returnBook(@PathVariable Long bookId) {
        return libraryService.returnBook(bookId);
    }

    @GetMapping("/my-books")
    public List<Book> getMyBooks(@RequestParam Long userId) {
        return libraryService.getBooksByUserId(userId);
    }

    @PostMapping("/cart/add/{bookId}")
    public String addToCart(@PathVariable Long bookId, @RequestParam Long userId) {
        return libraryService.addToCart(bookId, userId);
    }

    @GetMapping("/cart")
    public List<CartItem> getCart(@RequestParam Long userId) {
        return libraryService.getUserCart(userId);
    }

    @PostMapping("/cart/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId) {
        return libraryService.removeFromCart(cartItemId);
    }

    @PostMapping("/create-payment-intent")
    public PaymentResponse createPaymentIntent(@RequestBody PaymentRequest request) {
        String secret = stripeService.createPaymentIntent(request);
        return new PaymentResponse(secret);
    }

    @PostMapping("/cart/process-order")
    public String processOrder(@RequestParam Long userId,
                               @RequestParam String deliveryType,
                               @RequestParam(required = false) String address) { // Махнахме картата от тук
        return libraryService.checkout(userId, deliveryType, address);
    }

    @GetMapping("/notifications")
    public List<Notification> getNotifications(@RequestParam Long userId) {
        return libraryService.getUserNotifications(userId);
    }

    @PostMapping("/activate-subscription")
    public String activateSubscription(@RequestParam Long userId, @RequestParam String planType) {
        libraryService.activateSubscription(userId, planType);
        return "Абонаментът е активиран успешно!";
    }
}