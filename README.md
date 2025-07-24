# Thrift Store

An online thrift store application built with Java, Spring Boot, and Thymeleaf. This project demonstrates best practices in software engineering, including SOLID principles, layered and component-based design, and the Model-View-Controller (MVC) architecture.

## Features
- User authentication and profile management
- Inventory management for thrift items
- Shopping cart functionality
- Order and payment processing
- Item return management
- Dashboard for users and admins

## Tech Stack
- **Java 17**
- **Spring Boot 3.3.5**
- **Spring Data JPA**
- **Thymeleaf** (server-side rendering)
- **MySQL** (database)
- **Springdoc OpenAPI** (API documentation)

## Design and Architecture
This project is structured using:
- **SOLID Principles**: Ensuring maintainable, extensible, and testable code.
- **Layered Architecture**: Separating concerns into controller, service, repository, and entity layers.
- **Component-Based Design**: Each feature is encapsulated in its own set of classes.
- **MVC Pattern**: Clean separation between data (Model), UI (View), and business logic (Controller).

### Main Components
- **Controllers** (`controller/`): Handle HTTP requests and responses.
- **Services** (`service/`): Contain business logic and interact with repositories.
- **Repositories** (`repository/`): Interface with the database using Spring Data JPA.
- **Entities** (`entity/`): Define the data models mapped to database tables.
- **Templates** (`resources/templates/`): Thymeleaf HTML templates for the UI.

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL

### Setup Instructions
1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd thrift-store
   ```
2. **Configure the database:**
   - Create a MySQL database (e.g., `thriftstore`).
   - Update `src/main/resources/application.properties` with your DB credentials.
3. **Build the project:**
   ```bash
   ./mvnw clean install
   ```
4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Access the app:**
   - Open [http://localhost:8080](http://localhost:8080) in your browser.

### API Documentation
- Swagger UI is available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (after starting the app).

## Project Structure
```
├── src/main/java/com/thriftstore/
│   ├── controller/      # Web controllers (MVC)
│   ├── service/         # Business logic (Service layer)
│   ├── repository/      # Data access (Repository layer)
│   └── entity/          # JPA entities (Model)
├── src/main/resources/
│   ├── templates/       # Thymeleaf HTML templates (View)
│   └── application.properties
├── pom.xml              # Maven build file
```

## Contributing
Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the MIT License.
