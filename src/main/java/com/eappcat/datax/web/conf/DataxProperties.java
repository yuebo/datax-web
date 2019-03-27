package com.eappcat.datax.web.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datax")
@Data
public class DataxProperties {
    private String home;
}
