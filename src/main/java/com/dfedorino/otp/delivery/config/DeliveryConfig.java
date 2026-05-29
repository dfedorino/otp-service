package com.dfedorino.otp.delivery.config;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.delivery.impl.EmailDeliveryChannel;
import com.dfedorino.otp.delivery.impl.FileDeliveryChannel;
import com.dfedorino.otp.delivery.impl.SmsDeliveryChannel;
import com.dfedorino.otp.delivery.impl.TelegramBotDeliveryChannel;
import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class DeliveryConfig {
    private final Properties props = ApplicationPropertiesUtil.loadApplicationProperties();

    @Bean
    public DeliveryChannel emailDeliveryChannel() {
        return new EmailDeliveryChannel(props);
    }
    
    @Bean
    public DeliveryChannel smsDeliveryChannel() {
        return new SmsDeliveryChannel(props);
    }

    @Bean
    public DeliveryChannel telegramDeliveryChannel(@Value("${telegram.api.base-url}") String telegramBaseUrl) {
        return new TelegramBotDeliveryChannel(telegramBaseUrl);
    }

    @Bean
    public DeliveryChannel fileDeliveryChannel(@Value("${otp.file.path:otp-codes.txt}") String filePath) {
        return new FileDeliveryChannel(filePath);
    }
}
