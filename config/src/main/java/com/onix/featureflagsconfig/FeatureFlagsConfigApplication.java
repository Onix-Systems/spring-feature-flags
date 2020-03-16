package com.onix.featureflagsconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class FeatureFlagsConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagsConfigApplication.class, args);
    }

}
