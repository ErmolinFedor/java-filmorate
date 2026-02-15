package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = "id")
public class User {

  private Integer id;

  @Email
  @NotNull
  @NotBlank
  private String email;

  @NotNull
  @NotBlank
  private String login;

  private String name;

  @NotNull
  @Past
  private LocalDate birthday;
}
