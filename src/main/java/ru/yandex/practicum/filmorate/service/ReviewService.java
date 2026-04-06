package ru.yandex.practicum.filmorate.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.rewiew.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

  private final ReviewStorage reviewStorage;
  private final UserStorage userStorage;
  private final FilmStorage filmStorage;

  public Review add(Review review) {
    checkUserExists(review.getUserId());
    checkFilmExists(review.getFilmId());

    Review addedReview = reviewStorage.add(review);
    log.info("Добавлен новый отзыв: {}", addedReview);
    return addedReview;
  }

  public Review update(Review review) {
    getById(review.getReviewId());
    Review updatedReview = reviewStorage.update(review);
    log.info("Отзыв обновлен: {}", updatedReview);
    return updatedReview;
  }

  public void delete(int id) {
    getById(id);
    reviewStorage.delete(id);
    log.info("Отзыв с id {} удален", id);
  }

  public Review getById(int id) {
    return reviewStorage.findById(id)
        .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
  }

  public List<Review> getAll(Integer filmId, int count) {
    if (filmId != null) {
      checkFilmExists(filmId);
      return reviewStorage.getReviewsByFilmId(filmId, count);
    }
    return reviewStorage.findAll(count);
  }

  public void addLike(int id, int userId) {
    getById(id);
    checkUserExists(userId);
    reviewStorage.addLike(id, userId);
  }

  public void addDislike(int id, int userId) {
    getById(id);
    checkUserExists(userId);
    reviewStorage.addDislike(id, userId);
  }

  public void removeLike(int id, int userId) {
    getById(id);
    checkUserExists(userId);
    reviewStorage.removeLike(id, userId);
  }

  public void removeDislike(int id, int userId) {
    getById(id);
    checkUserExists(userId);
    reviewStorage.removeDislike(id, userId);
  }

  private void checkUserExists(int userId) {
    userStorage.findById(userId)
        .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
  }

  private void checkFilmExists(int filmId) {
    filmStorage.findById(filmId)
        .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
  }
}
