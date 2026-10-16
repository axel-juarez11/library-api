package com.axel.libraryapi.repository;

import com.axel.libraryapi.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>{

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByCategoryIgnoreCase(String category);
}
