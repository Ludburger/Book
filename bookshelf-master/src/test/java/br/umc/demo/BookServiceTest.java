package br.umc.demo.service;

import br.umc.demo.dto.BookRequest;
import br.umc.demo.dto.BookResponse;
import br.umc.demo.entity.Author;
import br.umc.demo.entity.Book;
import br.umc.demo.repository.AuthorRepository;
import br.umc.demo.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private Author autor;
    private Book livro;

    @BeforeEach
    void setUp() {
        autor = new Author();
        autor.setId(1L);
        autor.setName("Robert C. Martin");

        livro = new Book();
        livro.setId(1L);
        livro.setTitle("Clean Code");
        livro.setPublisher("Prentice Hall");
        livro.setIsbn("978-0132350884");
        livro.setSummary("Um guia para escrever código limpo");
        livro.setAuthors(List.of(autor));
    }

    // -------------------------------------------------------
    // listBooks
    // -------------------------------------------------------

    @Test
    void listBooks_deveRetornarListaVaziaDeLivros() {
        // setup
        when(bookRepository.findAll()).thenReturn(List.of());

        // exercise
        List<BookResponse> resultado = bookService.listBooks();

        // verify
        assertThat(resultado).isEmpty();
        verify(bookRepository).findAll();
    }

    @Test
    void listBooks_deveRetornarListaComMultiplosLivros() {
        // setup
        Book livro2 = new Book();
        livro2.setId(2L);
        livro2.setTitle("Clean Architecture");
        livro2.setPublisher("Prentice Hall");
        livro2.setIsbn("978-0134494166");
        livro2.setSummary("Uma arquitetura clara e prévia para software");
        livro2.setAuthors(List.of(autor));

        when(bookRepository.findAll()).thenReturn(List.of(livro, livro2));

        // exercise
        List<BookResponse> resultado = bookService.listBooks();

        // verify
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getTitle()).isEqualTo("Clean Code");
        assertThat(resultado.get(1).getTitle()).isEqualTo("Clean Architecture");
        verify(bookRepository).findAll();
    }

    // -------------------------------------------------------
    // getBook
    // -------------------------------------------------------

    @Test
    void getBook_deveRetornarLivroComDadosCorretos() {
        // setup
        when(bookRepository.findById(1L)).thenReturn(Optional.of(livro));

        // exercise
        BookResponse resultado = bookService.getBook(1L);

        // verify
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitle()).isEqualTo("Clean Code");
        assertThat(resultado.getAuthors()).hasSize(1);
        assertThat(resultado.getPublisher()).isEqualTo("Prentice Hall");
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBook_comIdInvalido_deveLancarNotFound() {
        // setup
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // exercise & verify
        assertThatThrownBy(() -> bookService.getBook(999L))
                .isInstanceOf(ResponseStatusException.class);

        verify(bookRepository).findById(999L);
    }

    // -------------------------------------------------------
    // createBook
    // -------------------------------------------------------

    @Test
    void createBook_deveSalvarERetornarComDadosCorretos() {
        // setup
        BookRequest request = buildRequest("Domain-Driven Design", List.of(1L));
        when(authorRepository.findAllById(List.of(1L))).thenReturn(List.of(autor));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(2L);
            return b;
        });

        // exercise
        BookResponse resultado = bookService.createBook(request);

        // verify
        assertThat(resultado.getTitle()).isEqualTo("Domain-Driven Design");
        assertThat(resultado.getAuthors()).hasSize(1);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_semAuthorIds_naoDeveChamarFindAllById() {
        // setup
        BookRequest request = buildRequest("Livro Sem Autor", null);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(3L);
            return b;
        });

        // exercise
        bookService.createBook(request);

        // verify
        verify(authorRepository, never()).findAllById(any());
    }

    // -------------------------------------------------------
    // updateBook
    // -------------------------------------------------------

    @Test
    void updateBook_deveAtualizarERetornarComDadosAtualizados() {
        // setup
        BookRequest request = buildRequest("Clean Code - Updated Edition", List.of(1L));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(authorRepository.findAllById(List.of(1L))).thenReturn(List.of(autor));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            return b;
        });

        // exercise
        BookResponse resultado = bookService.updateBook(1L, request);

        // verify
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitle()).isEqualTo("Clean Code - Updated Edition");
        assertThat(resultado.getPublisher()).isEqualTo("Editora");
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_comIdInvalido_deveLancarNotFound() {
        // setup
        BookRequest request = buildRequest("Clean Code", List.of(1L));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // exercise & verify
        assertThatThrownBy(() -> bookService.updateBook(999L, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(bookRepository).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    // -------------------------------------------------------
    // deleteBook
    // -------------------------------------------------------

    @Test
    void deleteBook_deveApagarLivroExistente() {
        // setup
        when(bookRepository.existsById(1L)).thenReturn(true);

        // exercise
        bookService.deleteBook(1L);

        // verify
        verify(bookRepository).existsById(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_comIdInvalido_deveLancarNotFound() {
        // setup
        when(bookRepository.existsById(999L)).thenReturn(false);

        // exercise & verify
        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(ResponseStatusException.class);

        verify(bookRepository).existsById(999L);
        verify(bookRepository, never()).deleteById(any());
    }


    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private BookRequest buildRequest(String title, List<Long> authorIds) {
        BookRequest r = new BookRequest();
        r.setTitle(title);
        r.setAuthorIds(authorIds);
        r.setPublisher("Editora");
        r.setIsbn("000-000");
        r.setSummary("Resumo");
        return r;
    }
}