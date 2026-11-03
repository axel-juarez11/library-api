package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.BookRequest;
import com.axel.libraryapi.dto.BookResponse;
import com.axel.libraryapi.entity.Book;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.DuplicateResourceException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.BookRepository;
import com.axel.libraryapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private LoanRepository loanRepository;

    @InjectMocks private BookService bookService;

    private Book book;

    @BeforeEach
    void setUp(){
        book = Book.builder()
                .id(1L)
                .title("El señor de los anillos")
                .isbn("9781234567891")
                .author("J.R.R. Tolkien")
                .availableCopies(3)
                .totalCopies(3)
                .category("Fantasía")
                .build();
    }

    @Test
    void create_success_availableEqualsTotal(){
        when(bookRepository.existsByIsbn("9877894561232")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse response = bookService.create(new BookRequest("Pedro Páramo", "Juan Rulfo", "9877894561232", "Realismo Mágico", 2));

        assertThat(response.availableCopies()).isEqualTo(2);
        assertThat(response.totalCopies()).isEqualTo(2);
        assertThat(response.isbn()).isEqualTo("9877894561232");
        assertThat(response.title()).isEqualTo("Pedro Páramo");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void create_duplicateIsbn_throwsDuplicate(){
        when(bookRepository.existsByIsbn("9781234567891")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(new BookRequest("Pedro Páramo", "Juan Rulfo", "9781234567891", "Realismo Mágico", 2)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("9781234567891");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_duplicateIsbn_throwsDuplicate(){
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("9877894561232", 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.update(1L, new BookRequest("El señor de los anillos", "J.R.R. Tolkien", "9877894561232", "Fantasía", 3)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("9877894561232");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_totalCopiesBelowLoanedCopies_throwsIllegalArgument(){
        book.setTotalCopies(5);
        book.setAvailableCopies(2);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("9781234567891",1L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.update(1L, new BookRequest("El señor de los anillos", "J.R.R. Tolkien", "9781234567891", "Fantasía", 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("copias totales");

        verify(bookRepository, never()).save(any());
        assertThat(book.getAvailableCopies()).isEqualTo(2);
        assertThat(book.getTotalCopies()).isEqualTo(5);
    }

    @Test
    void update_success_recalculatesAvailableCopies(){
        book.setTotalCopies(5);
        book.setAvailableCopies(2);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("9781234567891", 1L)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse response = bookService.update(1L, new BookRequest("El señor de los anillos", "J.R.R. Tolkien", "9781234567891", "Fantasía", 8));

        assertThat(response.availableCopies()).isEqualTo(5);
        assertThat(response.totalCopies()).isEqualTo(8);
        assertThat(response.title()).isEqualTo("El señor de los anillos");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void delete_bookWithLoanHistory_throwsBusinessRule(){
        when(loanRepository.existsByBookId(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("préstamos");

        verify(bookRepository, never()).delete(any());
    }

    @Test
    void delete_success_callsDelete(){
        when(loanRepository.existsByBookId(1L)).thenReturn(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.delete(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void delete_notFound_throwsNotFound(){
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(bookRepository, never()).delete(any());
    }
    
    @Test
    void findAll_withAuthor_usesAuthorFilter(){
        when(bookRepository.findByAuthorContainingIgnoreCase("tolkien")).thenReturn(List.of(book));

        List<BookResponse> result = bookService.findAll("tolkien", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).author()).isEqualTo("J.R.R. Tolkien");
        verify(bookRepository).findByAuthorContainingIgnoreCase("tolkien");
        verify(bookRepository, never()).findAll();
    }

    @Test
    void findAll_withCategory_usesCategoryFilter(){
        when(bookRepository.findByCategoryIgnoreCase("fantasía")).thenReturn(List.of(book));

        List<BookResponse> result = bookService.findAll(null, "fantasía");

        assertThat(result).hasSize(1);
        verify(bookRepository).findByCategoryIgnoreCase("fantasía");
        verify(bookRepository, never()).findAll();
    }

    @Test
    void findAll_withoutFilters_returnsAll(){
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookResponse> result = bookService.findAll(null, null);

        assertThat(result).hasSize(1);
        verify(bookRepository).findAll();
    }
}
