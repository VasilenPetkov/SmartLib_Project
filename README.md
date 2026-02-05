# 📚 SmartLib - Intelligent Library Management System

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green) ![MySQL](https://img.shields.io/badge/Database-MySQL-blue) ![Stripe](https://img.shields.io/badge/Payment-Stripe-purple)

**SmartLib** is a comprehensive full-stack web application designed to modernize traditional library operations. It acts as a bridge between readers and the library catalog, enabling seamless book management, secure subscription processing, and AI-assisted book recommendations.

---

## 🚀 Key Features

### 👤 User Experience
* **Secure Authentication:** Custom login and registration system.
* **Smart Dashboard:** Real-time tracking of active subscriptions and borrowed books.
* **Interactive Assistant:** A built-in "Smart Librarian" bot that suggests books based on keywords and user input.

### 📚 Book & Order Management
* **Dynamic Catalog:** Browse books with rich metadata (Author, Genre, Price).
* **Shopping Cart System:** Add books to a cart, manage quantities, and checkout.
* **Order History:** Complete logging of all transactions and borrowed items (`OrderLog`).
* **Delivery Options:** Choose between "Pickup" or "Courier" delivery.

### 💳 Payments & Subscriptions
* **Stripe Integration:** Fully functional payment gateway processing transactions in **EUR**.
* **Flexible Plans:** Support for Weekly, Monthly, and Yearly subscriptions.
* **Logic:** Automatic calculation of expiration dates based on payment success.

### 🔔 System Logic
* **Notifications:** Automated alerts for successful orders, subscriptions, and overdue books.
* **Validation:** Backend checks for book limits (max 6), duplicate entries, and active subscription status.

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **Backend** | Java, Spring Boot (Web, Data JPA) |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (Fetch API) |
| **Database** | MySQL, Hibernate ORM |
| **Payment** | Stripe API |
| **Build Tool** | Maven |

---

## 🏗️ Architecture

The application follows a strict **MVC (Model-View-Controller)** layered architecture to ensure separation of concerns:

1.  **Model:** JPA Entities representing the database (`Book`, `AppUser`, `OrderLog`).
2.  **Repository:** Interfaces for direct DB communication.
3.  **Service:** Contains the business logic (e.g., `LibraryService` calculates totals, `BotService` filters books).
4.  **Controller:** REST endpoints handling HTTP requests.
5.  **View:** Dynamic HTML/JS frontend consuming the REST API.

---

## 💾 Database Schema


## 🔌 API Reference (Examples)

### Bot
`GET /api/bot/ask?msg={query}`
* Returns a list of books matching the user's query keywords.

### Cart
`POST /api/library/cart/add/{bookId}`
* Adds a book to the user's cart or increments quantity.

`POST /api/library/cart/process-order`
* Finalizes the order, clears the cart, and saves the transaction log.

### Payments
`POST /api/library/create-payment-intent`
* Initializes a secure Stripe session for the frontend.

---

## ⚙️ Installation & Setup

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/yourusername/smartlib.git](https://github.com/yourusername/smartlib.git)
    ```

2.  **Configure Database**
    * Create a MySQL database named `smartlib_db`.
    * Update `application.properties` with your credentials:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/smartlib_db
    spring.datasource.username=root
    spring.datasource.password=yourpassword
    ```

3.  **Stripe Configuration**
    * Add your Stripe Secret Key in `application.properties`:
    ```properties
    stripe.api.key=sk_test_...
    ```

4.  **Run the App**
    ```bash
    mvn spring-boot:run
    ```
    Access the app at `http://localhost:8080`


## 🛡️ License

This project is created for educational purposes.
