# Online Shop

We build an **online shop platform**. People track **orders** (items in cart, 
purchases, deliveries).

## Product

An order management system that validates state transitions for e-commerce orders. The system ensures orders flow through proper lifecycle stages without invalid state changes.

## Core item

**Order** - A core business entity identified by OrderId, tracked through its lifecycle with status changes: CART → PAID → SHIPPED → DELIVERED → RETURNED (optional).

## Status table

| From | To | Allowed? | 
|------|-----|----------|
| CART | PAID | ✅ |
| PAID | SHIPPED | ✅ |
| DELIVERED | RETURNED | ❌ |
| SHIPPED | CART | ❌ |

## Forbidden — why

1. **DELIVERED → RETURNED**: Cannot return a delivered order directly. Must use the separate return process with approval workflow.

2. **SHIPPED → CART**: Cannot move a shipped order back to cart. Order is already in logistics and cannot be reverted.

---

## Technologies

- Java 21
- Maven
- JUnit 5

## Project Structure

```text
src/
├── main/java/org/example/
│   ├── OrderId.java
│   ├── OrderStatus.java
│   └── OrderPolicy.java
└── test/java/org/example/
    └── OrderPolicyTest.java
```

## Building and Running

```bash
mvn clean test
```

All tests must pass with green status.
