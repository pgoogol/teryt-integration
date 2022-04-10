package com.pgoogol.teryt.integration.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.elasticsearch.index-config")
public class IndexConfigProperties {
    private String addressIndex;
    private String addressVersionIndex;
    private int shardsNumber;
    private int replicasNumber;
}
