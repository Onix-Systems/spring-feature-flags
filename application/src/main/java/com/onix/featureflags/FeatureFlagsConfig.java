package com.onix.featureflags;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FeatureFlags.class)
public class FeatureFlagsConfig {
}
