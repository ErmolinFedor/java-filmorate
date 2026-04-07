package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Director {
  private Integer id;
  private String name;
}
