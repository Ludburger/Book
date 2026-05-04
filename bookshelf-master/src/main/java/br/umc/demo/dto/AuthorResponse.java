package br.umc.demo.dto;

public class AuthorResponse {
    private Long id;
    private String name;

    public AuthorResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
