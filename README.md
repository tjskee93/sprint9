# Sprint12 - Микросервисная банковская система

## О проекте

My Bank — это учебная банковская система, построенная на микросервисной архитектуре. Проект демонстрирует использование современных технологий Spring Boot, Spring Cloud, OAuth2 Keycloak, Apache Kafka, Kubernetes и Helm.

### Архитектура

Система состоит из следующих микросервисов:

| Сервис | Порт | Описание |
|--------|------|----------|
| **Gateway** | 8081 | API Gateway на Spring Cloud Gateway, маршрутизация запросов, проверка JWT |
| **Front UI** | 8080 | Пользовательский интерфейс на Thymeleaf |
| **Accounts Service** | 8083 | Управление банковскими счетами пользователей |
| **Cash Service** | 8090 | Операции с наличными (пополнение/снятие) |
| **Transfer Service** | 8084 | Переводы между счетами |
| **Notifications Service** | 8082 | Отправка уведомлений (потребитель Kafka) |
| **Keycloak** | 9090 | Сервер авторизации (OAuth2 / OIDC) |
| **PostgreSQL** | 5432 | База данных |
| **Kafka** | 9092 | Брокер сообщений для асинхронного взаимодействия |
| **Zipkin** | 9411 | Cистема распределённых трассировок |
| **Prometheus** | 8089 | Cистема сбора и анализа метрик |
| **Grafana** | 3000 | Платформа для визуализации, мониторинга и анализа данных |
| **Logstash** | 5000 | Cистема сбора логов |
| **Elasticsearch** | 9200 | Платформа для хранения логов |
| **Kibana** | 5601 | Платформа для анализа логов логов |

### Взаимодействие между сервисами

#### Спринт 10 (REST)
В предыдущей версии взаимодействие между сервисами происходило через REST API через Gateway.

#### Спринт 11 (Kafka)
В текущей версии внедрена асинхронная коммуникация через Apache Kafka:

- **Accounts Service**, **Cash Service**, **Transfer Service** отправляют события в Kafka-топик `notifications-service` при выполнении операций:
    - Изменение данных аккаунта → `ACCOUNT_UPDATED`
    - Изменение баланса → `BALANCE_CHANGED`
    - Снятие наличных → `CASH_WITHDRAW`
    - Пополнение наличных → `CASH_DEPOSIT`
    - Перевод средств → `TRANSFER_SENT`

#### Спринт 12 (Zipkin, Prometheus, Grafana, ELK)
В текущей версии внедрено:

- **Zipkin**:
    - трейсинг запросов с использованием системы распределённых трассировок Zipkin
- **Prometheus и Grafana**:
    - мониторинги/графики метрик и алерты с использованием Prometheus и Grafana
- **Logstash, Elasticsearch, Kibana**:
    - логирование с использованием ELK-стека

## Технологический стек

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud Gateway** - API Gateway
- **Spring Security + OAuth2** - Аутентификация и авторизация
- **Keycloak** - Identity Provider
- **Apache Kafka** - Асинхронное взаимодействие между сервисами
- **Resilience4j** - Circuit Breaker
- **PostgreSQL** - Реляционная БД
- **Flyway** - Миграции БД
- **Kubernetes** - Оркестрация контейнеров
- **Helm** - Управление Kubernetes-приложениями
- **Docker** - Контейнеризация
- **Zipkin** - Трейсинг запросов
- **Zipkin** - Трейсинг запросов
- **Prometheus и Grafana** - Мониторинги/графики метрик и алерты
- **ELK** - Cбор, хранение, обработка и анализ логов микросервисов

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

##### Проброс порта для Zipkin
kubectl port-forward -n my-bank service/zipkin 9411:9411

##### Проброс порта для Prometheus
kubectl port-forward -n my-bank service/prometheus 8089:8089

##### Проброс порта для Grafana
kubectl port-forward -n my-bank service/grafana 3000:3000

##### Проброс порта для Kibana
kubectl port-forward -n my-bank service/kibana 5601:5601

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
│   ├── elasticsearch/
│   ├── gateway/
│   ├── front-ui/
│   ├── gateway/
│   ├── grafana/
│   └── kafka/
│   └── keycloak/
│   └── kibana/
│   └── logstash/
│   └── notifications/
│   └── postgres/
│   └── prometheus/
│   └── transfer/
│   └── zipkin/
└── templates/
└── tests/
└── test-all.yaml   # Комплексные тесты всех сервисов