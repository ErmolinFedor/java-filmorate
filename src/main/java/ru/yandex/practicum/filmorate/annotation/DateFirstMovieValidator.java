package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateFirstMovieValidator implements ConstraintValidator<DateFirstMovie, LocalDate> {
  @Override
  public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return !value.isBefore(LocalDate.of(1895,12,28));
  }
}
