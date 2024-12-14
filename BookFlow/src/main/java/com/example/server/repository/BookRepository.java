package com.example.server.repository;

import java.util.List;
import com.example.server.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findTop10ByGenreInOrderByRatingDesc(List<String> genres);

}
