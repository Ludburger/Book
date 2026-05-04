package br.umc.demo.repository;

import br.umc.demo.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthors_Id(Long authorId);
}
