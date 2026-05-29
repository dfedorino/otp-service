package com.dfedorino.otp.delivery.config;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.delivery.impl.EmailDeliveryChannel;
import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryConfig {
    private final Properties props = ApplicationPropertiesUtil.loadApplicationProperties();

    @Bean
    public DeliveryChannel emailDeliveryChannel() {
        return new EmailDeliveryChannel(props);
    }
}
