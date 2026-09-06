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

### keycloak

- Управляет пользователями, ролями и клиентами.
- Выдаёт JWT для UI и сервисов.


### Postgresql

- Хранит информацию о пользовательских счетах банковского приложения
- Хранит информацию сервера авторизации Keycloak

---

# Запуск проекта

## 1. Keycloak

Кейклок контейнер находится вне кластера k8s, как и приложение UI (front). Параметры запуска Keycloak можно
указать/изменить в файле переменных окружения

[.env](common/keycloak/.env)

В проекте есть экспорт realm’а и тестовых пользователей:

[bank-app-realm.json](common/keycloak/import/bank-app-realm.json)

[bank-app-users-0.json](common/keycloak/import/bank-app-users-0.json)

Он включает:

- realm `bank-app`;
- роли (`USER`, `TRANSFER_WRITE`, `ACCOUNTS_WRITE`, `CASH_WRITE`, `NOTIFICATION_WRTIE`);
- клиентов (`front-app`, `accounts-service`, `cash-service`,`transfer-service`);
- протокольные мапперы.

**Создавать realm, роли, пользователей или клиентов вручную НЕ нужно.**
При запуске контейнера Keycloak автоматически применяет этот экспорт,
и вы сразу получаете полностью готовый к работе Keycloak без ручной настройки.

### Запуск Keycloak

```bash
docker run -d --name bank-keycloak -p 8080:8080 --env-file ./common/keycloak/.env -v ./common/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:26.6.3 start-dev --import-realm
```

Контейнер Keycloak поднимется на:

```
http://localhost:8080
```

---

## 2. Запуск сервисов

Сервисная часть организована в виде Helm charts типа Umbrella. В первую очередь необходимо добавить репозитории

```bash

helm repo add stable https://charts.helm.sh/stable 
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
```

Чтобы разрвернуть проект локально, необходимо собрать образы каждого микросервиса.
Для этого в каталоге bank-app выполнить следующие команды сборки образов

### accounts

```bash
docker build -f accounts/Dockerfile -t accounts-service:0.1.0 .
```

### transfer

```bash
docker build -f transfer/Dockerfile -t transfer-service:0.1.0 .
```

### cash

```bash
docker build -f cash/Dockerfile -t cash-service:0.1.0 .
```

### notification

```bash
docker build -f notification/Dockerfile -t notification-service:0.1.0 .
```

### PostgreSQL

Для БД используется готовый chart postgresql, в values.yaml опеределены реквизиты для подключения

```
url: jdbc:postgresql://host.docker.internal:30432/bank_db?currentSchema=accounts
username: bank_admin
password: bank_admin
database: bank_db
```

Далее необходимо обновить и собрать зависимости helm chart. В каталоге bank-app-chart выполняем обновление зависимостей
чарта

```bash
helm dependency update
```

### Установка релиза

Прежде чем устанавливать helm релиз, можно проверить его манифесты на наличие ошибок. Для быстрой проверки синтаксиса
можно выполнить

```bash
helm lint . --with-subcharts
```

Для более тщательной проверки/имитации установки можно выполнить

```bash
helm install bank-app . --dry-run=client --debug
```

Для проверки рендеринга манифестов (без имитации запуска) можно проверить

```bash
helm template .
```

После проверки можно устанавливать релиз

```bash
# установка
helm intall bank-app .

# обновление
helm upgrade --install bank-app .

# удаление 
helm uninstall bank-app
```

В проекте предусмотрены тесты установки релиза. Нужно выполнить 

```bash
helm test bank-app 
```

### front

Для запуска UI компонента в корне проекта выполните

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

В них `StubRunner` поднимает локальный HTTP‑сервер на порту `8888` и отвечает по контрактам, загруженным из jar‑файла
стабов.  
Тест вызывает реальный клиент сервиса и проверяет, что он правильно формирует запрос и корректно обрабатывает ответ.

Важно: чтобы этот тест прошёл, jar со стабами должны быть доступну в локальном Maven‑репозитории, откуда его заберёт
Stub Runner.

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


