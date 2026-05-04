package br.umc.demo.service;

import br.umc.demo.dto.AuthorRequest;
import br.umc.demo.dto.AuthorResponse;
import br.umc.demo.dto.BookResponse;
import br.umc.demo.entity.Author;
import br.umc.demo.repository.AuthorRepository;
import br.umc.demo.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public AuthorResponse createAuthor(AuthorRequest request) {
        Author author = new Author(request.getName());
        author = authorRepository.save(author);
        return toResponse(author);
    }

    public List<AuthorResponse> listAuthors() {
        return authorRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AuthorResponse getAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado"));
        return toResponse(author);
    }

    public AuthorResponse updateAuthor(Long id, AuthorRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado"));
        author.setName(request.getName());
        return toResponse(authorRepository.save(author));
    }

    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado");
        }
        authorRepository.deleteById(id);
    }

    public List<BookResponse> listBooksByAuthor(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado");
        }
        return bookRepository.findByAuthors_Id(authorId).stream()
                .map(book -> new BookResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthors().stream().map(this::toResponse).toList(),
                        book.getPublisher(),
                        book.getIsbn(),
                        book.getSummary()
                ))
                .toList();
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
