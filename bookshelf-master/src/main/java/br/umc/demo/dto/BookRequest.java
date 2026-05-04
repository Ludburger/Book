package br.umc.demo.dto;

import java.util.List;

public class BookRequest {
    private String title;
    private List<Long> authorIds;
    private String publisher;
    private String isbn;
    private String summary;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Long> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<Long> authorIds) { this.authorIds = authorIds; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
