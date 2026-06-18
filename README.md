# sprint9 - Микросервисная банковская система

## О проекте

My Bank — это учебная банковская система, построенная на микросервисной архитектуре. Проект демонстрирует использование современных технологий Spring Boot, Spring Cloud, OAuth2 Keycloak, Consul Service Discovery и Resilience4j.

### Архитектура

Система состоит из следующих микросервисов:

| Сервис | Порт | Описание |
|--------|------|----------|
| **Gateway** | 8081 | API Gateway на Spring Cloud Gateway, маршрутизация запросов, проверка JWT |
| **Front UI** | 8080 | Пользовательский интерфейс на Thymeleaf |
| **Accounts Service** | 8083 | Управление банковскими счетами пользователей |
| **Cash Service** | 8090 | Операции с наличными (пополнение/снятие) |
| **Transfer Service** | 8084 | Переводы между счетами |
| **Notifications Service** | 8082 | Отправка уведомлений |
| **Keycloak** | 9090 | Сервер авторизации (OAuth2 / OIDC) |
| **Consul** | 8500 | Service Discovery и хранение конфигурации |
| **PostgreSQL** | 5432 | База данных |

## Технологический стек

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Consul** - Service Discovery & Config
- **Spring Security + OAuth2** - Аутентификация и авторизация
- **Keycloak** - Identity Provider
- **Resilience4j** - Circuit Breaker
- **PostgreSQL** - Реляционная БД
- **Flyway** - Миграции БД
- **Docker / Docker Compose** - Контейнеризация

### 1. Клонирование репозитория

git clone <repository-url>
cd sprint9

### 2. Запуск всех сервисов
docker-compose up -d