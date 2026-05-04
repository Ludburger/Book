package br.umc.demo.dto;

import java.util.List;

public class BookResponse {
    private Long id;
    private String title;
    private List<AuthorResponse> authors;
    private String publisher;
    private String isbn;
    private String summary;

    public BookResponse(Long id, String title, List<AuthorResponse> authors,
                        String publisher, String isbn, String summary) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.isbn = isbn;
        this.summary = summary;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public List<AuthorResponse> getAuthors() { return authors; }
    public String getPublisher() { return publisher; }
    public String getIsbn() { return isbn; }
    public String getSummary() { return summary; }
}
