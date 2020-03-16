package com.onix.featureflags.books;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/books")
@RestController
@RequiredArgsConstructor
public final class BooksController {

    private final BooksService booksService;

    @GetMapping
    public String books() {
        return this.booksService.getBooksResult();
    }

}
