# Как пользоваться сервисом

```shell
mvn clean -U package -e
```

```shell
docker compose up --build -d
```

## Функциональные требования

### API для регистрации и аутентификации пользователей

Зарегистрировать пользователя

```shell
curl -X POST -H "Content-Type:application/json" -d '{
  "login" : "your@mail.ru",
  "password" : "password",
  "phoneNumber" : "+79123456789"
}' --url "http://localhost:8080/api/auth"
```

Ответ:

```json
{
  "id": 2,
  "login": "your@mail.ru",
  "phoneNumber": "+79123456789",
  "role": "USER"
}
```

---

Авторизоваться

```shell
curl -X POST -H "Content-Type:application/json" -d '{
  "login" : "your@mail.ru",
  "password" : "password"
}' --url "http://localhost:8080/api/auth/login"
```

Ответ:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b3VyQG1haWwucnUiLCJ1c2VySWQiOjIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzgwMDg1Nzc3LCJleHAiOjE3ODAwODkzNzd9.t_-BEZaOrA9cqMT5RGHA1ery-Vdpc08kLR3OTNdOhYo"
}
```

### API пользователя

Сгенерировать OTP

```shell
curl -X POST -H 'Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b3VyQG1haWwucnUiLCJ1c2VySWQiOjIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzgwMDg4MzUzLCJleHAiOjE3ODAwOTE5NTN9.pJitetxcMGtrUBefzzbKCMc171G2dSG0rbNN4-gH0yo' -H "Content-Type:application/json" -d '{
  "operationId" : "test-operation-id",
  "userId" : 2
}' --url "http://localhost:8080/api/users/otp/generate"
```

Ответ:

```json
{
  "userId": 2,
  "operationId": "test-operation-id",
  "code": "400322",
  "status": "ACTIVE",
  "expiresAt": "2026-05-29T21:04:41.601383026Z"
}
```

### Каналы рассылки сгенерированных кодов

> Для полноценного тестирования необходим Telegram бот!

Отправка кода по СМС (указаны логи из SMPP-сервера, который получил код):

```shell
$ docker logs otp-smpp 2>&1 | grep "Your code"
2026.05.29 20:59:41 704 INFO    31 short_message=Your code: 400322
```

---

Отправка кода по email (указан скриншот UI SMTP-сервера, который получил письмо с кодом):

![img.png](img.png)

UI доступен по адресу http://localhost:8025

---

Отправка кода в Telegram бот (указан скриншот веб-версии бота):

![img_1.png](img_1.png)

Сохранение кода в файл:

```text
INFO  c.d.o.d.i.FileDeliveryChannel - Successfully wrote OTP code to file: /data/otp/codes.txt
```

Проверка файла:

```shell
docker exec -it otp-service bash
root@7c6361c37357:/usr/local/tomcat# cat /data/otp/codes.txt 
[2026-05-29 21:03:52] User: your@mail.ru, Code: 400322
```

---

Провалидировать операцию с помощью OTP

```shell
curl -X POST -H 'Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b3VyQG1haWwucnUiLCJ1c2VySWQiOjIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzgwMDg4MzUzLCJleHAiOjE3ODAwOTE5NTN9.pJitetxcMGtrUBefzzbKCMc171G2dSG0rbNN4-gH0yo' -H "Content-Type:application/json" -d '{
  "code" : "400322",
  "operationId" : "test-operation-id",
  "userId" : 2
}' --url "http://localhost:8080/api/users/otp/validate"
```

Ответ:

```text
true
```

## Админские операции

Получить список пользователей

```shell
curl -X GET -H 'Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzgwMDg5MjU4LCJleHAiOjE3ODAwOTI4NTh9.DbeH-fGWKxJ7PDQ2NxO-A72jR5h0sSKtXDFS3z27ZX8' --url "http://localhost:8080/api/admin/users"
```

Ответ:

```json
[
  {
    "id": 2,
    "login": "your@mail.ru",
    "phoneNumber": "+79123456789",
    "role": "USER"
  }
]
```

---

Удалить пользователя

```shell
curl -X DELETE -H 'Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzgwMDg5MjU4LCJleHAiOjE3ODAwOTI4NTh9.DbeH-fGWKxJ7PDQ2NxO-A72jR5h0sSKtXDFS3z27ZX8' -H "Content-Type:application/x-www-form-urlencoded" -d '' --url "http://localhost:8080/api/admin/users?userId=2"
```

Ответ:

![img_2.png](img_2.png)

---

Обновить конфигурацию OTP

```shell
curl -X PUT -H 'Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzgwMDg5NjExLCJleHAiOjE3ODAwOTMyMTF9.o5i2MRwSbJcJgEGzxx942r4NBbjC2kcoG3D3Kr1gk6U' -H "Content-Type:application/json" -d '{
  "codeLength" : 3,
  "ttlSeconds" : 3600
}' --url "http://localhost:8080/api/admin/otp/config"
```

Ответ:

```json
{
  "id": 1,
  "codeLength": 3,
  "ttlSeconds": 3600
}
```

---

Реализован механизм, который отмечает просроченные OTP-коды раз в определенный интервал времени и
присваивает им статус EXPIRED:

```text
otp-service  | 2026-05-29 22:53:39.452 [main] DEBUG c.d.o.s.i.StartupListener - Starting OTP expiration service
otp-service  | 2026-05-29 22:53:39.453 [main] DEBUG c.d.o.s.i.StartupListener - OTP expiration service started
otp-service  | 2026-05-29 22:55:15.680 [pool-1-thread-1] DEBUG c.d.o.s.i.DefaultExpirationService - Deactivated 1 OTP codes with expiration before 2026-05-29T22:55:15.679387796Z
```

Проверка статуса кода:
```shell
docker exec -it otp-db bash
psql -U admin -d otp_db
select code, status from otp_codes;

  code  | status  
--------+---------
 730167 | EXPIRED

```

## Технические требования

База данных реализована с помощью PostgreSQL 17, взаимодействие с базой реализовано через JDBC.
Транзакционность реализована через собственную реализацию аннотации @Transactional, поддерживается
пулл коннектов к БД:

```text
│   OtpConfigRepository.java
│   OtpRepository.java
│   UserRepository.java
│   
├───config
│       RepositoryConfig.java
│       
├───datasource
│   │   DataSource.java
│   │   
│   └───impl
│           PooledDataSource.java
│           
├───impl
│       JdbcOtpConfigRepository.java
│       JdbcOtpRepository.java
│       JdbcUserRepository.java
│       
├───transaction
│       PerThreadConnectionHolder.java
│       Transactional.java
│       TransactionalProxy.java
│       TransactionCallback.java
│       TransactionManager.java
│       
└───utils
        Connections.java
        Queries.java
        ResultSetMapper.java
```

Реализовано 3 таблицы:

- users:
  - логин
  - телефонный номер (для отправки СМС)
  - пароль в хэшированном виде
  - роль (USER, ADMIN)
- otp_codes:
  - идентификатор пользователя
  - идентификатор операции
  - одноразовый код
  - статус (ACTIVE, EXPIRED, USED)
- otp_config:
  - длина кода (по умолчанию 6)
  - время жизни кода в секундах (по умолчанию 300)

Пользователи (хранит логин пользователя, пароль пользователя в хешированном виде, а также его роль).
Конфигурация OTP-кода (количество записей в ней никогда не должно превышать 1).
Таблица OTP-кодов (может содержать идентификатор операции в привязке к OTP-коду, но также
допускается вынести логику работы с операциями в отдельную таблицу).

---

Реализовано логирование с помощью библиотек

```xml
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>
```

Настройки приведены в файле [logback.xml](src/main/resources/logback.xml)

---

Реализована проверка аутентификация пользователей по ролям в декларативном стиле:

```java
    @RequiresRole(Role.ADMIN)
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return adminService.getUsers();
    }

    @RequiresRole(Role.USER)
    @PostMapping("/otp/generate")
    public OtpCodeDto generateOtp(@RequestBody OtpRequest request) {
        return userService.generateOtp(request.userId(), request.operationId());
    }
```

## Архитектурные требования

Приложение имеет три основных слоя:

1. Слой controller (API), содержащий обработчики HTTP-запросов. Выполнен с помощью Spring MVC.
   ```text
    ├───controller
    │   │   AdminController.java
    │   │   AuthController.java
    │   │   GlobalExceptionHandler.java
    │   │   UserController.java
    │   │   
    │   ├───auth
    │   │   ├───annotation
    │   │   │       RequiresRole.java
    │   │   │       RoleInterceptor.java
    │   │   │       
    │   │   ├───context
    │   │   │       SecurityContext.java
    │   │   │       
    │   │   └───filter
    │   │           JwtFilter.java
    │   │           
    │   ├───config
    │   │       WebConfig.java
    │   │       
    │   └───dto
    │           ErrorResponse.java
    │           LoginRequest.java
    │           LoginResponse.java
    │           OtpRequest.java
    │           UpdateOtpConfigRequest.java
    │           UserRequest.java
    │           ValidateOtpRequest.java
   ```
2. Слой сервисов, содержащий в себе основную бизнес-логику приложения.
   ```text
    ├───service
    │   │   AdminService.java
    │   │   AuthService.java
    │   │   JwtService.java
    │   │   UserService.java
    │   │   
    │   ├───config
    │   │       ServiceConfig.java
    │   │       
    │   ├───dto
    │   │       OtpCodeDto.java
    │   │       UserDto.java
    │   │       
    │   ├───impl
    │   │       DefaultAdminService.java
    │   │       DefaultAuthService.java
    │   │       DefaultJwtService.java
    │   │       DefaultUserService.java
    │   │       
    │   └───internal
    │           DefaultExpirationService.java
    │           ExpirationService.java`
   ```
3. Слой repository (DAO), содержащий в себе классы, осуществляющие выполнение запросов к БД.
   ```text
    ├───repository
    │   │   OtpConfigRepository.java
    │   │   OtpRepository.java
    │   │   UserRepository.java
    │   │   
    │   ├───config
    │   │       RepositoryConfig.java
    │   │       
    │   ├───datasource
    │   │   │   DataSource.java
    │   │   │   
    │   │   └───impl
    │   │           PooledDataSource.java
    │   │           
    │   ├───impl
    │   │       JdbcOtpConfigRepository.java
    │   │       JdbcOtpRepository.java
    │   │       JdbcUserRepository.java
    │   │       
    │   ├───transaction
    │   │       PerThreadConnectionHolder.java
    │   │       Transactional.java
    │   │       TransactionalProxy.java
    │   │       TransactionCallback.java
    │   │       TransactionManager.java
    │   │       
    │   └───utils
    │           Connections.java
    │           Queries.java
    │           ResultSetMapper.java
   ```