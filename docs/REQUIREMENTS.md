# Description

Мы планируем разработать простое и удобное backend-приложение, которое поможет защитить операции с
помощью временных кодов. Основная цель сервиса — обеспечить безопасность при выполнении различных
действий, требующих подтверждения.

Что будет делать сервис?

Создавать операции на защиту — пользователи смогут инициировать защиту своих операций, чтобы
добавить дополнительный уровень безопасности.

Генерировать защитный код — для каждой операции будет автоматически создаваться уникальный код,
который будет использоваться для подтверждения.

Отправлять код — пользователи смогут получать защитные коды через разные каналы:

SMS — вам предстоит использовать эмулятор для отправки SMS, чтобы протестировать функционал.
Email — коды можно будет отправлять как на эмулятор, так и на реальные почтовые адреса.
Telegram — с помощью Telegram API вы создадите бота, который будет отправлять коды пользователям.
Проверять код — при выполнении операции пользователи смогут вводить полученный код, который будет
проверяться на правильность.

Настраивать время жизни и длину кода — каждый код будет иметь ограниченное время действия.
Администратор сможет настраивать, как долго код будет действовать и сколько цифр он будет содержать.

Вдохновение для проекта можно взять из концепции одноразовых кодов подтверждения, что поможет
сделать защиту более надежной.

Таким образом, вы создадите удобный сервис, который поможет пользователям безопасно выполнять
операции, используя временные коды для подтверждения.

# Database Layer Requirements

База данных должна быть реализована с помощью PostgreSQL 17, взаимодействие с базой данных должно
быть реализовано через JDBC.

Должно быть реализовано минимум 3 таблицы:

- Пользователи (хранит логин пользователя, пароль пользователя в хешированном виде, а также его
  роль).
- Конфигурация OTP-кода (количество записей в ней никогда не должно превышать 1).
- Таблица OTP-кодов (может содержать идентификатор операции в привязке к OTP-коду, но также
  допускается вынести логику работы с операциями в отдельную таблицу).

OTP-код должен иметь минимум три статуса:

- ACTIVE (код активен);
- EXPIRED (код просрочен);
- USED (код прошел валидацию и был использован).

# API Layer Requirements

Для регистрации и аутентификации пользователей необходимо реализовать соответствующее API, которое
должно минимально выполнять следующие операции:

- Регистрация нового пользователя.
  У пользователей может быть две роли:
    - администратор
    - пользователь.
      Если администратор уже существует, то регистрация второго администратора должна быть
      невозможной.
- Логин зарегистрированного пользователя.
  Данная операция должна возвращать токен с ограниченным сроком действия (JWT) для осуществления
  аутентификации и авторизации пользователя.

У администратора должно быть свое отдельное API, которое позволяет как минимум:

- Менять конфигурацию OTP-кодов (время жизни и количество знаков в коде).
- Получать список всех пользователей кроме администраторов.
- Удалять пользователей и привязанные к ним OTP-коды.

API пользователя минимально должно реализовывать следующие функции:

- Генерация OTP-кода привязанного к операции либо к ее идентификатору и рассылка его тремя способами
  либо сохранение сгенерированного кода в файл в корне проекта
- Валидация OTP-кода, который был выслан пользователю по одному из каналов

**Пользователи, не являющиеся администраторами, не должны иметь доступа к API администратора.**

# Integration Layer Requirements

Пользователи смогут получать защитные коды через различные каналы, что обеспечит гибкость и удобство
в использовании сервиса. Для реализации этой функциональности необходимо учесть следующие
требования:

- Отправка кода по SMS — вам предстоит использовать эмулятор для отправки SMS, чтобы протестировать
  функционал. Это позволит имитировать процесс получения кодов без необходимости использования
  реальных SMS.
- Отправка кода по Email — коды можно будет отправлять как на эмулятор, так и на реальные почтовые
  адреса. Это обеспечит пользователям возможность получать коды на удобный для них почтовый ящик.
- Отправка кода через Telegram — с помощью Telegram API вы создадите бота, который будет отправлять
  коды пользователям. Это позволит мгновенно доставлять коды через популярное приложение для обмена
  сообщениями.
- Сохранение кода в файл — реализуйте возможность сохранения сгенерированных кодов в файл.

# Structural Requirements

Приложение должно иметь три основных слоя:

1. Слой API, содержащий обработчики HTTP-запросов. Слой API (хэндлеров или контроллеров) должен быть
   выполнен с помощью пакета com.sun.net.httpserver. Также допускается использование Spring MVC.
2. Слой сервисов, содержащий в себе основную бизнес-логику приложения.
3. Слой DAO, содержащий в себе классы, осуществляющие выполнение запросов к БД.

# Functional Requirements

- Необходимо реализовать механизм, который будет отмечать просроченные OTP-коды раз в определенный
  интервал времени и присваивать им статус EXPIRED.
- Необходимо настроить логирование в приложении с помощью любой понравившейся вам библиотеки из
  модуля про логирование.
- Приложение должно использовать систему сборки Maven.

---

# Email Integration Requirements

В этой инструкции описывается процесс интеграции Java приложения с почтовым сервисом с
использованием библиотеки для работы с почтой по SMTP.

Для работы с почтой добавьте зависимость в pom.xml. В примере ниже используется реализация Jakarta
Mail — Angus Mail:

```xml

<dependency>
    <groupId>org.eclipse.angus</groupId>
    <artifactId>angus-mail</artifactId>
    <version>2.0.5</version>
</dependency>
```

Создайте файл email.properties в папке src/main/resources вашего проекта. Этот файл будет содержать
параметры конфигурации для подключения к почтовому сервису. Пример содержимого файла:

```text
email.username=your_email@example.com
email.password=your_email_password
email.from=your_email@example.com
mail.smtp.host=smtp.example.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
```

Для загрузки пропертей используйте следующий метод:

```java
private Properties loadConfig() {
    try {
        Properties props = new Properties();
        props.load(EmailNotificationService.class.getClassLoader()
            .getResourceAsStream("email.properties"));
        return props;
    } catch (Exception e) {
        throw new RuntimeException("Failed to load email configuration", e);
    }
}
```

Далее создайте конструктор вашего почтового сервиса:

```java
public EmailNotificationService() {
// Загрузка конфигурации
    Properties config = loadConfig();
    this.username = config.getProperty("email.username");
    this.password = config.getProperty("email.password");
    this.fromEmail = config.getProperty("email.from");
    this.session = Session.getInstance(config, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    });
}
```

Теперь осталось только реализовать метод отправки письма с кодом подтверждения на электронную почту:

```java
public void sendCode(String toEmail, String code) {
    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject("Your OTP Code");
        message.setText("Your verification code is: " + code);

        Transport.send(message);
    } catch (MessagingException e) {
        throw new RuntimeException("Failed to send email", e);
    }
}
```

# SMPP Emulator Integration Requirements

Для начала необходимо скачать эмулятор SMPP-протокола, например SMPPsim, или другой совместимый
эмулятор. Распакуйте его и запустите в командной строке. Если вы используете SMPPsim, можно
выполнить файл startsmppsim.bat.

Далее необходимо подключить библиотеку для работы с протоколом SMPP в Java:

```xml

<dependency>
    <groupId>org.jsmpp</groupId>
    <artifactId>jsmpp</artifactId>
    <version>3.0.1</version>
</dependency>
```

Создаем файл sms.properties c следующим наполнением:

```text
smpp.host=localhost
smpp.port=2775
smpp.system_id=smppclient1
smpp.password=password
smpp.system_type=OTP
smpp.source_addr=OTPService
```

Параметры smpp.system_id и smpp.password можно взять в файле config/smppsim.props установленного
SMPPsim.

Далее по аналогии с рассылкой по email загружаем проперти в наш сервис.

Теперь необходимо написать метод, который будет отправлять СМС в эмулятор по протоколу SMPP:

```java
import java.nio.charset.StandardCharsets;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

public void sendCode(String destination, String code) {
    SMPPSession session = new SMPPSession();

    try {
        BindParameter bindParameter = new BindParameter(
            BindType.BIND_TX,
            systemId,
            password,
            systemType,
            TypeOfNumber.UNKNOWN,
            NumberingPlanIndicator.UNKNOWN,
            sourceAddress
        );

        session.connectAndBind(host, port, bindParameter);

        session.submitShortMessage(
            systemType,
            TypeOfNumber.UNKNOWN,
            NumberingPlanIndicator.UNKNOWN,
            sourceAddress,
            TypeOfNumber.UNKNOWN,
            NumberingPlanIndicator.UNKNOWN,
            destination,
            new ESMClass(),
            (byte) 0,
            (byte) 1,
            null,
            null,
            new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
            (byte) 0,
            new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
            (byte) 0,
            ("Your code: " + code).getBytes(StandardCharsets.UTF_8)
        );

        logSuccess();
    } catch (Exception e) {
        handleError(e.getMessage(), e);
    } finally {
        session.unbindAndClose();
    }
}
```

# Telegram Integration Requirements

Для начала необходимо создать Telegram-бота через @BotFather и получить его токен.

Затем необходимо подключить HTTP-клиент для Java, чтобы выполнять запросы к Telegram API.

Теперь необходимо начать диалог с ботом в Telegram, после чего следует выполнить запрос к Telegram
API, чтобы получить значение chatId. Это id вашего диалога с ботом.

https://api.telegram.org/botYOUR_BOT_TOKEN/getUpdates

В результате получим примерно следующее:

```json
{
  "ok": true,
  "result": [
    {
      "update_id": 123456789,
      "message": {
        "message_id": 1,
        "from": {
          "id": 987654321,
          "is_bot": false,
          "first_name": "YourName",
          "username": "YourUsername",
          "language_code": "en"
        },
        "chat": {
          "id": -123456789,
          "first_name": "YourName",
          "username": "YourUsername",
          "type": "private"
        },
        "date": 1610000000,
        "text": "Hello, bot!"
      }
    }
  ]
}
```

После получения нужного chat.id и токена бота можем приступать к реализации отправки сообщения с
кодом через него:

```java
public void sendCode(String destination, String code) {
    // 1. Формируем текст сообщения с кодом подтверждения
    String message = String.format("%s, your confirmation code is: %s", destination, code);

    // 2. Собираем URL для запроса к Telegram Bot API
    String url = String.format("%s?chat_id=%s&text=%s",
        telegramApiUrl,
        chatId,
        urlEncode(message));

    // 3. Передаём готовый URL в метод отправки запроса
    sendTelegramRequest(url);
}

private void sendTelegramRequest(String url) {
    // 1. Создаём HTTP-клиент из стандартной библиотеки Java
    HttpClient httpClient = HttpClient.newHttpClient();

    // 2. Создаём GET-запрос к Telegram API
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build();

    try {
        // 3. Отправляем запрос и получаем ответ в виде строки
        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        // 4. Проверяем HTTP-статус ответа
        int statusCode = response.statusCode();
        if (statusCode != 200) {
            logger.error("Telegram API error. Status code: {}", statusCode);
        } else {
            logger.info("Telegram message sent successfully");
        }
    } catch (InterruptedException e) {
        // 5. Если поток был прерван, логируем ошибку и восстанавливаем флаг прерывания
        logger.error("Error sending Telegram message: {}", e.getMessage(), e);
        Thread.currentThread().interrupt();
    } catch (IOException e) {
        // 6. Если произошла ошибка ввода-вывода, логируем её
        logger.error("Error sending Telegram message: {}", e.getMessage(), e);
    }
}

private static String urlEncode(String value) {
    // Кодируем текст сообщения, чтобы его можно было безопасно передать в URL
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
}
```

# README File Requirements

Добавьте файл README.md, в котором опишите:

- Как пользоваться сервисом.
- Какие API-эндпоинты и сценарии использования поддерживаются.
- Как протестировать ваш код.

# Evaluation Criteria

- Структура приложения соответствует требованиям — 5 баллов;
- Используется система сборки Maven или Gradle — 5 баллов;
- Реализован минимальный функционал основных операций приложения без аутентификации с помощью токена
  и авторизации — 9 баллов;
- Запросы к приложению имеют разграничение по ролям администратора и обычного пользователя — 5
  баллов;
- Для разработки API был использован пакет com.sun.net.httpserver или Spring MVC — 5 баллов;
- Было реализовано минимальное покрытие логами каждого запроса к API — 3 балла.
- Реализован механизм рассылки OTP-кодов по почте — 3 балла.
- Реализован механизм рассылки OTP-кодов через эмулятор SMPP — 3 балла.
- Реализован механизм рассылки OTP-кодов через Telegram — 3 балла.
- Реализован механизм сохранения OTP-кодов в файл — 3 балла.
- Реализован механизм аутентификации с помощью токена и авторизации — 3 балла.
- Реализовано подробное покрытие всех запросов к API логами — 3 балла.