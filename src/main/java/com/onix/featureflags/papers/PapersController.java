package com.onix.featureflags.papers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/papers")
@RestController
@RequiredArgsConstructor
public final class PapersController {

    private final PapersService papersService;

    @GetMapping
    public String papers() {
        return this.papersService.getPapersResult();
    }

}
