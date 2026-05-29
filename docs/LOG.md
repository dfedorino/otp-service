# Development

## Completed
- Project structure and Maven setup
- Domain models (User, OtpCode, OtpConfig)
- Enums (Role, OtpStatus)
- Custom exceptions (Connection, Query, Transaction)
- Repository infrastructure (DataSource, TransactionManager, utilities)
- UserRepository with integration tests
- OtpRepository with integration tests
- OtpConfigRepository (JdbcOtpConfigRepository + tests)
- JwtService (token generation + validation)
- Role-based request authorization (ADMIN/USER guards)
- PasswordUtil (BCrypt wrapper)
- AuthService (register, login → JWT)
- AdminService (list users, delete user, update OTP config)
- UserService (generate, deliver, validate, store OTP in a file)
- OTP Expiration Service (mark codes as expired once per interval)
- SpringMVC AuthController (register, login → JWT)
- SpringMVC AdminController (list users, delete user, update OTP config)
- SpringMVC UserController (generate OTP, validate OTP)
- EmailNotificationService (deliver via email)
- SmsNotificationService (deliver via sms)
- TelegramNotificationService (deliver via telegram bot)


## Planned
- README (endpoints, usage, test instructions)