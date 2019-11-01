package org.wisdom.consortium.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "consortium")
@Component
@Getter
@Setter
@NoArgsConstructor
public class ConsortiumConfig {

    @Getter@Setter@NoArgsConstructor
    public static class ConsensusConfig{
        private String name;
        private String genesis;
        private int blockInterval;
        private boolean enableMining;
        private String minerCoinBase;
    }

    @NestedConfigurationProperty
    private ConsensusConfig consensus;
}
