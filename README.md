# Campus Market 
## Site similar to OLX for College Students 


## Detailed API Reference

Base URL when running locally on Tomcat:

```text
http://localhost:8080/CampusMarket
```

All endpoints below are implemented as Jakarta servlets in `src/main/java/com/market/servlets/`.

### 1. Register User

**Endpoint**

```http
POST /RegisterServlet
```

**Purpose**

Creates a new user account in the `users` table and starts a login session immediately after successful registration.

**Request Parameters**

| Field | Type | Required | Description |
|------|------|----------|-------------|
| `full_name` | string | Yes | Full name of the student |
| `email` | string | Yes | Must end with `@silicon.ac.in` |
| `password` | string | Yes | Plain text password as currently implemented |
| `phone` | string | No | User phone number |
| `hostel_block` | string | No | Hostel or residence block |
| `year_of_study` | integer | No | Expected values like `1`, `2`, `3`, `4` |
| `branch` | string | No | Academic branch |

**Behavior**

- Rejects missing `full_name`, `email`, or `password`
- Rejects emails not ending in `@silicon.ac.in`
- Checks for duplicate email before insert
- Inserts the user into the `users` table
- Creates session values:
  - `userEmail`
  - `userName`
- Redirects to `home.html` after success

**Redirect/Error Outcomes**

| Case | Result |
|------|--------|
| Missing required fields | `signin.html?error=failed` |
| Invalid email domain | `signin.html?error=invalid_email` |
| Email already exists | `signin.html?error=email_exists` |
| Database error | `signin.html?error=database` |
| Success | `home.html` |

**Implementation Notes**

- Passwords are not hashed yet
- The code comments say the request fields match `signin.html`
- The current checked-in frontend file is `login.html`, not `signin.html`

---

### 2. Login User

**Endpoint**

```http
POST /LoginServlet
```

**Purpose**

Authenticates an existing user against the `users` table and creates a session.

**Request Parameters**

| Field | Type | Required | Description |
|------|------|----------|-------------|
| `email` | string | Yes | User email |
| `password` | string | Yes | User password |

**Behavior**

- Checks the `users` table for a matching `email` and `password`
- On success creates session values:
  - `userId`
  - `userName`
  - `userEmail`
- Redirects to `home.html`

**Redirect/Error Outcomes**

| Case | Result |
|------|--------|
| Invalid credentials | `signin.html?error=invalid` |
| Database error | `signin.html?error=database` |
| Success | `home.html` |

**Implementation Notes**

- Authentication is based on plain text password comparison
- The servlet queries `user_id`, but the checked-in SQL schema defines `sic` as the primary key and does not define `user_id`
- This means login may fail unless the real database differs from `database_schema.sql`

---

### 3. Post Item Listing

**Endpoint**

```http
POST /PostItemServlet
```

**Purpose**

Creates a marketplace listing in the `items` table and uploads the item image.

**Authentication**

Requires an active session containing `userEmail`.

**Request Type**

```http
multipart/form-data
```

**Request Parameters**

| Field | Type | Required | Description |
|------|------|----------|-------------|
| `title` | string | Yes | Item title |
| `category` | string | Yes | Category such as textbook, electronics, furniture, fashion |
| `condition` | string | Yes | Item condition |
| `description` | string | Yes | Listing description |
| `price` | number | Yes | Item price |
| `photo` | file | No in code | Uploaded image file |

**Behavior**

- Redirects unauthenticated users to `signin.html`
- Reads the multipart upload
- Saves the uploaded file under:

```text
uploads/items/<timestamp>_<original-file-name>
```

- Inserts a new row into `items`
- Uses the logged-in user's `userEmail` as `seller_email`
- Redirects to `home.html?success=posted` after success

**Redirect/Error Outcomes**

| Case | Result |
|------|--------|
| Not logged in | `signin.html` |
| Insert failed | `post-item.html?error=failed` |
| Database error | `post-item.html?error=database` |
| Success | `home.html?success=posted` |

**Upload Limits**

- Maximum file size: `10 MB`

**Implementation Notes**

- Files are stored relative to the deployed servlet context path
- The code allows `image_path = null`, but the checked-in SQL schema marks `image_path` as `NOT NULL`
- If no image is uploaded, the insert may fail depending on the actual database schema

---

### 4. Browse Available Items

**Endpoint**

```http
GET /BrowseServlet
```

**Purpose**

Returns a JSON array of all available listings, with optional category and search filtering.

**Query Parameters**

| Parameter | Type | Required | Description |
|----------|------|----------|-------------|
| `category` | string | No | Exact match category filter |
| `search` | string | No | Text search across title and description |
| `format` | string | No | Read by code but not used in logic |

**Behavior**

- Selects from `items` joined with `users`
- Only returns rows where `items.status = 'available'`
- If `category` is provided, filters by exact category
- If `search` is provided, filters using `ILIKE` on:
  - `title`
  - `description`
- Orders results by `posted_at DESC`
- Always responds with JSON

**Response Shape**

```json
[
  {
    "itemId": 1,
    "title": "Sample Item",
    "description": "Item description",
    "price": 120.0,
    "category": "electronics",
    "conditionType": "first-hand",
    "imagePath": "uploads/items/123_file.jpg",
    "sellerName": "Student Name",
    "sellerEmail": "student@silicon.ac.in",
    "hostelBlock": "Hostel A"
  }
]
```

**Error Behavior**

| Case | Result |
|------|--------|
| Database error | Returns `[]` |

**Implementation Notes**

- This is the main catalog API
- `format` is currently unused
- The README comments say this powers `home.html` and `item-details.html`

---

### 5. Get Single Item Detail

**Endpoint**

```http
GET /ItemDetailServlet?id=<item_id>
```

**Purpose**

Returns one item with seller information as JSON.

**Query Parameters**

| Parameter | Type | Required | Description |
|----------|------|----------|-------------|
| `id` | integer | Yes | Item ID |

**Behavior**

- Validates that `id` exists
- Validates that `id` is numeric
- Queries the `items` table joined with `users`
- Returns exactly one JSON object if found

**Response Shape**

```json
{
  "itemId": 1,
  "title": "Sample Item",
  "description": "Item description",
  "price": 120.0,
  "category": "electronics",
  "conditionType": "first-hand",
  "imagePath": "uploads/items/123_file.jpg",
  "status": "available",
  "sellerName": "Student Name",
  "sellerEmail": "student@silicon.ac.in",
  "hostelBlock": "Hostel A"
}
```

**JSON Error Responses**

| Case | Response |
|------|----------|
| Missing `id` | `{"error":"Missing item id"}` |
| Invalid `id` | `{"error":"Invalid item id"}` |
| Item not found | `{"error":"Item not found"}` |
| Database error | `{"error":"Database error"}` |

**Implementation Notes**

- The servlet also queries seller phone as `seller_phone`
- That phone value is not included in the final JSON response

---

### 6. Get Logged-in User Profile

**Endpoint**

```http
GET /ProfileServlet
```

**Purpose**

Returns the currently logged-in user's profile data and all items posted by that user.

**Authentication**

Requires an active session containing `userEmail`.

**Behavior**

- If there is no session, responds with `401` and redirects to `signin.html`
- Loads user profile fields from the `users` table
- Loads all listings from the `items` table for the same `seller_email`
- Orders listings by `posted_at DESC`
- Returns a single JSON object containing the profile and items array

**Response Shape**

```json
{
  "fullName": "Student Name",
  "email": "student@silicon.ac.in",
  "phone": "9999999999",
  "hostelBlock": "Hostel A",
  "yearOfStudy": 2,
  "branch": "CSE",
  "itemCount": 2,
  "items": [
    {
      "itemId": 1,
      "title": "Sample Item",
      "category": "electronics",
      "conditionType": "first-hand",
      "price": 120.0,
      "imagePath": "uploads/items/123_file.jpg",
      "status": "available"
    }
  ]
}
```

**JSON/Error Outcomes**

| Case | Result |
|------|--------|
| Not logged in | HTTP `401` then redirect to `signin.html` |
| User not found | `{"error":"User not found"}` |
| Database error | `{"error":"Database error"}` |

**Implementation Notes**

- This endpoint depends entirely on the session email
- It returns both profile details and the user's posted items in one response

---

### 7. Logout User

**Endpoint**

```http
GET /LogoutServlet
```

**Purpose**

Logs out the current user by invalidating the session.

**Behavior**

- Reads the current session if it exists
- Invalidates the session
- Redirects to `signin.html`

**Redirect Outcomes**

| Case | Result |
|------|--------|
| Session exists or not | `signin.html` |

---

## Data Model Used by the APIs

### `users` table

Important fields referenced by the servlets:

- `full_name`
- `email`
- `password`
- `phone`
- `hostel_block`
- `year_of_study`
- `branch`

### `items` table

Important fields referenced by the servlets:

- `item_id`
- `seller_email`
- `title`
- `description`
- `price`
- `category`
- `condition_type`
- `image_path`
- `status`
- `posted_at`

---

## Important Project Notes

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