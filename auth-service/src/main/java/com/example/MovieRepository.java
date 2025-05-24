package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
