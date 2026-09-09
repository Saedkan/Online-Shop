# Online Shop

Учебный проект интернет-магазина на Java.

## Технологии

- Java 21
- Maven
- JUnit 5

## Структура проекта

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

## Описание

Проект содержит базовую логику управления заказами интернет-магазина.

Основные возможности:

- создание идентификатора заказа;
- работа со статусами заказа;
- проверка допустимых переходов между статусами;
- обработка некорректных переходов;
- автоматическое тестирование бизнес-логики.

## Требования

Для запуска проекта необходимы:

- JDK 21 или выше;
- Apache Maven;
- Git.

Проверить установленные версии можно командами:

```bash
java -version
mvn -version
git --version
```

## Сборка проекта

Выполните в корневой папке проекта:

```bash
mvn compile
```

## Запуск тестов

```bash
mvn test
```

## Запуск приложения

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Если плагин запуска не настроен, приложение можно запустить через IDE, открыв класс `org.example.Main`.

## GitHub

Репозиторий проекта:

https://github.com/Saedkan/Online-Shop
