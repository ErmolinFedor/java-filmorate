package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @Autowired
  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @PostMapping
  public Review addReview(@Valid @RequestBody Review review) {
    log.info("Добавление нового отзыва: {}", review);
    return reviewService.add(review);
  }

  @PutMapping
  public Review updateReview(@Valid @RequestBody Review review) {
    log.info("Обновление отзыва: {}", review);
    return reviewService.update(review);
  }

  @DeleteMapping("/{id}")
  public void deleteReview(@PathVariable int id) {
    log.info("Удаление отзыва с id: {}", id);
    reviewService.delete(id);
  }

  @GetMapping("/{id}")
  public Review getReviewById(@PathVariable int id) {
    log.info("Получение отзыва с id: {}", id);
    return reviewService.getById(id);
  }

  @GetMapping
  public List<Review> getReviews(
      @RequestParam(required = false) Integer filmId,
      @RequestParam(defaultValue = "10") int count) {
    log.info("Получение отзывов для фильма: {}, лимит: {}", filmId, count);
    return reviewService.getAll(filmId, count);
  }

  @PutMapping("/{id}/like/{userId}")
  public void addLike(@PathVariable int id, @PathVariable int userId) {
    log.info("Пользователь {} ставит лайк отзыву {}", userId, id);
    reviewService.addLike(id, userId);
  }

  @PutMapping("/{id}/dislike/{userId}")
  public void addDislike(@PathVariable int id, @PathVariable int userId) {
    log.info("Пользователь {} ставит дизлайк отзыву {}", userId, id);
    reviewService.addDislike(id, userId);
  }

  @DeleteMapping("/{id}/like/{userId}")
  public void removeLike(@PathVariable int id, @PathVariable int userId) {
    log.info("Пользователь {} удаляет лайк у отзыва {}", userId, id);
    reviewService.removeLike(id, userId);
  }

  @DeleteMapping("/{id}/dislike/{userId}")
  public void removeDislike(@PathVariable int id, @PathVariable int userId) {
    log.info("Пользователь {} удаляет дизлайк у отзыва {}", userId, id);
    reviewService.removeDislike(id, userId);
  }
}
