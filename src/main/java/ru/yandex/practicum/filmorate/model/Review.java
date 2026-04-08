package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {

  private Integer reviewId;
  @NotBlank
  private String content;
  @NotNull
  private Boolean isPositive;
  @NotNull
  private Integer userId;
  @NotNull
  private Integer filmId;
  private int useful;
}
