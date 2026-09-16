# Bank App

Система реализует банковский перевод между счетами, включая:

- аутентификацию пользователя через Keycloak (Authorization Code Flow);
- проброс пользовательского JWT через API Gateway (Token Relay);
- проверку прав пользователя и сервисов на основе ролей realm_access;
- вызовы между микросервисами через Client Credentials Flow;
- разделение ответственности между следующими сервисами:
    - front
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

- Читает сообщения из топика bank-app-notification Kafka
- Выводит событие приложения в консоль

### kafka

- Осуществляет взаимодействие сервисов accounts, transfer и cash с сервисом notification через топик
  bank-app-notification
- Реализует стратегию at-least-once

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

### front

Для запуска UI компонента в корне проекта выполните

```bash
./mvnw spring-boot:run -pl front
```

Фронт запускается отдельно от кластера k8s, а для связи с микросервисами используется единая точка входа - ingress. Для
работы в ingress необходимо настроить ingress-controller (в нашем случае это nginx-controller)
Выполните

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace
```

Далее находим внешний порт nginx-controller

```bash
kubectl get svc -n ingress-nginx                     

NAME                                 TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
ingress-nginx-controller             NodePort    10.96.204.166   <none>        80:31717/TCP,443:30651/TCP   7d19h
```

Далее указывает адрес ingress в application.yaml или в переменной окружения GATEWAY_API

```yaml
custom:
  baseUrl:
    api-gateway: ${GATEWAY_API:http://bank-api:31717}
```

После запуска UI будет доступен по адресу:

```
http://localhost:8083
```
Так как `front` работает вне кластера k8s, для отправки трейсов в Zipkin и логов в Logstash ему нужно явно передать
переменные окружения (адреса берутся из ingress/NodePort, см. раздел [Observability](#observability)):

```bash
export ZIPKIN_ENDPOINT=http://zipkin.bank-api:31717/api/v2/spans
export LOGSTASH_HOST=localhost
export LOGSTASH_PORT=30500

./mvnw spring-boot:run -pl front
```

### kafka

Топик для реализации механизма уведомлений имеет дефолтное значени `bank-app-notification`, но может быть изменен. Для
этого необходимо в каждом определить переменную окружения `BANK_APP_NOTIFICATION_TOPIC` 

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
docker build -f accounts/Dockerfile -t accounts-service:1.0.0 .
```

### transfer

```bash
docker build -f transfer/Dockerfile -t transfer-service:1.0.0 .
```

### cash

```bash
docker build -f cash/Dockerfile -t cash-service:1.0.0 .
```

### notification

```bash
docker build -f notification/Dockerfile -t notification-service:1.0.0 .
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


# Observability

Стек мониторинга разворачивается вместе с остальными сервисами как Helm-сабчарты umbrella charts `bank-app-chart` и
покрывает три компонента: трейсы (Zipkin), метрики (Prometheus + Grafana) и логи (ELK: Elasticsearch, Logstash, Kibana).

## Компоненты

| Компонент  | Назначение                                                    | Chart                                    |
|------------|----------------------------------------------------------------|-------------------------------------------|
| Zipkin     | Хранение и просмотр распределённых трейсов                    | `charts/zipkin`                            |
| Prometheus | Сбор и хранение метрик (через `kube-prometheus-stack`)        | `kube-prometheus-stack` (сабчарт prometheus) |
| Grafana    | Дашборды по метрикам                                           | `kube-prometheus-stack` (сабчарт grafana)  |
| Alertmanager | Обработка алертов из `PrometheusRule`                        | `kube-prometheus-stack` (сабчарт alertmanager) |
| Elasticsearch | Хранилище логов                                             | `charts/elasticsearch`                     |
| Logstash   | Приём логов и запись в Elasticsearch | `charts/logstash-custom`                   |
| Kibana     | Просмотр и поиск логов                                        | `charts/kibana`                            |

Каждый сервис (`accounts`, `cash`, `transfer`, `notification`, `front`) подключает общий модуль `common`, который:

- настраивает трейсинг через Micrometer Tracing + Brave и отправляет спаны в Zipkin
  (`management.tracing.export.zipkin.endpoint`);
- экспортирует метрики в формате Prometheus на `/actuator/prometheus`;
- отправляет структурированные JSON-логи в Logstash.

## Запуск

Все компоненты observability входят в umbrella chart `bank-app-chart` и поднимаются вместе с приложением:

```bash
cd bank-app-chart
helm dependency update
helm upgrade --install bank-app . --namespace bank-ns --create-namespace
```

Проверить, что поды стека наблюдаемости поднялись:

```bash
kubectl get pods -n bank-ns | grep -E "zipkin|prometheus|grafana|alertmanager|elasticsearch|logstash|kibana"
```

## Доступ через UI

Доступ ко всем UI даётся через тот же ingress-controller, что и к API (`http://bank-api:<nodePort>`), по отдельным
хостам, объявленным в `values.yaml -> ingress.observability`:

| UI         | Host (пример)          | Логин/пароль          |
|------------|-------------------------|------------------------|
| Zipkin     | `http://zipkin.bank-api:31717`     | —                      |
| Prometheus | `http://prometheus.bank-api:31717` | —                      |
| Grafana    | `http://grafana.bank-api:31717`    | `admin` / `admin` (см. `kube-prometheus-stack.grafana.adminPassword`) |
| Kibana     | `http://kibana.bank-api:31717`     | —                      |

Для резолва этих хостов локально добавьте их в `/etc/hosts`, указав на IP ноды/кластера:

```
127.0.0.1 bank-api zipkin.bank-api prometheus.bank-api grafana.bank-api kibana.bank-api
```

(порт `31717` — это NodePort `ingress-nginx-controller`, см. раздел про запуск `front`; в вашем окружении он может
отличаться).

## Дашборды Grafana

Дашборды заведены как ConfigMap с лейблом `grafana_dashboard: "1"` (`bank-app-chart/dashboards/*.json`,
шаблон `templates/dashboards/configmaps.yaml`) и автоматически подхватываются sidecar-контейнером Grafana
(`kube-prometheus-stack.grafana.sidecar.dashboards.enabled: true`). После деплоя они появляются в Grafana автоматически:

- **Bank App / JVM (Micrometer) & Business Metrics** — адаптированный community-дашборд
  [4701 "JVM (Micrometer)"](https://grafana.com/grafana/dashboards/4701-jvm-micrometer/) (память heap/non-heap, GC,
  CPU, threads, classloading, buffer pools, HTTP I/O overview) с добавленным рядом панелей "Bank App Business Metrics
  (Transfer & Cash)" — количество и длительность операций перевода и кассовых операций по исходу
  (`success`/`error`), error ratio;
- **Bank App / Spring Boot HTTP & Kafka** — RPS, 5xx error rate, latency (p50/p95/p99) в разрезе `uri`, Kafka
  producer/consumer rate.

Оба дашборда используют переменную `$application`, поэтому каждая метрика во всех сервисах помечается общим тегом
`application=${spring.application.name}` (настроено в `common/src/main/resources/common-observability.yaml` через
`management.metrics.tags.application`) — без этого тега часть панелей была бы пустой.

## Бизнес-метрики

`transfer` и `cash` публикуют собственные метрики через `MeterRegistry` (видны на `/actuator/prometheus`):

- `bank_transfer_total{outcome=...}` — счётчик операций перевода (успех/ошибка);
- `bank_transfer_duration_seconds{outcome=...}` — таймер длительности перевода;
- `bank_transfer_amount` — сумма переводов (DistributionSummary);
- `bank_cash_operation_total{action=GET|PUT, outcome=...}` — счётчик кассовых операций (снятие/пополнение, успех/ошибка);
- `bank_cash_operation_duration_seconds{action=..., outcome=...}` — таймер длительности кассовой операции;
- `bank_cash_amount{action=...}` — сумма кассовых операций.

## Алерты Prometheus

Алерты заведены как `PrometheusRule` (`templates/alerts/prometheusrule.yaml`) и обрабатываются Alertmanager:

| Alert                         | Условие                                                             | Severity |
|--------------------------------|----------------------------------------------------------------------|----------|
| `BankAppServiceDown`           | `up == 0` дольше 2 минут для accounts/cash/transfer/notification     | critical |
| `BankAppHighHttp5xxErrorRate`  | доля HTTP 5xx > 5% за 5 минут                                        | critical |
| `BankAppHighHttpLatency`       | p95 времени ответа > 1s за 5 минут                                   | warning  |
| `BankAppJvmHeapUsageHigh`      | используемый heap > 85% от максимума за 5 минут                      | warning  |
| `BankAppJvmGcTimeHigh`         | время в GC > 30% за 5 минут                                          | warning  |
| `BankAppTransferErrorRateHigh` | доля ошибочных переводов > 10% за 5 минут                            | critical |
| `BankAppCashErrorRateHigh`     | доля ошибочных кассовых операций > 10% за 5 минут                    | critical |


# Контрактные тесты

В проекте настроены контрактные тесты Spring Cloud Contract для взаимодействия между сервисами

- **accounts** (провайдер API) и **transfer** (клиент этого API)
- **accounts** (провайдер API) и **cash** (клиент этого API)
- **accounts** (провайдер API) и **front** (клиент этого API)
- **cash** (провайдер API) и **front** (клиент этого API)
- **transfer** (провайдер API) и **front** (клиент этого API)
- **accounts** (продьюсер kafka) и **notification** (консьюмер kafka)
- **cash** (продьюсер kafka) и **notification** (консьюмер kafka)
- **transfer** (продьюсер kafka) и **notification** (консьюмер kafka)

## Продюсеры: accounts, transfer и cash

Для сервисов `accounts`, `transfer` и `cash` контракты описаны 

#### Rest contracts
`src/test/resources/contracts/rest/`

#### Messaging contracts
`src/test/resources/contracts/messaging/`


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

## Консьюмеры: transfer, cash, front, notification

#### Rest contracts

В модулья-консьюмерах находятся классы для проверки клиентов:

- transfer -> ru.yandex.practicum.transfer.contract.AccountClientContractTest
- cash -> ru.yandex.practicum.cash.contract.AccountClientContractTest
- front -> ru.yandex.practicum.mybankfront.contract.*

В них `StubRunner` поднимает локальный HTTP‑сервер на порту `8888` и отвечает по контрактам, загруженным из jar‑файла
стабов.  
Тест вызывает реальный клиент сервиса и проверяет, что он правильно формирует запрос и корректно обрабатывает ответ.

Важно: чтобы этот тест прошёл, jar со стабами должны быть доступну в локальном Maven‑репозитории, откуда его заберёт
Stub Runner.

#### Messaging contracts

Контрактные тесты консьюмера Kafka расположены в модуле `notification` в пакете
`ru.yandex.practicum.notification.contract.*`

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


