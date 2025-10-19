# Hotel Management API

A Spring Boot application for managing hotels and rooms.

## Table of Contents

- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Endpoints](#api-endpoints)

---

## Getting Started

1. Make sure you have the following installed:

   - Java 21
   - Maven
   - Docker & Docker Compose

2. Clone the repository:

```bash
git clone https://github.com/mehmet-f-dogan/hotel-booking-platform
cd hotel-booking-platform
```

## Running the Application

Simply build jars and use Docker Compose:

```bash
mvn clean package -DskipTests
docker-compose up --build
```

The API will be available at:

```bash
http://localhost:8080
```

## Running Tests

Run all unit and integration tests with Maven:

```bash
mvn test
```

This will execute:

- Unit tests for services
- Integration tests for controllers

## API Endpoints

### Auth

| Method | Endpoint            | Description              | Request Body / Headers                         |
| ------ | ------------------- | ------------------------ | ---------------------------------------------- |
| POST   | `/api/auth/login`   | Login and get JWT tokens | `LoginRequest` JSON                            |
| POST   | `/api/auth/refresh` | Refresh access token     | `Authorization: Bearer <refresh_token>` header |
| POST   | `/api/auth/create`  | Create a new user        | `CreateUserRequest` JSON                       |

LoginRequest Example:

```json
{
  "username": "user1",
  "password": "password123"
}
```

LoginResponse Example:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER"
}
```

CreateUserRequest Example:

```json
{
  "username": "newuser",
  "password": "password123",
  "role": "USER"
}
```

### Hotels

POST, PUT, DELETE methods need admin rights. Send JWT Bearer token.

| Method | Endpoint           | Description        | Request Body        |
| ------ | ------------------ | ------------------ | ------------------- |
| POST   | `/api/hotels`      | Create a new hotel | `HotelRequest` JSON |
| GET    | `/api/hotels/{id}` | Get hotel by ID    | -                   |
| GET    | `/api/hotels`      | Get all hotels     | -                   |
| PUT    | `/api/hotels/{id}` | Update hotel by ID | `HotelRequest` JSON |
| DELETE | `/api/hotels/{id}` | Delete hotel by ID | -                   |

HotelRequest Example:

```json
{
  "name": "Grand Hotel",
  "address": "123 Main St",
  "starRating": 5
}
```

### Rooms

POST, PUT, DELETE methods need admin rights. Send JWT Bearer token.

| Method | Endpoint          | Description       | Request Body       |
| ------ | ----------------- | ----------------- | ------------------ |
| POST   | `/api/rooms`      | Create a new room | `RoomRequest` JSON |
| GET    | `/api/rooms/{id}` | Get room by ID    | -                  |
| GET    | `/api/rooms`      | Get all rooms     | -                  |
| PUT    | `/api/rooms/{id}` | Update room by ID | `RoomRequest` JSON |
| DELETE | `/api/rooms/{id}` | Delete room by ID | -                  |

RoomRequest Example:

```json
{
  "hotelId": 1,
  "roomNumber": "101",
  "capacity": 2,
  "pricePerNight": 120.0
}
```

### Reservations

## Reservation API

| Method | Endpoint                 | Description              | Request Body                    |
| ------ | ------------------------ | ------------------------ | ------------------------------- |
| POST   | `/api/reservations`      | Create a new reservation | `CreateReservationRequest` JSON |
| GET    | `/api/reservations/{id}` | Get reservation by ID    | -                               |
| PUT    | `/api/reservations/{id}` | Update reservation by ID | `UpdateReservationRequest` JSON |
| DELETE | `/api/reservations/{id}` | Delete reservation by ID | -                               |

CreateReservationRequest Example:

```json
{
  "hotelId": 1,
  "roomId": "101",
  "guestName": "John Doe",
  "checkIn": "2025-11-01",
  "checkOut": "2025-11-05"
}
```

UpdateReservationRequest Example:

```json
{
  "guestName": "Jane Doe",
  "checkIn": "2025-11-02",
  "checkOut": "2025-11-06"
}
```
