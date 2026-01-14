package com.example.librarybot.service;

import com.example.librarybot.model.AppUser;
import com.example.librarybot.model.Book;
import com.example.librarybot.model.CartItem;
import com.example.librarybot.model.Notification;
import com.example.librarybot.repository.BookRepository;
import com.example.librarybot.repository.CartItemRepository;
import com.example.librarybot.repository.NotificationRepository;
import com.example.librarybot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final NotificationRepository notificationRepository;

    public LibraryService(BookRepository bookRepository,
                          UserRepository userRepository,
                          CartItemRepository cartItemRepository,
                          NotificationRepository notificationRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.notificationRepository = notificationRepository;
    }

    public AppUser loginOrRegister(String username, String password) {
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (!user.getPassword().equals(password)) throw new RuntimeException("Грешна парола!");
            return user;
        } else {
            return userRepository.save(new AppUser(username, password));
        }
    }

    public String payFee(Long userId, String planType, String cardNumber, String expiryDate, String cvv) {

        if (cardNumber == null || cardNumber.length() < 16 ||
            (!cardNumber.startsWith("4") && !cardNumber.startsWith("5"))) {
            return "ГРЕШКА: Невалиден номер на карта (Visa/MasterCard)!";
        }

        if (cvv == null || cvv.length() != 3 || !cvv.matches("\\d+")) {
            return "ГРЕШКА: Невалиден CVV код!";
        }

        if (expiryDate == null || expiryDate.isEmpty()) {
            return "ГРЕШКА: Моля въведете валидност на картата!";
        }

        AppUser user = userRepository.findById(userId).orElseThrow();
        int days = 0;
        double amount = 0.0;

        if ("WEEKLY".equals(planType)) {
            days = 7;
            amount = 5.00;
        } else if ("MONTHLY".equals(planType)) {
            days = 30;
            amount = 15.00;
        } else if ("YEARLY".equals(planType)) {
            days = 365;
            amount = 100.00;
        } else return "Невалиден план";

        if (user.getSubscriptionEndDate() == null ||
            user.getSubscriptionEndDate().isBefore(java.time.LocalDate.now())) {
            user.setSubscriptionEndDate(java.time.LocalDate.now().plusDays(days));
        } else {
            user.setSubscriptionEndDate(user.getSubscriptionEndDate().plusDays(days));
        }
        userRepository.save(user);

        String bill = "Успешно плащане на абонамент: " + planType +
            "\nСума: " + amount + " лв." +
            "\nКарта: **** **** **** " + cardNumber.substring(cardNumber.length() - 4);

        notificationRepository.save(new Notification(user, "Фактура за абонамент", bill));

        return "УСПЕХ: Абонаментът е активиран до " + user.getSubscriptionEndDate();
    }

    private boolean isSubscriptionActive(AppUser user) {
        return user.getSubscriptionEndDate() != null && user.getSubscriptionEndDate().isAfter(LocalDate.now());
    }

    public Map<String, Object> getUserStatus(Long userId) {
        AppUser user = userRepository.findById(userId).orElseThrow();
        Map<String, Object> status = new HashMap<>();

        boolean isActive = isSubscriptionActive(user);
        status.put("active", isActive);

        if (isActive) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), user.getSubscriptionEndDate());
            status.put("daysLeft", daysLeft);
            status.put("date", user.getSubscriptionEndDate().toString());
        } else {
            status.put("daysLeft", 0);
        }
        return status;
    }

    private static final int MAX_ALLOWED_BOOKS = 6;

    public String borrowBook(Long bookId, Long userId) {
        AppUser user = userRepository.findById(userId).orElseThrow();

        if (!isSubscriptionActive(user)) {
            return "ГРЕШКА: Абонаментът ви е изтекъл!";
        }

        List<Book> currentBooks = getBooksByUserId(userId);
        if (currentBooks.size() >= MAX_ALLOWED_BOOKS) {
            return "ГРЕШКА: Достигнахте лимита от " + MAX_ALLOWED_BOOKS + " книги! Върнете някоя, за да вземете нова.";
        }

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getBorrower() != null) return "Тази книга вече е заета.";

            book.setBorrower(user);
            book.setDueDate(LocalDate.now().plusWeeks(2));
            bookRepository.save(book);
            return "Успешно заехте: " + book.getTitle();
        }
        return "Няма такава книга.";
    }

    public String returnBook(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setBorrower(null);
            book.setDueDate(null);
            bookRepository.save(book);
            return "Върнахте: " + book.getTitle();
        }
        return "Грешка.";
    }

    public List<Book> getBooksByUserId(Long userId) {
        return bookRepository.findAll().stream()
            .filter(b -> b.getBorrower() != null && b.getBorrower().getId().equals(userId))
            .collect(Collectors.toList());
    }

    public String addToCart(Long bookId, Long userId) {
        AppUser user = userRepository.findById(userId).orElseThrow();
        Optional<Book> bookOpt = bookRepository.findById(bookId);

        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();

            if (book.getBorrower() != null) return "Книгата вече е заета от друг!";

            List<CartItem> cart = cartItemRepository.findByUserId(userId);
            boolean alreadyInCart = cart.stream().anyMatch(item -> item.getBook().getId().equals(bookId));
            if (alreadyInCart) return "Тази книга вече е в списъка ви!";

            cartItemRepository.save(new CartItem(user, book));
            return "Добавено в списъка: " + book.getTitle();
        }
        return "Книгата не е намерена.";
    }

    public List<CartItem> getUserCart(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public String removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
        return "Премахнато от списъка.";
    }

    @jakarta.transaction.Transactional

    public String checkout(Long userId, String deliveryType, String address) {
        AppUser user = userRepository.findById(userId).orElseThrow();

        if (!isSubscriptionActive(user)) return "ГРЕШКА: Нямате активен абонамент! Моля платете такса.";

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) return "Списъкът е празен!";

        StringBuilder booksTitle = new StringBuilder();
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            if (book.getBorrower() != null) return "ГРЕШКА: Книгата " + book.getTitle() + " вече е заета!";

            book.setBorrower(user);
            book.setDueDate(java.time.LocalDate.now().plusWeeks(2));
            bookRepository.save(book);
            booksTitle.append(book.getTitle()).append(", ");
        }
        cartItemRepository.deleteByUserId(userId);

        String deliveryInfo = "PICKUP".equals(deliveryType) ? "Взимане от библиотека" : "Куриер до адрес: " + address;

        String emailBody = "Здравейте, " + user.getUsername() + "!\n\n" +
            "Поръчката е приета.\n" +
            "Книги: " + booksTitle.toString() + "\n" +
            "Доставка: " + deliveryInfo + "\n" +
            "ВАЖНО: Услугата е безплатна, тъй като имате активен абонамент.";

        notificationRepository.save(new Notification(user, "Потвърждение за поръчка", emailBody));

        return "УСПЕХ";
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public void activateSubscription(Long userId, String planType) {
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Потребителят не е намерен"));

        LocalDate endDate = LocalDate.now();

        if ("WEEKLY".equals(planType)) {
            endDate = endDate.plusWeeks(1);
        } else if ("MONTHLY".equals(planType)) {
            endDate = endDate.plusMonths(1);
        } else if ("YEARLY".equals(planType)) {
            endDate = endDate.plusYears(1);
        }

        user.setSubscriptionEnd(endDate);

        userRepository.save(user);
    }
}