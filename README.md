# Online Shop

CSS-3008 Lab 2 — Join Spring Boot to your product.

## Product

Online Shop is an order management system for an e-commerce process.

An order moves through a controlled lifecycle:

`CART → PAID → SHIPPED → DELIVERED`

A return is handled as a separate process.

## Business Rules

| From      | To       | Allowed? |
| --------- | -------- | -------- |
| CART      | PAID     | Yes      |
| PAID      | SHIPPED  | Yes      |
| DELIVERED | RETURNED | No       |
| SHIPPED   | CART     | No       |

### Forbidden transitions

1. `DELIVERED → RETURNED`

A delivered order cannot be returned directly. It must go through a separate return process.

2. `SHIPPED → CART`

A shipped order is already in logistics and cannot be moved back to the cart.

## Technologies

- Java 21
- Spring Boot 3.3.0
- Maven
- JUnit 5

Arrows point inward.

The domain layer does not import Spring.

Spring is used at the application/configuration edge.

## Spring Integration

`Application` starts the Spring Boot application.

`OrderService` is a Spring `@Service` and receives a `Rule` through constructor injection.

The `Rule` bean is configured in `Config`.

The domain classes remain plain Java classes and do not contain Spring annotations or imports.

## Rule Chain

The application uses two collaborating rule implementations:

- `TransitionRule` — validates the normal order lifecycle.
- `ForbiddenRule` — blocks forbidden business transitions.

They are combined by `RuleChain` and injected into `OrderService` through the `Rule` interface.

## Running Tests

```bash
mvn -q verify
```

## Running the Application

```bash
mvn spring-boot:run
```

The application does not require REST, a database, or Docker for this lab.
