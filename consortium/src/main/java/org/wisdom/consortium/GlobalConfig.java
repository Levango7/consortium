package org.wisdom.consortium;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@ConfigurationProperties(prefix = "consortium")
@Component
public class GlobalConfig extends Properties {
}
