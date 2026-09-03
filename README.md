# Bank App 
 
Система реализует банковский перевод между счетами, включая:

- аутентификацию пользователя через Keycloak (Authorization Code Flow);
- проброс пользовательского JWT через API Gateway (Token Relay);
- проверку прав пользователя и сервисов на основе ролей realm_access;
- вызовы между микросервисами через Client Credentials Flow;
- разделение ответственности между следующими сервисами: 
  - front 
  - gateway 
  - transfer 
  - cash  
  - accounts
  - notification

---

# Взаимодействие компонентов

### UI (front)
- Аутентифицирует пользователя через Keycloak.
- Получает user-token (JWT).
- Делает запросы к Gateway с пользовательским токеном.
- Показывает страницу счета с информацией о владельце, а также элементы операции со счетом.

### Gateway API (gateway-service)
- Проверяет JWT как Resource Server.
- Выполняет кастомный Token Relay.
- Проксирует запросы в cash, accounts, transfer и notification.

### transfer
- Принимает запросы с user-token.
- Проверяет роли пользователя (`USER`, `transfer.write`).
- Для вызова сервисов accounts и notification получает service-token (Client Credentials Flow).
- Выполняет перевод между счетами.

### accounts
- Принимает запросы с service-token.
- Проверяет роли (`SERVICE`, `accounts.write`).
- Сообщает, кому принадлежит счёт.
- Для вызова notification получает service-token (Client Credentials Flow).
- Выполняет действия над счетам - прямые действия со счетами:
  - предоставление и обновление информации
  - изменение баланса

### cash
- Принимает запросы с service-token.
- Проверяет роли (`SERVICE`, `cash.write`).
- Для вызова сервисов accounts и notification получает service-token (Client Credentials Flow).
- Выполняет операции пополнения и снятия средств со счета.

### notification
- Принимает запросы с service-token.
- Проверяет роли (`SERVICE`, `notification.write`).
- Выводит событие приложения в консоль

### Keycloak
- Управляет пользователями, ролями и клиентами.
- Выдаёт JWT для UI и сервисов.

### Consul
- Реализует паттерн Externalized/Distributed Configs
- Реализует паттерн Service Discovery

### Postgresql
- Хранит информацию о пользовательских счетах банковского приложения
- Хранит информацию сервера авторизации Keycloak

---

# Запуск проекта

## 1. Keycloak

В проекте есть экспорт realm’а и тестовых пользователей:

[bank-app-realm.json](bank-app-chart/files/keycloak/import/bank-app-realm.json)

[bank-app-users-0.json](bank-app-chart/files/keycloak/import/bank-app-users-0.json)

Он включает:

- realm `bank-app`;
- роли (`USER`, `TRANSFER_WRITE`, `ACCOUNTS_WRITE`, `CASH_WRITE`, `NOTIFICATION_WRTIE`);
- клиентов (`front-app`, `accounts-service`, `cash-service`,`transfer-service`);
- протокольные мапперы.

**Создавать realm, роли, пользователей или клиентов вручную НЕ нужно.**
При запуске контейнера Keycloak автоматически применяет этот экспорт,
и вы сразу получаете полностью готовый к работе Keycloak без ручной настройки.

### Запуск Keycloak, PostgreSQL и  Consul

```bash
docker compose up
```

Keycloak поднимется на:

```
http://localhost:8080
```

PostgreSQL будет доступна по следующем реквизитам 
ВАЖНО: В проекте используется liquibase миграции, поэтому необходимо соблюсти данные реквизиты (в т.ч. указать схему) в конфигурации сервиса accounts
```
url:  jdbc:postgresql://localhost:5432/bank_db?currentSchema=accounts
login: postgres
password: postgres
```

Consul стартует на 
```
http://localhost:8500
```

---

## 2. Запуск сервисов

### gateway-service

```bash
./mvnw spring-boot:run -pl gateway-service
```

### accounts

```bash
./mvnw spring-boot:run -pl accounts
```

### transfer

```bash
./mvnw spring-boot:run -pl transfer
```

### cash

```bash
./mvnw spring-boot:run -pl cash
```

### notification

```bash
./mvnw spring-boot:run -pl notification
```

### front

```bash
./mvnw spring-boot:run -pl front
```

UI будет доступен по адресу:

```
http://localhost:8083
```


# Контрактные тесты 

В проекте настроены контрактные тесты Spring Cloud Contract для взаимодействия между сервисами 
- **accounts** (провайдер API) и **transfer** (клиент этого API)
- **accounts** (провайдер API) и **cash** (клиент этого API)
- **accounts** (провайдер API) и **front** (клиент этого API)
- **cash** (провайдер API) и **front** (клиент этого API)
- **transfer** (провайдер API) и **front** (клиент этого API)


## Продюсеры: accounts, transfer и cash

Для сервисов `accounts`, `transfer` и `cash` контракты описаны в `src/test/resources/contracts`.

При сборке модулей `accounts`, `transfer` и `cash`:

```bash
./mvnw clean verify -pl accounts
./mvnw clean verify -pl transfer
./mvnw clean verify -pl cash
```

Spring Cloud Contract:

- генерирует тесты по контрактам;
- выполняет их на стороне провайдера;
- собирает jar со стабами с classifier `stubs` 
  - артефакт `ru.yandex.practicum:accounts:…:stubs`
  - артефакт `ru.yandex.practicum:transfer:…:stubs`
  - артефакт `ru.yandex.practicum:cash:…:stubs`
- 
Этот jar со стабами необходимо выложить в Maven‑репозиторий.

## Консьюмеры: transfer, cash, front

В модулья-консьюмерах находятся классы для проверки клиентов:
- transfer -> ru.yandex.practicum.transfer.contract.AccountClientContractTest
- cash -> ru.yandex.practicum.cash.contract.AccountClientContractTest
- front -> ru.yandex.practicum.mybankfront.contract.*
  

В них `StubRunner` поднимает локальный HTTP‑сервер на порту `8888` и отвечает по контрактам, загруженным из jar‑файла стабов.  
Тест вызывает реальный клиент сервиса и проверяет, что он правильно формирует запрос  и корректно обрабатывает ответ.

Важно: чтобы этот тест прошёл, jar со стабами должны быть доступну в локальном Maven‑репозитории, откуда его заберёт Stub Runner.




## Как запустить тесты

1. Собрать и опубликовать стабы в локальный Maven‑репозиторий для запуска контрактных тестов:

```bash
./mvnw clean install -pl accounts
./mvnw clean install -pl transfer
./mvnw clean install -pl cash
```


2. Запустить необходимые типы тестов :

```bash
# юнит тесты
./mvnw clean test -Dgroups=unit

# интеграционные тесты
./mvnw clean test -Dgroups=integration

# тесты контроллеров
./mvnw clean test -Dgroups=controller

# тесты сервисов
./mvnw clean test -Dgroups=service

# контрактные тесты
./mvnw clean test -Dgroups=contract

```


