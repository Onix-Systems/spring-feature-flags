package com.onix.featureflags;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlags {

	private Boolean isNewBooksServiceEnabled = false;
	private Boolean isNewPapersServiceEnabled = false;

}
