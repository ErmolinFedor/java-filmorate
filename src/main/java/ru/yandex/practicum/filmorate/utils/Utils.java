package ru.yandex.practicum.filmorate.utils;

import java.util.Map;

public final class Utils {
  public static Integer getNextId(Map<Integer, ?> data) {
    int currentMaxId = data.keySet()
        .stream()
        .mapToInt(id -> id)
        .max()
        .orElse(0);
    return ++currentMaxId;
  }
}
