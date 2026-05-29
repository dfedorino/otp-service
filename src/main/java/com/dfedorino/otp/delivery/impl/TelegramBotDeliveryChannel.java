package com.dfedorino.otp.delivery.impl;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramBotDeliveryChannel implements DeliveryChannel {
    private final String telegramBaseUrl;
    public static final String BOT_SEND_MESSAGE_PATH = "/" + System.getProperty("BOT_TOKEN", System.getenv("BOT_TOKEN")) + "/sendMessage";
    public static final String TELEGRAM_CHAT_ID = System.getProperty("BOT_CHAT_ID", System.getenv("BOT_CHAT_ID"));

    public TelegramBotDeliveryChannel(String telegramBaseUrl) {
        this.telegramBaseUrl = telegramBaseUrl;
    }

    @Override
    public void deliver(UserDto user, OtpCodeDto otp) {
        // 1. Формируем текст сообщения с кодом подтверждения
        String message = String.format("Your confirmation code is: %s", otp.code());

        // 2. Собираем URL для запроса к Telegram Bot API
        String url = String.format("%s?chat_id=%s&text=%s",
            telegramBaseUrl + BOT_SEND_MESSAGE_PATH,
            TELEGRAM_CHAT_ID,
            urlEncode(message));

        // 3. Передаём готовый URL в метод отправки запроса
        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {

        // 1. Создаём HTTP-клиент из стандартной библиотеки Java
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            // 2. Создаём GET-запрос к Telegram API
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            // 3. Отправляем запрос и получаем ответ в виде строки
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            // 4. Проверяем HTTP-статус ответа
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                log.error("Telegram API error. Status code: {}", statusCode);
            } else {
                log.info("Telegram message sent successfully");
            }
        } catch (InterruptedException e) {
            // 5. Если поток был прерван, логируем ошибку и восстанавливаем флаг прерывания
            log.error("Error sending Telegram message: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // 6. Если произошла ошибка ввода-вывода, логируем её
            log.error("Error sending Telegram message: {}", e.getMessage(), e);
        }
    }

    private static String urlEncode(String value) {
        // Кодируем текст сообщения, чтобы его можно было безопасно передать в URL
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String name() {
        return "TELEGRAM";
    }
}
