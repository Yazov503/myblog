package com.liu.myblog.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "custom.parameters")
public class AppConfig {

    private String defaultAvatar;

    private String defaultUsername;

}
