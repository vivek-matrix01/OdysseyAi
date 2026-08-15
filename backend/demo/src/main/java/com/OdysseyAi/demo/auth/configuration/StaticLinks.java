package com.OdysseyAi.demo.auth.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "static-links")
public class StaticLinks {
    private String verificationLink;
    private String resetPasswordLink;
}
