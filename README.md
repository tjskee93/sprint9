# Sprint10 - Микросервисная банковская система

## О проекте

My Bank — это учебная банковская система, построенная на микросервисной архитектуре. Проект демонстрирует использование современных технологий Spring Boot, Spring Cloud, OAuth2 Keycloak, Kubernetes и Helm.

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
| **PostgreSQL** | 5432 | База данных |

## Технологический стек

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud Gateway** - API Gateway
- **Spring Security + OAuth2** - Аутентификация и авторизация
- **Keycloak** - Identity Provider
- **Resilience4j** - Circuit Breaker
- **PostgreSQL** - Реляционная БД
- **Flyway** - Миграции БД
- **Kubernetes** - Оркестрация контейнеров
- **Helm** - Управление Kubernetes-приложениями
- **Docker** - Контейнеризация

## Реализована схема 2
### Фронт (Front UI) и сервер авторизации (OAuth 2.0) внутри Kubernetes-кластера:

## Развертывание в Kubernetes с Helm

### Предварительные требования

- Docker Desktop с включенным Kubernetes
- Установленный Helm
- Установленный kubectl

### 1. Сборка Docker-образов

bash
docker build -t accounts:0.0.1 -f accounts-service/Dockerfile .
docker build -t cash:0.0.1 -f cash-service/Dockerfile .
docker build -t gateway:0.0.1 -f gateway/Dockerfile .
docker build -t notifications:0.0.1 -f notifications-service/Dockerfile .
docker build -t transfer:0.0.1 -f transfer-service/Dockerfile .
docker build -t front-ui:0.0.1      -f front-ui/Dockerfile .

### 2. Установка приложения

#### Переключиться на контекст Docker Desktop
kubectl config use-context docker-desktop

#### Создать namespace
kubectl create namespace my-bank

#### Установить Helm-чарт
helm install my-bank helm/my-bank -n my-bank

#### Проверить статус подов
kubectl get pods -n my-bank

### 3. Запуск тестов
#### Запустить Helm тесты
helm test my-bank -n my-bank --logs

### 4. Доступ к приложению
#### Для доступа к сервисам из локальной машины необходимо пробросить порты из отдельных командных строк:

##### Проброс порта для Keycloak
kubectl port-forward -n my-bank service/keycloak 9090:9090

##### Проброс порта для Front-UI
kubectl port-forward -n my-bank service/front-ui 8080:8080

## Управление Helm-релизом
### Обновление приложения
#### Обновить релиз
helm upgrade my-bank helm/my-bank -n my-bank

#### Проверить статус
helm status my-bank -n my-bank

### Удаление приложения
helm uninstall my-bank -n my-bank

## Структура Helm-чарта

helm/my-bank/
├── Chart.yaml              # Метаданные чарта
├── values.yaml             # Основные значения конфигурации
├── charts/                 # Подчарты для каждого микросервиса
│   ├── accounts/
│   ├── cash/
│   ├── front-ui/
│   ├── gateway/
│   ├── transfer/
│   ├── notifications/
│   ├── keycloak/
│   └── postgres/
└── templates/
└── tests/
└── test-all.yaml   # Комплексные тесты всех сервисов