
let currentUserId = null;
let selectedPlan   = null;
let isRegisterMode = false;
let cartTotalAmount = 0;

const stripe = Stripe('pk_test_51SpXBFRZ0kCB37XxCqN9NPNwIm2gE9ZjC4z90bOuzk31E76psWga9WG48O4VcsTBXqYyHgPaChbl60BaaMGhkZTM00AXTppRHR');
let elements;
let cardElement;

function toggleAuthMode(mode) {
    const loginTab      = document.getElementById("tab-login");
    const regTab        = document.getElementById("tab-register");
    const confirmGroup  = document.getElementById("confirm-password-group");
    const btn           = document.getElementById("auth-btn");

    document.getElementById("username-input").value        = "";
    document.getElementById("password-input").value        = "";
    document.getElementById("confirm-password-input").value = "";

    if (mode === 'register') {
        isRegisterMode = true;
        loginTab.classList.remove("active");
        regTab.classList.add("active");
        confirmGroup.style.display = "block";
        btn.innerText = "Регистрирай се";
    } else {
        isRegisterMode = false;
        regTab.classList.remove("active");
        loginTab.classList.add("active");
        confirmGroup.style.display = "none";
        btn.innerText = "Влез";
    }
}

async function handleAuth() {
    const username = document.getElementById("username-input").value.trim();
    const password = document.getElementById("password-input").value.trim();

    if (!username || !password) return alert("Попълнете всички полета!");

    if (isRegisterMode) {
        const confirmPass = document.getElementById("confirm-password-input").value.trim();

        if (password !== confirmPass) {
            alert("❌ Паролите не съвпадат! Опитайте отново.");
            return;
        }

        try {
            const res = await fetch('/api/library/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const text = await res.text();

            if (res.ok) {
                alert("✅ Успешна регистрация! Сега можете да влезете.");
                toggleAuthMode('login');
            } else {
                alert("Грешка: " + text);
            }
        } catch (e) {
            alert("Грешка при връзка със сървъра!");
        }

    } else {
        try {
            const res = await fetch(`/api/library/login?username=${username}&password=${password}`, { method: 'POST' });
            if (!res.ok) return alert("Грешна парола или потребител!");

            const user = await res.json();
            currentUserId = user.id;

            document.getElementById("user-display-name").innerText  = user.username;
            document.getElementById("login-screen").style.display   = 'none';
            document.getElementById("app-container").style.display  = 'flex';

            if (user.username === 'admin') {
                document.getElementById("admin-btn").style.display = "block";
            }

            updateStatus();
            loadMyBooks();
            updateCartCount();
            checkNotifications();
            loadCatalog();

        } catch (e) {
            alert("Грешка при логин!");
        }
    }
}

function logout() {
    location.reload();
}

async function updateCartCount() {
    const res       = await fetch(`/api/library/cart?userId=${currentUserId}`);
    const cartItems = await res.json();
    document.getElementById("cart-count").innerText = cartItems.length;
}

async function openCart() {
    const res       = await fetch(`/api/library/cart?userId=${currentUserId}`);
    const cartItems = await res.json();
    const container = document.getElementById("cart-items-container");

    container.innerHTML = "";
    cartTotalAmount     = 0;
    document.getElementById("cart-total-display").innerHTML = "";

    if (cartItems.length === 0) {
        container.innerHTML = "<p style='text-align:center; color:#999;'>Списъкът е празен.</p>";
        document.getElementById("cart-footer").style.display = "none";
    } else {
        cartItems.forEach(item => {
            const price     = item.book.price ? item.book.price : 20.00;
            const itemTotal = price * item.quantity;
            cartTotalAmount += itemTotal;

            container.innerHTML += `
                <div class="cart-item">
                    <div class="cart-item-title" style="flex: 2;">
                        ${item.book.title}
                        <br><small style="color:#666">${item.book.author}</small>
                        <br><small style="color:#28a745; font-weight:bold;">ед. цена: ${price.toFixed(2)} EUR</small>
                    </div>
                    <div style="flex: 1; display: flex; align-items: center; gap: 5px;">
                        <input type="number" min="1" value="${item.quantity}"
                               style="width: 50px; padding: 5px; text-align: center; border: 1px solid #ccc; border-radius: 4px;"
                               onchange="changeQuantity(${item.id}, this.value)">
                    </div>
                    <div style="flex: 1; font-weight: bold; text-align: right; margin-right: 10px;">
                        ${itemTotal.toFixed(2)} EUR
                    </div>
                    <div class="remove-btn" onclick="removeFromCart(${item.id})" style="margin-left: 10px;">❌</div>
                </div>
            `;
        });

        document.getElementById("cart-footer").style.display = "block";
        document.getElementById("cart-total-display").innerHTML =
            `ОБЩО: <span style="font-size:1.4em; color:#d32f2f;">${cartTotalAmount.toFixed(2)} EUR</span>`;
    }

    document.getElementById("cart-modal").style.display = "block";
}

function closeCart() {
    document.getElementById("cart-modal").style.display = "none";
}

async function addToCart(bookId) {
    const res  = await fetch(`/api/library/cart/add/${bookId}?userId=${currentUserId}`, { method: 'POST' });
    const text = await res.text();
    alert(text);
    updateCartCount();
}

async function removeFromCart(cartItemId) {
    await fetch(`/api/library/cart/remove/${cartItemId}`, { method: 'POST' });
    openCart();
    updateCartCount();
}

async function changeQuantity(itemId, newQty) {
    if (newQty < 1) return;
    try {
        await fetch(`/api/library/cart/update/${itemId}?quantity=${newQty}`, { method: 'POST' });
        openCart();
    } catch (e) {
        console.error("Грешка при промяна на количеството");
    }
}

function payForCart() {
    if (cartTotalAmount <= 0) return alert("Количката е празна!");

    const delivery = document.getElementById("delivery-type").value;
    const address  = document.getElementById("delivery-address").value.trim();
    if (delivery === "COURIER" && !address) return alert("Моля въведете адрес!");

    closeCart();

    selectedPlan = 'CART_PURCHASE';
    document.getElementById("sub-plan-name").innerText = `Покупка на книги - ${cartTotalAmount.toFixed(2)} EUR`;
    document.getElementById("sub-modal").style.display = "block";

    if (!elements) {
        elements    = stripe.elements();
        cardElement = elements.create('card');
        cardElement.mount('#card-element');
    }
}

function toggleAddress() {
    const type = document.getElementById("delivery-type").value;
    document.getElementById("address-group").style.display = (type === "COURIER") ? "block" : "none";
}

function payFee(planType) {
    selectedPlan = planType;
    const plans  = { WEEKLY: ["1 Седмица", "5EUR"], MONTHLY: ["1 Месец", "15EUR"], YEARLY: ["1 Година", "100EUR"] };
    const [name, price] = plans[planType];

    document.getElementById("sub-plan-name").innerText = `${name} - ${price}`;
    document.getElementById("sub-modal").style.display = "block";

    if (!elements) {
        elements    = stripe.elements();
        cardElement = elements.create('card');
        cardElement.mount('#card-element');
    }
}

async function confirmSubscription() {
    const amountMap = { WEEKLY: 500, MONTHLY: 1500, YEARLY: 10000 };
    const descMap   = { WEEKLY: "Абонамент: 1 Седмица", MONTHLY: "Абонамент: 1 Месец", YEARLY: "Абонамент: 1 Година" };

    const amount      = selectedPlan === 'CART_PURCHASE' ? Math.round(cartTotalAmount * 100) : amountMap[selectedPlan];
    const description = selectedPlan === 'CART_PURCHASE' ? "Покупка на книги" : descMap[selectedPlan];

    const confirmBtn      = document.querySelector("#sub-modal button");
    confirmBtn.disabled   = true;
    confirmBtn.innerText  = "Обработка...";

    try {
        const res = await fetch('/api/library/create-payment-intent', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount, currency: 'eur', userEmail: 'user@example.com', description })
        });

        if (!res.ok) throw new Error("Сървърна грешка");

        const data         = await res.json();
        const clientSecret = data.clientSecret;

        const result = await stripe.confirmCardPayment(clientSecret, {
            payment_method: {
                card: cardElement,
                billing_details: { name: document.getElementById("user-display-name").innerText }
            }
        });

        if (result.error) {
            document.getElementById('card-errors').innerText = result.error.message;
            alert("❌ Плащането неуспешно: " + result.error.message);

        } else if (result.paymentIntent.status === 'succeeded') {

            if (selectedPlan === 'CART_PURCHASE') {
                const delivery = document.getElementById("delivery-type").value;
                const address  = document.getElementById("delivery-address").value;

                const orderRes = await fetch(
                    `/api/library/cart/process-order?userId=${currentUserId}&deliveryType=${delivery}&address=${encodeURIComponent(address)}`,
                    { method: 'POST' }
                );

                if (orderRes.ok) {
                    alert("✅ Плащането е успешно! Поръчката ви е приета.");
                    document.getElementById("sub-modal").style.display = "none";
                    cardElement.clear();
                    updateCartCount();
                    loadMyBooks();
                    checkNotifications();
                } else {
                    alert("⚠️ Плащането мина, но имаше проблем със записването на поръчката.");
                }

            } else {
                const activationRes = await fetch(
                    `/api/library/activate-subscription?userId=${currentUserId}&planType=${selectedPlan}`,
                    { method: 'POST' }
                );

                if (activationRes.ok) {
                    alert("✅ Плащането е успешно и абонаментът е активиран!");
                    document.getElementById("sub-modal").style.display = "none";
                    updateStatus();
                    cardElement.clear();
                } else {
                    alert("⚠️ Парите са взети, но възникна грешка при активирането.");
                }
            }
        }

    } catch (err) {
        console.error(err);
        alert("Възникна техническа грешка!");
    } finally {
        confirmBtn.disabled  = false;
        confirmBtn.innerText = "Плати сумата";
    }
}

async function updateStatus() {
    const res  = await fetch(`/api/library/status?userId=${currentUserId}`);
    const data = await res.json();
    const box  = document.getElementById("sub-status-box");
    const payOptions = document.getElementById("payment-options");

    if (data.active) {
        box.style.background   = "#e6fffa";
        box.style.color        = "#047857";
        box.style.borderColor  = "#a7f3d0";
        box.innerHTML = `<div>✅ АКТИВЕН</div><div>Остават: <strong>${data.daysLeft} дни</strong></div>`;
        payOptions.style.display = "none";
    } else {
        box.style.background   = "#fff5f5";
        box.style.color        = "#c62828";
        box.style.borderColor  = "#ffcdd2";
        box.innerHTML = `<div>⛔ НЕАКТИВЕН</div>`;
        payOptions.style.display = "block";
    }
}

async function loadMyBooks() {
    const res       = await fetch(`/api/library/my-books?userId=${currentUserId}`);
    const books     = await res.json();
    const container = document.getElementById("my-books-list");
    container.innerHTML = "";

    if (books.length === 0) {
        container.innerHTML = '<div style="color: #999; text-align: center; margin-top: 30px;">Нямате заети книги</div>';
        return;
    }

    books.forEach(book => {
        let dateHtml    = "";
        let statusColor = "#fff";
        let borderColor = "#eee";

        if (book.dueDate) {
            const diffDays = Math.ceil((new Date(book.dueDate) - new Date().setHours(0,0,0,0)) / (1000 * 60 * 60 * 24));
            if (diffDays < 0) {
                dateHtml    = `<span style="color: #c62828; font-weight: bold;">⚠️ ЗАКЪСНЯЛА с ${Math.abs(diffDays)} дни!</span>`;
                statusColor = "#fff5f5";
                borderColor = "#ffcdd2";
            } else {
                dateHtml = `<span style="color: #059669;">Остават ${diffDays} дни</span> <small style="color: #999;">(${book.dueDate})</small>`;
            }
        }

        container.innerHTML += `
            <div class="my-book-item" style="background: ${statusColor}; border-color: ${borderColor};">
                <div style="font-weight: bold; color: #333;">${book.title}</div>
                <div style="font-size: 0.9em; margin-bottom: 8px;">${dateHtml}</div>
                <button class="return-btn" onclick="returnBook(${book.id})">Върни книгата</button>
            </div>`;
    });
}

async function returnBook(id) {
    if (!confirm("Връщате ли книгата?")) return;
    await fetch(`/api/library/return/${id}`, { method: 'POST' });
    loadMyBooks();
}

async function sendMessage() {
    const inputField = document.getElementById("user-input");
    const message    = inputField.value.trim();
    if (!message) return;

    addMessage(message, "user-msg");
    inputField.value = "";

    const typingId = "typing-" + Date.now();
    addMessage(
        `<div class="typing"><div class="dot"></div><div class="dot"></div><div class="dot"></div></div>`,
        "bot-msg", true, typingId
    );

    try {
        const [response] = await Promise.all([
            fetch(`/api/bot/ask?msg=${encodeURIComponent(message)}`),
            new Promise(resolve => setTimeout(resolve, 1500))
        ]);

        const books       = await response.json();
        const typingBubble = document.getElementById(typingId);
        if (typingBubble) typingBubble.remove();

        if (books.length === 0) {
            addMessage("Не открих нищо подходящо.", "bot-msg");
        } else {
            addMessage("Ето какво намерих:", "bot-msg");
            books.forEach(book => {
                const btnState  = book.borrowed ? 'disabled' : '';
                const btnText   = book.borrowed ? 'Вече заета' : '➕ Добави в списъка';
                const bookHtml  = `
                    <div style="font-weight: bold; color: #333;">${book.title}</div>
                    <div style="color: #666; font-size: 0.9em; margin-bottom: 8px;">${book.author} | ${book.genre}</div>
                    <button class="action-btn" ${btnState} onclick="addToCart(${book.id})">${btnText}</button>
                `;
                addMessage(bookHtml, "bot-msg", true);
            });
        }
    } catch (error) {
        document.getElementById(typingId)?.remove();
        addMessage("Грешка при свързване.", "bot-msg");
    }
}

function addMessage(text, cls, isHtml = false, elementId = null) {
    const div       = document.createElement("div");
    div.className   = `message ${cls}`;
    if (elementId) div.id = elementId;
    if (isHtml) div.innerHTML = text;
    else        div.textContent = text;

    const chatBox       = document.getElementById("chat-box");
    chatBox.appendChild(div);
    chatBox.scrollTop   = chatBox.scrollHeight;
}

function handleEnter(e) {
    if (e.key === "Enter") sendMessage();
}

async function checkNotifications() {
    const res   = await fetch(`/api/library/notifications?userId=${currentUserId}`);
    const msgs  = await res.json();
    const badge = document.getElementById("notif-count");

    if (msgs.length > 0) {
        badge.innerText          = msgs.length;
        badge.style.display      = "flex";
    } else {
        badge.style.display      = "none";
    }
}

async function openNotifications() {
    const res  = await fetch(`/api/library/notifications?userId=${currentUserId}`);
    const msgs = await res.json();
    const list = document.getElementById("notif-list");
    list.innerHTML = "";

    if (msgs.length === 0) {
        list.innerHTML = "<p style='text-align:center'>Нямате нови съобщения.</p>";
    }

    msgs.forEach(msg => {
        list.innerHTML += `
            <div class="msg-item" onclick="this.querySelector('.msg-body').style.display = this.querySelector('.msg-body').style.display==='block'?'none':'block'">
                <div class="msg-subject">${msg.subject}</div>
                <div class="msg-date">${new Date(msg.sentAt).toLocaleString()}</div>
                <div class="msg-body">${msg.message}</div>
            </div>
        `;
    });

    document.getElementById("notif-modal").style.display = "block";
}

async function submitNewBook() {
    const title       = document.getElementById("new-book-title").value;
    const author      = document.getElementById("new-book-author").value;
    const category    = document.getElementById("new-book-category").value;
    const price       = document.getElementById("new-book-price").value;
    const keywordsRaw = document.getElementById("new-book-keywords").value;

    if (!title || !author || !category) return alert("Попълнете полетата!");

    const keywords = keywordsRaw.split(',').map(s => s.trim());
    const btn      = document.querySelector("#admin-modal button");
    btn.innerText  = "Записване...";

    try {
        const res = await fetch('/api/library/add-book', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, author, category, keywords, price: parseFloat(price) })
        });

        const text = await res.text();
        alert("✅ " + text);

        document.getElementById("new-book-title").value    = "";
        document.getElementById("new-book-keywords").value = "";
        document.getElementById("admin-modal").style.display = "none";

    } catch (e) {
        alert("Грешка при запис!");
        console.error(e);
    } finally {
        btn.innerText = "💾 Запази книгата";
    }
}

async function loadCatalog() {
    document.getElementById("catalog-section").style.display = "block";
    const grid = document.getElementById("book-grid");
    grid.innerHTML = "";

    const showcaseBooks = [
        { id: 1,  title: "Harry Potter",          author: "J.K. Rowling" },
        { id: 2,  title: "The Lord of the Rings",  author: "J.R.R. Tolkien" },
        { id: 3,  title: "Game of Thrones",        author: "George R.R. Martin" },
        { id: 4,  title: "The Witcher",            author: "Andrzej Sapkowski" },
        { id: 5,  title: "The Hobbit",             author: "J.R.R. Tolkien" },
        { id: 6,  title: "Dune",                   author: "Frank Herbert" },
        { id: 7,  title: "Hitchhiker's Guide",     author: "Douglas Adams" },
        { id: 8,  title: "1984",                   author: "George Orwell" },
        { id: 9,  title: "Fahrenheit 451",         author: "Ray Bradbury" },
        { id: 10, title: "IT",                     author: "Stephen King" },
        { id: 11, title: "The Shining",            author: "Stephen King" },
        { id: 12, title: "Gone Girl",              author: "Gillian Flynn" }
    ];

    showcaseBooks.forEach(book => {
        grid.innerHTML += `
            <div class="catalog-item" onclick="addToCart(${book.id})" title="${book.title} - ${book.author}">
                <img src="/images/covers/${book.id}.jpg" class="catalog-img" alt="${book.title}">
                <div class="catalog-title">${book.title}</div>
            </div>
        `;
    });
}

window.onclick = function(event) {
    if (event.target === document.getElementById("cart-modal")) closeCart();
};
