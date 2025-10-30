# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.5.7 e-commerce application built with Java 17, using Gradle as the build tool. The project follows standard Spring Boot project structure with JPA for data persistence and REST API capabilities.

## Build and Development Commands

### Building the Project
```bash
./gradlew build
```

### Running the Application
```bash
./gradlew bootRun
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests io.hhplus.ecommerce.EcommerceApplicationTests

# Run tests in a specific package
./gradlew test --tests io.hhplus.ecommerce.*
```

### Cleaning Build Artifacts
```bash
./gradlew clean
```

## Architecture

**Base Package**: `io.hhplus.ecommerce`

**Tech Stack**:
- Spring Boot 3.5.7
- Spring Data JPA (database persistence layer)
- Spring Web (REST API)
- Lombok (reduces boilerplate code)
- Spring Boot DevTools (development utilities)
- JUnit 5 (testing)

**Database**: MySQL connector is commented out in build.gradle - currently configured for in-memory or other database

**Project Structure**: Standard Spring Boot layered architecture expected (controllers, services, repositories, entities)