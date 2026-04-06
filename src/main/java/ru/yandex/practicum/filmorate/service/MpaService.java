package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
public class MpaService {
  private final MpaStorage mpaStorage;

  @Autowired
  public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
    this.mpaStorage = mpaStorage;
  }

  public Collection<Mpa> findAll() {
    return mpaStorage.findAll();
  }

  public Mpa findById(int id) {
    return mpaStorage.findById(id)
        .orElseThrow(() -> new NotFoundException("Рейтинг MPA с Id " + id + " не найден"));
  }
}
