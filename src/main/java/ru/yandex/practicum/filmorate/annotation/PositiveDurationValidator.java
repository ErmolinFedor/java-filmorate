package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Duration;

public class PositiveDurationValidator implements ConstraintValidator<PositiveDuration, Duration> {

  @Override
  public boolean isValid(Duration value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }
    return value.isPositive();
  }
}
