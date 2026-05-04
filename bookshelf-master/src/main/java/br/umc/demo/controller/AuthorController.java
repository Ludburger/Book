package br.umc.demo.controller;

import br.umc.demo.dto.AuthorRequest;
import br.umc.demo.dto.AuthorResponse;
import br.umc.demo.dto.BookResponse;
import br.umc.demo.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AuthorResponse> listAuthors() {
        return authorService.listAuthors();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AuthorResponse getAuthor(@PathVariable Long id) {
        return authorService.getAuthor(id);
    }

    @GetMapping("/{id}/books")
    @PreAuthorize("isAuthenticated()")
    public List<BookResponse> listBooksByAuthor(@PathVariable Long id) {
        return authorService.listBooksByAuthor(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AuthorResponse createAuthor(@RequestBody AuthorRequest request) {
        return authorService.createAuthor(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthorResponse updateAuthor(@PathVariable Long id, @RequestBody AuthorRequest request) {
        return authorService.updateAuthor(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
    }
}
