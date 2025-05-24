package com.example;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieRepository repo;

    public MovieController(MovieRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Movie> list() {
        return repo.findAll();
    }

    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        return repo.save(movie);
    }
}
