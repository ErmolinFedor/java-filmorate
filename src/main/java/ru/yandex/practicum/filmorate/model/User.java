package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private Integer id;

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String login;

  private String name;

  @Past
  private LocalDate birthday;

  @Builder.Default
  private Set<Integer> friends = new HashSet<>();
}
