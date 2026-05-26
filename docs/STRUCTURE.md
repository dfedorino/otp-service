otp-service/
├── pom.xml
├── README.md
├── .gitignore
├── .env
└── src/
├── main/
│   ├── java/
│   │   └── com/dfedorino/otp/
│   │       ├── Main.java
│   │       ├── controller/
│   │       │   ├── AuthController.java
│   │       │   ├── AdminController.java
│   │       │   └── UserController.java
│   │       ├── service/
│   │       │   ├── AuthService.java
│   │       │   ├── JwtService.java
│   │       │   ├── AdminService.java
│   │       │   ├── UserService.java
│   │       │   └── notification/
│   │       │       ├── EmailNotificationService.java
│   │       │       ├── SmsNotificationService.java
│   │       │       └── TelegramNotificationService.java
│   │       ├── domain/
│   │       │   ├── model/
│   │       │   │   ├── User.java
│   │       │   │   ├── OtpCode.java
│   │       │   │   └── OtpConfig.java
│   │       │   ├── enums/
│   │       │   │   ├── Role.java
│   │       │   │   └── OtpStatus.java
│   │       │   └── exception/
│   │       │       ├── ConnectionException.java
│   │       │       ├── QueryException.java
│   │       │       └── TransactionException.java
│   │       ├── repository/
│   │       │   ├── UserRepository.java
│   │       │   ├── OtpRepository.java
│   │       │   ├── OtpConfigRepository.java
│   │       │   ├── impl/
│   │       │   │   ├── JdbcUserRepository.java
│   │       │   │   ├── JdbcOtpRepository.java
│   │       │   │   └── JdbcOtpConfigRepository.java
│   │       │   ├── config/
│   │       │   │   └── RepositoryConfig.java
│   │       │   ├── datasource/
│   │       │   │   ├── DataSource.java
│   │       │   │   └── impl/
│   │       │   │       └── PooledDataSource.java
│   │       │   ├── transaction/
│   │       │   │   ├── PerThreadConnectionHolder.java
│   │       │   │   ├── Transactional.java
│   │       │   │   ├── TransactionalProxy.java
│   │       │   │   ├── TransactionCallback.java
│   │       │   │   └── TransactionManager.java
│   │       │   └── utils/
│   │       │       ├── Connections.java
│   │       │       ├── Queries.java
│   │       │       └── ResultSetMapper.java
│   │       └── utils/
│   │           └── PasswordUtil.java
│   └── resources/
│       ├── schema.sql
│       ├── application.properties
│       ├── email.properties
│       ├── sms.properties
│       ├── telegram.properties
│       └── logback.xml