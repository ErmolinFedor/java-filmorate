package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.DateFirstMovie;
import ru.yandex.practicum.filmorate.annotation.PositiveDuration;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
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
  @JsonDeserialize(using = DurationDeserializer.class)
  private Duration duration;

  private Mpa mpa;

  @Builder.Default
  private LinkedHashSet<Genre> genres = new LinkedHashSet<>();

  @Builder.Default
  private LinkedHashSet<Director> directors = new LinkedHashSet<>();

  @JsonIgnore
  @Builder.Default
  private Set<Integer> likes = new HashSet<>();
}
