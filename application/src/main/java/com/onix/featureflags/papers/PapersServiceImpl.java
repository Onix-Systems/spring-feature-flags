package com.onix.featureflags.papers;

import com.onix.featureflags.FeatureFlags;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RefreshScope
@RequiredArgsConstructor
public class PapersServiceImpl implements PapersService {

    private final FeatureFlags featureFlags;
    private PapersService papersService;

    @PostConstruct
    private void init() {
        if (this.featureFlags.getIsNewPapersServiceEnabled()) {
            this.papersService = new PapersNewService();
        } else {
            this.papersService = new PapersDefaultService();
        }
    }

    public String getPapersResult() {
        return this.papersService.getPapersResult();
    }

}

