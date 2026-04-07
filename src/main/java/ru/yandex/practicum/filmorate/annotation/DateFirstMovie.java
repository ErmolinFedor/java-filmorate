package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateFirstMovieValidator.class)
public @interface DateFirstMovie {
  String message() default "Дата должна быть после 28 декабря 1895 года";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
