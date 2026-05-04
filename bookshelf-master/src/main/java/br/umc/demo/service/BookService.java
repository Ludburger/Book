package br.umc.demo.service;

import br.umc.demo.dto.AuthorResponse;
import br.umc.demo.dto.BookRequest;
import br.umc.demo.dto.BookResponse;
import br.umc.demo.entity.Author;
import br.umc.demo.entity.Book;
import br.umc.demo.repository.AuthorRepository;
import br.umc.demo.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    public List<BookResponse> listBooks() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BookResponse createBook(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        return toResponse(bookRepository.save(book));
    }

    public BookResponse getBook(Long id) {
        return toResponse(findById(id));
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findById(id);
        applyRequest(book, request);
        return toResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        }
        bookRepository.deleteById(id);
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.getTitle());
        book.setPublisher(request.getPublisher());
        book.setIsbn(request.getIsbn());
        book.setSummary(request.getSummary());

        if (request.getAuthorIds() != null) {
            List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
            book.setAuthors(authors);
        }
    }

    private Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
    }

    private BookResponse toResponse(Book book) {
        List<AuthorResponse> authors = book.getAuthors().stream()
                .map(a -> new AuthorResponse(a.getId(), a.getName()))
                .toList();
        return new BookResponse(book.getId(), book.getTitle(), authors,
                book.getPublisher(), book.getIsbn(), book.getSummary());
    }
}
