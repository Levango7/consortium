package org.wisdom.consortium;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@ConfigurationProperties(prefix = "consortium")
@Component
@Getter
@Setter
@NoArgsConstructor
public class ConsensusProperties {
    static final String CONSENSUS_NAME = "name";
    private Properties consensus;
}

