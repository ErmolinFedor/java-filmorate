package ru.yandex.practicum.filmorate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbMpaServiceTest {

  private final MpaService mpaService;

  @Test
  void findAllMpaSuccessfully() {
    Collection<Mpa> mpaList = mpaService.findAll();

    assertEquals(5, mpaList.size(), "Должно быть ровно 5 рейтингов MPA");
  }

  @Test
  void findMpaByIdSuccessfully() {
    Mpa mpa = mpaService.findById(1);

    assertNotNull(mpa);
    assertEquals("G", mpa.getName(), "Рейтинг с ID=1 должен называться 'G'");
  }

  @Test
  void findMpaByIdThrowsNotFoundException() {
    int nonExistentId = 99;

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> mpaService.findById(nonExistentId));

    assertTrue(exception.getMessage().contains("не найден"));
  }
}
