package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.BookRequest;
import com.axel.libraryapi.dto.BookResponse;
import com.axel.libraryapi.dto.PageResponse;
import com.axel.libraryapi.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Libros", description = "Creación, actualización, borrado y búsqueda de libros")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Crear un libro")
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @Operation(summary = "Listar libros, con filtro opcional por autor o categoría")
    @GetMapping
    public PageResponse<BookResponse> findAll(@RequestParam(required = false) String author,
                                              @RequestParam(required = false) String category,
                                              @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return bookService.findAll(author, category, pageable);
    }

    @Operation(summary = "Buscar un libro por ID")
    @GetMapping("/{id}")
    public BookResponse findById(@PathVariable Long id){
        return bookService.findById(id);
    }

    @Operation(summary = "Actualizar un libro")
    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request){
        return bookService.update(id, request);
    }

    @Operation(summary = "Borrar un libro",
    description = "Falla con 422 si el libro tiene historial de préstamos")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        bookService.delete(id);
    }
}
