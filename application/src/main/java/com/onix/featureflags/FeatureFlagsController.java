package com.onix.featureflags;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/feature-flags")
@RestController
@RequiredArgsConstructor
public final class FeatureFlagsController {

    private final FeatureFlags featureFlags;

    @GetMapping
    public FeatureFlags featureFlags() {
        return this.featureFlags;
    }

}
