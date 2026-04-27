# Campus Market

A college student OLX-like marketplace built using Java Servlets, Tomcat, HTML, CSS, and JavaScript.

Campus Market lets students post items, browse listings, view item details, and manage their profile in a simple campus marketplace experience.

---

## 🚀 Project Description

Campus Market is a lightweight web application designed for college students to buy and sell second-hand goods within their campus community. The platform uses Java Servlet technology on Tomcat and stores data in PostgreSQL. Front-end pages are built using HTML, CSS, and JavaScript.

This project demonstrates:
- servlet-based form handling
- file upload support for product images
- session-based user login
- JSON-backed browse and item detail endpoints

---

## ⭐ Features

- User registration and login
- Add new marketplace items with image upload
- Browse available campus items
- View item details and seller information
- User profile view with posted listings
- Server-side session timeout handling

---

## 🧰 Tech Stack

- Java Servlets
- Jakarta Servlet API
- Apache Tomcat 10.x
- PostgreSQL
- HTML, CSS, JavaScript
- JDBC PostgreSQL Driver

---

## 📁 Folder Structure

```
CampusMarket/
├── database_schema.sql
├── src/main/java/
│   └── com/market/
│       ├── db/
│       │   └── DBConnection.java
│       └── servlets/
│           ├── BrowseServlet.java
│           ├── ItemDetailServlet.java
│           ├── LoginServlet.java
│           ├── PostItemServlet.java
│           ├── ProfileServlet.java
│           └── RegisterServlet.java
├── src/main/webapp/
│   ├── home.html
│   ├── item-details.html
│   ├── post-item.html
│   ├── profile.html
│   ├── signin.html
│   ├── uploads/items/
│   └── WEB-INF/
│       ├── lib/
│       │   └── postgresql-42.7.1.jar
│       └── web.xml
```

---

## 📌 API Documentation

### RegisterServlet
- URL: `/RegisterServlet`
- Method: `POST`
- Description: Handles new user registration from `signin.html`.

### LoginServlet
- URL: `/LoginServlet`
- Method: `POST`
- Description: Authenticates users and starts a session.

### PostItemServlet
- URL: `/PostItemServlet`
- Method: `POST`
- Description: Receives item post submissions, including file uploads for images.

### BrowseServlet
- URL: `/BrowseServlet`
- Method: `GET`
- Description: Returns marketplace items for the home/browse page.

### ItemDetailServlet
- URL: `/ItemDetailServlet`
- Method: `GET`
- Description: Returns details for a single item.

### ProfileServlet
- URL: `/ProfileServlet`
- Method: `GET`
- Description: Returns the current user profile and posted listings.

---

## ⚙️ Setup Instructions

### 1. Install prerequisites

- Java JDK 11+ or compatible
- Apache Tomcat 10.x
- PostgreSQL
- PostgreSQL JDBC driver (`postgresql-42.7.1.jar`)

### 2. Create the PostgreSQL database

Run the SQL schema in `database_schema.sql`.

Example using `psql`:

```sql
CREATE DATABASE campus_market;
\c campus_market
\i database_schema.sql
```

### 3. Configure database connection

Edit `src/main/java/com/market/db/DBConnection.java` and update:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/campus_market";
private static final String USER = "postgres";
private static final String PASS = "12345";
```

Update values to match your PostgreSQL credentials.

### 4. Add the JDBC driver

Download `postgresql-42.7.1.jar` from the PostgreSQL JDBC website and place it into:

```
src/main/webapp/WEB-INF/lib/
```

### 5. Prepare uploads directory

Ensure the upload folder exists and is writable:

```
src/main/webapp/uploads/items/
```

### 6. Deploy to Tomcat

- Import the project into Eclipse or your IDE as a Dynamic Web Project.
- Configure Apache Tomcat 10.x as the project runtime.
- Add the JDBC driver JAR to the build path.
- Run the project on the Tomcat server.

Or build a WAR file and deploy it to Tomcat's `webapps/` directory.

### 7. Access the app

Open the app at:

```
http://localhost:8080/CampusMarket/
```

---

## 🔧 Troubleshooting

- `HTTP 404`: Confirm the project is deployed and the URL is correct.
- `HTTP 500`: Check Tomcat logs, servlet compilation, and database connectivity.
- `ClassNotFoundException`: Ensure `postgresql-42.7.1.jar` is in `WEB-INF/lib/`.
- `jakarta.servlet` errors: Use Tomcat 10 / Jakarta Servlet API.
- File uploads fail: Check that `uploads/items/` exists and is writable.
- Database connection errors: Verify PostgreSQL is running and credentials are correct.

---

## 🌱 Future Improvements

<<<<<<< HEAD
- Add search and category filters
- Implement secure password hashing
- Add user dashboard with edit/delete listings
- Add item image previews and multiple uploads
- Add email notifications and messaging between users
- Replace static HTML with a modern frontend framework
=======
These are not separate APIs, but they affect how the current APIs behave:

1. The frontend files currently checked into `src/main/webapp/` are:
   - `login.html`
   - `homepage.html`
   - `createproduct.html`
   - `productdetail.html`
   - `profilepage.html`

2. The backend configuration expects:
   - `signin.html`
   - `home.html`
   - `post-item.html`
   - `item-details.html`
   - `profile.html`

3. `LoginServlet` and `RegisterServlet` use `user_id`, but `database_schema.sql` defines `sic` instead of `user_id`.

4. The current frontend forms are not fully wired to these servlet endpoints yet, so some backend APIs are implemented but not connected to the checked-in HTML pages.

---
### Contribution
---Updated Readme
>>>>>>> 367fab9ca4e1f296ecb1d9a5b3255a2f9b676bc1
