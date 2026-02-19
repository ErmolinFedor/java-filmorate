package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.DateFirstMovie;
import ru.yandex.practicum.filmorate.annotation.PositiveDuration;

@Data
@EqualsAndHashCode(exclude = "id")
public class Film {
  private Integer id;

  @NotBlank
  private String name;

  private String description;

  @DateFirstMovie(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate releaseDate;

  @NotNull
  @PositiveDuration(message = "Продолжительность фильма должна быть положительным числом")
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private Duration duration;
}
