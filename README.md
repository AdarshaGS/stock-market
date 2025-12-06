# Stock Market Application

## Overview
A Spring Boot application designed to help users manage their stock portfolios, analyze sector diversification, and make informed investment decisions. The system integrates with third-party APIs to fetch real-time market data.

## Features
-   **Stock Data Retrieval**: Fetch stock details (price, company profile, sector) using external APIs (Indian Market Data).
-   **Portfolio Management**: Add stocks to a user's portfolio and track investments.
-   **Portfolio Summary**: View total investment, current value, profit/loss, and sector allocation.
-   **Diversification Analysis**: (In Progress) Analyze portfolio diversification scores to reduce risk.
-   **Sector Tracking**: Automatically categorizes stocks into sectors.

## Technology Stack
-   **Java**: 17
-   **Framework**: Spring Boot 4.0.0
-   **Database**: MySQL
-   **ORM**: Spring Data JPA
-   **Build Tool**: Gradle
-   **Utilities**: Lombok

## Prerequisites
-   Java 17 installed.
-   MySQL Server running locally.
-   Gradle (optional, `gradlew` wrapper is included).

## Setup & and Installation

1.  **Clone the repository**
    ```bash
    git clone <repository_url>
    cd stocks
    ```

2.  **Database Configuration**
    -   Create a MySQL database named `stock-market`.
    -   The application connects to `localhost:3306` with user `root` and password `mysql`.
    -   To change these credentials, update `src/main/resources/application.properties`:
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/stock-market...
        spring.datasource.username=YOUR_USERNAME
        spring.datasource.password=YOUR_PASSWORD
        ```

3.  **Build the project**
    ```bash
    ./gradlew build
    ```

4.  **Run the application**
    ```bash
    ./gradlew bootRun
    ```

## API Endpoints

### Stock

-   **Get Stock Details**
    -   `GET /api/stocks/{symbol}`
    -   Fetches details for a given stock symbol (e.g., `RELIANCE`, `TCS`). If not in DB, fetches from third-party and saves it.

### Portfolio

-   **Add Portfolio Item**
    -   `POST /api/portfolio`
    -   Body: `Portfolio` JSON object.
    -   Adds a stock transaction to the user's portfolio.

-   **Get Portfolio Summary**
    -   `GET /api/portfolio/summary/{userId}`
    -   Returns total investment, current value, profit/loss, and sector allocation for the user.

-   **Get Diversification Score**
    -   `GET /api/portfolio/diversification-score/{userId}`
    -   (Stub) Returns diversification analysis for the user.

## Project Structure

-   `src/main/java/com/stocks/api`: General Stock APIs.
-   `src/main/java/com/stocks/diversification/portfolio`: Portfolio management logic (API, Data, Service, Repo).
-   `src/main/java/com/stocks/diversification/sectors`: Sector management.
-   `src/main/java/com/stocks/thirdParty`: Integration with external Stock APIs.
