# Online Shop

An educational Java project for an online shop.

## Technologies

- Java 21
- Maven
- JUnit 5

## Project Structure

```text
src/
├── main/java/org/example/
│   ├── Main.java
│   ├── OrderId.java
│   ├── OrderPolicy.java
│   └── OrderStatus.java
└── test/java/org/example/
    └── OrderPolicyTest.java
```

## Description

The project contains the basic order management logic for an online shop.

Main features:

- creating an order identifier;
- working with order statuses;
- validating allowed transitions between statuses;
- handling invalid transitions;
- automated testing of the business logic.

## Requirements

To run the project you need:

- JDK 21 or higher;
- Apache Maven;
- Git.

You can check the installed versions with:

```bash
java -version
mvn -version
git --version
```

## Building the Project

Run in the project root folder:

```bash
mvn compile
```

## Running Tests

```bash
mvn test
```

## Running the Application

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

If the run plugin is not configured, the application can be started via an IDE by opening the `org.example.Main` class.

## GitHub

Project repository:

https://github.com/Saedkan/Online-Shop
