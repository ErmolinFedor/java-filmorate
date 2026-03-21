package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {
  private final GenreService genreService;

  @Autowired
  public GenreController(GenreService genreService) {
    this.genreService = genreService;
  }

  @GetMapping
  public Collection<Genre> findAll() {
    log.info("Получен запрос GET /genres на получение списка жанров");
    return genreService.findAll();
  }

  @GetMapping("/{genreId}")
  public Genre findById(@PathVariable int genreId) throws NotFoundException {
    log.info("Получен запрос GET /genres/genreId на получение жанра с id={}", genreId);
    return genreService.findById(genreId);
  }
}
