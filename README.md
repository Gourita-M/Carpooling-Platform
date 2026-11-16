# Carpooling Platform

A Spring Boot REST API for managing a multi-stop carpooling system. Drivers can publish trips with intermediate stops, passengers can search and book seats for a route segment, and the platform enforces seat availability, pricing, and trip lifecycle rules.

## Overview

This project implements the core backend of a carpooling platform where:

- Drivers manage their profile and vehicles
- Passengers manage account details and booking history
- Trips may include multiple stopping points between departure and destination
- Seat bookings are calculated using route price differences
- Remaining availability and trip status are updated automatically

The business logic follows the requirement specification in the project context, including route validation, price computation, booking constraints, and cancellation rules.

## Key Features

- Driver, passenger, and vehicle management
- Multi-stop route publishing with ordered stops
- Search trips by departure city, destination city, and date
- Booking seats for a segment between two points on a trip
- Automatic price calculation and remaining seat validation
- Trip completion and cancellation handling
- Booking cancellation within business constraints
- PostgreSQL-backed persistence with JPA

## Tech Stack

- Java 17
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Lombok
- MapStruct
- Maven

## Project Structure

```text
src/
├── main/
│   ├── java/org/carpooling/platform/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── CarpoolingPlatformApplication.java
│   │   └── ServletInitializer.java
│   └── resources/
│       ├── application.properties
│       ├── static/
│       └── templates/
└── test/
    └── java/org/carpooling/platform/
```

## Prerequisites

Before running the project, make sure you have:

- JDK 17 or newer
- Maven 3.9+
- PostgreSQL installed and running
- A database named `carpooling` created locally

## Configuration

Set your PostgreSQL connection details in `src/main/resources/application.properties`:

```properties
spring.application.name=CarpoolingPlatform

spring.datasource.url=jdbc:postgresql://localhost:5432/carpooling
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

If needed, update the database URL, username, and password to match your environment.

## Running the Application

From the project root:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Or with Maven directly:

```bash
mvn clean install
mvn spring-boot:run
```

The application runs as a Spring Boot application on the default local port configured by Spring Boot, typically `http://localhost:8080`.

## API Overview

The API is organized around the following resource groups:

### Driver endpoints

- `POST /api/drivers`
- `GET /api/drivers/{id}`
- `GET /api/drivers`
- `GET /api/drivers/search`
- `PUT /api/drivers/{id}`
- `DELETE /api/drivers/{id}`

### Passenger endpoints

- `POST /api/passengers`
- `GET /api/passengers/{id}`
- `GET /api/passengers`
- `GET /api/passengers/search`
- `PUT /api/passengers/{id}`
- `DELETE /api/passengers/{id}`
- `GET /api/passengers/{id}/bookings`

### Vehicle endpoints

- `POST /api/vehicles`
- `GET /api/vehicles/{id}`
- `GET /api/vehicles/driver/{driverId}`
- `PUT /api/vehicles/{id}`
- `DELETE /api/vehicles/{id}`

### Trip endpoints

- `POST /api/trips?driverId={driverId}`
- `GET /api/trips/{id}`
- `GET /api/trips`
- `GET /api/trips/driver/{driverId}`
- `GET /api/trips/search`
- `PUT /api/trips/{id}?driverId={driverId}`
- `PATCH /api/trips/{id}/complete?driverId={driverId}`
- `PATCH /api/trips/{id}/cancel?driverId={driverId}`
- `GET /api/trips/{id}/remaining-seats`
- `GET /api/trips/{id}/bookings`

### Booking endpoints

- `POST /api/bookings`
- `GET /api/bookings/{id}`
- `GET /api/bookings/passenger/{passengerId}`
- `PATCH /api/bookings/{id}/cancel?passengerId={passengerId}`
- `POST /api/bookings/calculate-price`

## Core Business Rules

The application enforces business rules similar to the following:

- Driver email and license number must be unique
- Vehicle registration numbers must be unique
- A trip must contain a valid ordered route with a departure and destination
- Price increases strictly between consecutive stopping points
- A passenger can book only valid segments belonging to the trip
- Seat availability must never be exceeded
- Trip status changes between `PUBLISHED` and `FULL`
- Canceling a trip cancels its confirmed bookings atomically
- Monetary values are handled using `BigDecimal` with 2 decimal places

## Development Notes

- The project uses JPA entities and repositories for persistence
- DTOs allow clean input/output separation from database entities
- MapStruct handles entity-to-DTO mapping
- Validation constraints and service-layer rules keep the domain logic centralized

## License

This project is intended for educational and internal project use unless otherwise specified by the owning organization.

## Next Steps

- Add Swagger/OpenAPI documentation for the REST API
- Add integration tests for booking and trip validation scenarios
- Add authentication and authorization for drivers and passengers
- Add front-end integration for trip search and booking flow
