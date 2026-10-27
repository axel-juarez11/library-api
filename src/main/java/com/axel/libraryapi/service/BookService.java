package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.BookResponse;
import com.axel.libraryapi.dto.BookRequest;
import com.axel.libraryapi.entity.Book;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.DuplicateResourceException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.BookRepository;
import com.axel.libraryapi.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public BookResponse create(BookRequest request){
        if (bookRepository.existsByIsbn(request.isbn())){
            throw new DuplicateResourceException("Ya existe un libro con el ISBN " + request.isbn());
        }
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .category(request.category())
                .totalCopies(request.totalCopies())
                .availableCopies(request.totalCopies())
                .build();
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll(String author, String category){
        List<Book> books;
        if (author != null && !author.isBlank()) {
            books = bookRepository.findByAuthorContainingIgnoreCase(author);
        } else if (category != null && !category.isBlank()) {
            books = bookRepository.findByCategoryIgnoreCase(category);
        } else {
            books = bookRepository.findAll();
        }
        return books.stream().map(BookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookResponse.from(getBook(id));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBook(id);

        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new DuplicateResourceException("Ya existe un libro con el ISBN " + request.isbn());
        }

        int onLoan = book.getTotalCopies() - book.getAvailableCopies();
        if (request.totalCopies() < onLoan){
            throw new IllegalArgumentException(
                    "No puedes tener menos copias totales(" + request.totalCopies() + ") que las que están prestadas (" + onLoan + ")");
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setCategory(request.category());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies() - onLoan);

        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id){
        if (loanRepository.existsByBookId(id)){
            throw new BusinessRuleException("No se puede eliminar un libro con historial de préstamos");
        }
        bookRepository.delete(getBook(id));
    }

    private Book getBook(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con el id " + id));
    }
}