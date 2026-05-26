package com.joxelito.adivinaquien.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @Min(1)
    private int matchTimeoutSeconds = 60;

    @Min(1)
    private int reconnectTimeoutSeconds = 45;

    @Min(1)
    private int questionResponseTimeoutSeconds = 15;

    @Min(50)
    private int dummyActionDelayMillis = 600;

}

