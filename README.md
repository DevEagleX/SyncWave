# SyncWave

**A full-stack real-time messaging platform** built with Spring Boot, WebSockets (STOMP), React, and PostgreSQL. SyncWave supports secure one-to-one and group conversations with live message delivery, online presence, typing indicators, read receipts, and unread counts.

---

## Features

- **Authentication** - Register and log in with JWT-based stateless authentication, secured by Spring Security
- **User profiles** - Edit your name, bio, and avatar; search for other users by name or email
- **Friend system** - Send, accept, and reject friend requests; view your friends list
- **Private chat** - One-to-one conversations, restricted to friends
- **Group chat** - Create named groups with multiple friends; sender names shown on messages
- **Real-time delivery** - Messages appear instantly via WebSockets (STOMP over SockJS), no refresh needed
- **Online presence** - See which friends are currently online
- **Typing indicators** - See when someone is typing in a conversation
- **Read receipts** - "Seen" indicator once the other person opens the conversation
- **Unread counts** - Per-conversation unread badges with last-message previews, updated live

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 3, Spring Security, Spring WebSocket, STOMP, Spring Data JPA, Bean Validation, Lombok, Maven |
| **Frontend** | React 18, Vite, React Router, Axios, SockJS, STOMP.js, Context API, Tailwind CSS, React Hook Form, React Hot Toast |
| **Database** | PostgreSQL |
| **Auth** | JWT (HMAC-SHA256), BCrypt password hashing |

---

## Architecture

```
┌──────────────────┐   REST (Axios)    ┌──────────────────────┐        ┌──────────────┐
│                  │ ────────────────► │                      │  JPA   │              │
│  React Frontend  │                   │  Spring Boot Backend │ ─────► │  PostgreSQL  │
│                  │ ◄──────────────── │                      │        │              │
└────────┬─────────┘   WebSocket       └──────────┬───────────┘        └──────────────┘
         │             (STOMP/SockJS)             │
         └────────────────────────────────────────┘
              persistent connection for live events
```

**Key design decisions**

- **Stateless JWT auth** - the same token authenticates HTTP requests (via a servlet filter) and the WebSocket session (via a STOMP `CONNECT` interceptor), so no server-side sessions are needed.
- **One generic conversation model** - `Conversation` + `ConversationParticipant` + `Message` power both 1:1 and group chats; a group is simply a conversation with more than two participants and a name.
- **Friendship as derived state** - friendships are `FriendRequest` rows with an `ACCEPTED` status, avoiding duplicate sources of truth.
- **Ephemeral vs. persisted events** - messages and read positions are stored in PostgreSQL; typing indicators and presence are broadcast over WebSocket without being persisted.
- **"Read up to" tracking** - read receipts use a single `lastReadAt` timestamp per participant rather than a row per message per reader.
- **Authorization at every layer** - the service layer verifies the acting user is a participant before saving or broadcasting any message, typing event, or read receipt.

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+

### 1. Create the database

```bash
psql -U postgres
```
```sql
CREATE DATABASE chatapp;
\q
```

Tables are created automatically by Hibernate on first startup.

### 2. Run the backend

```bash
cd chat-backend
```

Set your database credentials (replace with your own):

```bash
# Mac / Linux
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword

# Windows PowerShell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="yourpassword"
```

Start the server:

```bash
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

> **JWT secret:** a development default is included in `application.yml`. For anything beyond local development, set your own Base64-encoded secret:
> ```bash
> export JWT_SECRET=$(openssl rand -base64 32)
> ```

### 3. Run the frontend

```bash
cd chat-frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

### 4. Try it out

Real-time features need two users. Open two different browsers (or one normal and one incognito window), register two accounts, then:

1. Search for the other user under **Find people** and send a friend request
2. Accept it from the other account under **Friends**
3. Click **Message** to open a private chat and send messages in both directions
4. Create a group from **Messages -> + New group**

---

## Project Structure

```
syncwave/
├── chat-backend/
│   └── src/main/java/com/chatapp/
│       ├── config/        Security, CORS, WebSocket configuration
│       ├── controller/    REST controllers + WebSocket message handler
│       ├── service/       Business logic (auth, users, friends, conversations, presence)
│       ├── repository/    Spring Data JPA repositories
│       ├── entity/        JPA entities
│       ├── dto/           Request/response objects
│       ├── security/      JWT filter, WebSocket auth interceptor, presence listener
│       └── exception/     Custom exceptions + global handler
│
└── chat-frontend/
    └── src/
        ├── pages/         Login, Register, Profile, Search, Friends, Chat, New Group
        ├── components/    Navbar, cards, conversation list, chat window
        ├── context/       Auth and presence state
        ├── services/      REST API clients
        ├── websocket/     STOMP/SockJS client and pub/sub helpers
        └── utils/         Token storage
```

---

## API Overview

### REST

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Log in, receive JWT |
| GET | `/api/users/me` | Current user |
| PUT | `/api/users/me` | Update profile |
| GET | `/api/users/search?query=` | Search users |
| POST | `/api/friend-requests/{userId}` | Send friend request |
| PUT | `/api/friend-requests/{id}/accept` | Accept request |
| PUT | `/api/friend-requests/{id}/reject` | Reject request |
| GET | `/api/friend-requests/incoming` | Pending incoming requests |
| GET | `/api/friends` | Friends list |
| POST | `/api/conversations/private/{friendId}` | Get or create a private chat |
| POST | `/api/conversations/group` | Create a group |
| GET | `/api/conversations` | List conversations (with last message + unread count) |
| GET | `/api/conversations/{id}/messages` | Message history |
| PUT | `/api/conversations/{id}/read` | Mark conversation as read |

### WebSocket (STOMP)

Connect to `/ws` with the header `Authorization: Bearer <jwt>`.

| Direction | Destination | Purpose |
|---|---|---|
| Publish | `/app/chat.send` | Send a message |
| Publish | `/app/chat.typing` | Typing start/stop |
| Publish | `/app/chat.read` | Mark conversation read |
| Subscribe | `/topic/conversations/{id}` | New messages |
| Subscribe | `/topic/conversations/{id}/typing` | Typing events |
| Subscribe | `/topic/conversations/{id}/read` | Read receipts |
| Subscribe | `/topic/presence` | Online/offline events |

---

## Known Limitations & Roadmap

- Uses Spring's in-memory STOMP broker, which works for a single server instance. Scaling horizontally would require an external broker (e.g. RabbitMQ) for message relay.
- Presence is broadcast on a shared topic and filtered client-side, which is fine at this scale but not designed for very large user counts.
- Planned: message search, pinned/archived chats, media uploads, notifications, unit tests (JUnit/Mockito), and a CI pipeline with GitHub Actions.

---

## License

This project is open source and available under the [MIT License](LICENSE).
