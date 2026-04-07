package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {
  private final DirectorService directorService;

  @Autowired
  public DirectorController(DirectorService directorService) {
    this.directorService = directorService;
  }

  @GetMapping
  public Collection<Director> findAll() {
    log.info("Получен запрос GET /directors на получение всех режиссёров");
    return directorService.findAll();
  }

  @GetMapping("/{id}")
  public Director findById(@PathVariable int id) {
    log.info("Получен запрос GET /directors/{}", id);
    return directorService.getDirectorById(id);
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public Director create(@Valid @RequestBody Director director) throws ValidationException {
    log.info("Получен запрос POST /directors на добавление режиссёра: {}", director.getName());
    return directorService.create(director);
  }

  @PutMapping
  @ResponseStatus(CREATED)
  public Director update(@Valid @RequestBody Director director) throws ValidationException {
    log.info("Получен запрос POST /directors на обновление режиссёра: {}", director.getName());
    return directorService.update(director);
  }

  @DeleteMapping("/{id}")
  public Director deleteById(@PathVariable int id) {
    log.info("Получен запрос DELETE /directors/{} на удаление режиссёра", id);
    return directorService.remove(id);
  }
}
