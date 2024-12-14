package com.example.server.controller;

import com.example.server.model.Book;
import com.example.server.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/books")
    public List<Book> getBooks() {
        return bookRepository.findAll();

    }
    @GetMapping("/recommendations")
public List<Book> getRecommendations(@RequestParam List<String> genres) {
    return bookRepository.findTop10ByGenreInOrderByRatingDesc(genres);
}

    
}
