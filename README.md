# 📚 BookBuddy: Online Book Recommendation System

A social book-discovery web app built with **Java Spring Boot**. Users connect with friends, rate books, and recommend books to each other. Admins manage users and books and monitor rating trends.

---

## 1. Why I Built This

Finding a good book to read is harder than it sounds. Existing options have clear gaps:

| Problem in existing systems | How BookBuddy addresses it |
|---|---|
| Recommendations come from generic algorithms, purchase history or bestseller lists | Recommendations come from **friends you trust**, with a personal comment |
| Platforms are built for individuals, with no social connection | **Friend requests** create a trusted network |
| Users can rate and review but cannot recommend directly to a friend | A **Recommend** button sends a book plus comment to a friend |
| Books are hard to find quickly | **Search** by title, author or ISBN |
| No visual view of book popularity | A **bar chart** of average ratings per book |
| No central control | An **Admin panel** for users, books and ratings |

**Goal:** encourage reading habits by combining social networking with personalised book suggestions.

---

## 2. Features

**User module**
- Register and log in, with passwords stored as BCrypt hashes
- Edit profile (email)
- Search books by name, author or ISBN (live filtering)
- Rate books from 1 to 5 stars (re-rating updates the old rating)
- Send, receive and accept friend requests
- Recommend a book to a friend with a comment (friends only)
- View received recommendations
- View the average-rating bar chart

**Admin module**
- View and delete users (admin accounts are protected)
- Add and delete books (deleting a book also removes its ratings)
- Monitor rating trends in the chart

---

## 3. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation, Security) |
| Database | H2 (default, zero setup) or MySQL (profile `mysql`) |
| Frontend | HTML, CSS, JavaScript (single page, calls the REST API) |
| Build tool | Maven |

> **Note:** the original design used JSP. Executable Spring Boot jars do not support JSP well, so the frontend is a static HTML/CSS/JS page that talks to a REST API.

---

## 4. How It Works

```
Browser (index.html + JS)
        │  HTTP + JSON  (Authorization: Basic ...)
        ▼
Spring Security ── checks login and role (USER / ADMIN)
        ▼
ApiController (REST endpoints, validation, business rules)
        ▼
Spring Data JPA repositories
        ▼
Database (H2 or MySQL)
```

**Typical flow**
1. A user registers. The password is hashed with BCrypt and saved.
2. The user logs in. The browser keeps the credentials for the session and sends them with every request.
3. The user sends a friend request. The other user accepts it, and the status changes from `PENDING` to `ACCEPTED`.
4. The user searches for a book and rates it with the stars.
5. The user recommends a book to a friend. The server checks that they are accepted friends before saving it.
6. The friend sees it under **Recommendations**. The **Ratings** tab shows the averages as bars.

**Business rules enforced by the server**
- Usernames are unique, and passwords must be at least 6 characters.
- Ratings must be 1 to 5, with one rating per user per book.
- You cannot friend yourself or send a duplicate request.
- Only the receiver can accept a request.
- Recommendations are allowed only between accepted friends.
- `/api/admin/**` is accessible only to the ADMIN role.

---

## 5. Data Model

| Table | Main fields |
|---|---|
| `app_user` | id, username (unique), password (hashed), email, role |
| `book` | isbn (primary key), title, author, publisher, publishYear |
| `rating` | id, username, isbn, stars (unique per username + isbn) |
| `friendship` | id, requester, receiver, status (PENDING / ACCEPTED) |
| `recommendation` | id, fromUser, toUser, isbn, note, createdAt |

---

## 6. REST API

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create account |
| GET / PUT | `/api/me` | User | View / update profile |
| GET | `/api/books`, `/api/books/{isbn}` | User | List books, book details with average rating |
| POST | `/api/books/{isbn}/rate` | User | Rate a book |
| GET | `/api/ratings/summary` | User | Data for the bar chart |
| POST | `/api/friends/request/{username}` | User | Send friend request |
| GET | `/api/friends/requests` | User | Incoming requests |
| POST | `/api/friends/accept/{id}` | User | Accept request |
| GET | `/api/friends` | User | List friends |
| POST / GET | `/api/recommendations` | User | Send / view recommendations |
| GET, DELETE | `/api/admin/users`, `/api/admin/users/{id}` | Admin | Manage users |
| POST, DELETE | `/api/admin/books`, `/api/admin/books/{isbn}` | Admin | Manage books |

---

## 7. Project Structure

```
bookrec/
├── pom.xml
└── src/main/
    ├── java/com/bookrec/
    │   ├── BookRecApplication.java   # entry point + sample data seeding
    │   ├── SecurityConfig.java       # BCrypt, roles, HTTP Basic
    │   ├── ApiController.java        # all REST endpoints
    │   ├── Repos.java                # Spring Data repositories
    │   └── AppUser, Book, Rating, Friendship, Recommendation (entities)
    └── resources/
        ├── application.properties          # H2 (default)
        ├── application-mysql.properties    # MySQL profile
        └── static/index.html               # frontend UI
```

---

## 8. Run the Project

**Requirements:** JDK 17 or newer, Maven (or IntelliJ IDEA, which bundles Maven)

**Command line**
```bash
mvn spring-boot:run
```
**IntelliJ:** open the `bookrec` folder, wait for Maven to finish, then run `BookRecApplication`.

Open **http://localhost:8080**

**Use MySQL instead:** edit the password in `application-mysql.properties`, make sure MySQL is running, then run
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

**Default admin:** `admin` / `admin123` (change this before any real use). Register normal users from the login page.

---

## 9. Limitations and Future Scope

- Authentication uses HTTP Basic, which is suitable for local use only. For deployment, use HTTPS with sessions or JWT.
- No pagination, password change, friend removal or book cover images yet.
- ISBN format is checked, but not its checksum.
- Possible upgrades:
  - Automatic recommendations (collaborative filtering)
  - Reviews and comments
  - Reading lists
  - Server-side search for large catalogues
  - Unit and integration tests

---

## 10. Author

Developed by **Ronnie** as an academic and portfolio project to demonstrate Spring Boot REST design, JPA data modelling, role-based security and a clean single-page UI.
