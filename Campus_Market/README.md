# Campus Second-Hand Marketplace
IWT Lab Project — Backend Setup Guide

---

## Project Structure

```
CampusMarket/
├── src/main/java/
│   └── com/market/
│       ├── db/
│       │   └── DBConnection.java          ← PostgreSQL connection helper
│       └── servlets/
│           ├── RegisterServlet.java       ← User registration
│           ├── LoginServlet.java          ← User login
│           ├── PostItemServlet.java       ← Post item + file upload
│           └── BrowseServlet.java         ← Browse/search items
├── src/main/webapp/
│   ├── signin.html                        ← (clone from GitHub)
│   ├── home.html                          ← (clone from GitHub)
│   ├── post-item.html                     ← (clone from GitHub)
│   ├── item-details.html                  ← (clone from GitHub)
│   ├── uploads/items/                     ← User uploaded photos
│   └── WEB-INF/
│       ├── lib/
│       │   └── postgresql-42.7.1.jar      ← Download separately
│       └── web.xml
└── database_schema.sql                    ← Run this in PostgreSQL
```

---

## Setup Steps

### 1. PostgreSQL Database
```sql
-- Open psql or pgAdmin and run:
CREATE DATABASE campus_market;
\c campus_market
-- Then paste contents of database_schema.sql
```

### 2. Update DB Password
Open `src/main/java/com/market/db/DBConnection.java` and change:
```java
private static final String PASS = "your_password"; // ← set your postgres password
```

### 3. Download JDBC Driver
Download `postgresql-42.7.1.jar` from https://jdbc.postgresql.org/download/
and place it in `src/main/webapp/WEB-INF/lib/`

### 4. Frontend Files
Clone from: https://github.com/Halloloid/IWT/tree/main/frontend
Rename and copy to `src/main/webapp/`:
- createproduct.html  → post-item.html
- homepage.html       → home.html
- productdetail.html  → item-details.html
- profilepage.html    → profile.html
- Add signin.html

### 5. Eclipse Setup
1. File → New → Dynamic Web Project
2. Name: `CampusMarket`
3. Target runtime: Apache Tomcat v10.1
4. Dynamic web module version: 5.0
5. Add External JAR: postgresql-42.7.1.jar via Build Path

### 6. Deploy
- Right-click project → Run As → Run on Server
- OR export as WAR → copy to `C:\apache-tomcat-10.1.54\webapps\`
- Access: http://localhost:8080/CampusMarket/

---

## Servlet URL Mappings

| Servlet           | URL Pattern         | Method |
|-------------------|---------------------|--------|
| RegisterServlet   | /RegisterServlet    | POST   |
| LoginServlet      | /LoginServlet       | POST   |
| PostItemServlet   | /PostItemServlet    | POST   |
| BrowseServlet     | /BrowseServlet      | GET    |

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| HTTP 404 | Check project deployed to Tomcat |
| HTTP 500 | Check servlet compilation, PostgreSQL running |
| ClassNotFoundException | Verify postgresql-42.7.1.jar in WEB-INF/lib/ |
| javax.servlet error | Change all imports to jakarta.servlet |
| File upload fails | Check uploads/items/ folder exists with write permissions |
| Connection error | Check PostgreSQL service is running |
