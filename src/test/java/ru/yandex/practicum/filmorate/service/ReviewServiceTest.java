package ru.yandex.practicum.filmorate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.BaseServiceTest;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRawMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.rewiew.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@JdbcTest
@Import({ReviewService.class, ReviewDbStorage.class, ReviewRawMapper.class, UserDbStorage.class,
    UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewServiceTest extends BaseServiceTest {

  private final ReviewService reviewService;
  private final UserStorage userStorage;
  private final FilmStorage filmStorage;

  private User user;
  private Film film;

  @BeforeEach
  void setUp() {
    user = User.builder().email("reviewer@yandex.ru").login("reviewer").name("Reviewer Name")
        .birthday(LocalDate.now().minusYears(25)).build();
    userStorage.create(user);

    film = createValidFilm();
    filmStorage.create(film);
  }

  @Test
  void addReviewSuccessfully() {
    Review review = createValidReview(user.getId(), film.getId());
    Review saved = reviewService.add(review);

    assertNotNull(saved.getReviewId());
    assertEquals(review.getContent(), saved.getContent());
    assertEquals(0, saved.getUseful(), "Начальный рейтинг должен быть 0");
  }

  @Test
  void updateReviewContentAndTypeOnly() {
    Review review = reviewService.add(createValidReview(user.getId(), film.getId()));

    Review updateData = Review.builder().reviewId(review.getReviewId()).content("Updated Content")
        .isPositive(false).userId(999)
        .filmId(888)
        .build();

    Review updated = reviewService.update(updateData);

    assertEquals("Updated Content", updated.getContent());
    assertEquals(false, updated.getIsPositive());
    assertEquals(user.getId(), updated.getUserId(), "Автор не должен измениться");
    assertEquals(film.getId(), updated.getFilmId(), "Фильм не должен измениться");
  }

  @Test
  void deleteReviewSuccessfully() {
    Review review = reviewService.add(createValidReview(user.getId(), film.getId()));
    reviewService.delete(review.getReviewId());

    assertThrows(NotFoundException.class, () -> reviewService.getById(review.getReviewId()));
  }

  @Test
  void getReviewsByFilmIdWithLimit() {
    reviewService.add(createValidReview(user.getId(), film.getId()));

    User user2 = User.builder().email("2@2.ru").login("l2").birthday(LocalDate.now()).build();
    userStorage.create(user2);
    reviewService.add(createValidReview(user2.getId(), film.getId()));

    List<Review> reviews = reviewService.getAll(film.getId(), 1);

    assertEquals(1, reviews.size(), "Должен вернуться только 1 отзыв согласно лимиту");
  }

  @Test
  void addLikeIncreasesUsefulRating() {
    Review review = reviewService.add(createValidReview(user.getId(), film.getId()));

    reviewService.addLike(review.getReviewId(), user.getId());

    Review rated = reviewService.getById(review.getReviewId());
    assertEquals(1, rated.getUseful());
  }

  @Test
  void removeLikeDecreasesUsefulRating() {
    Review review = reviewService.add(createValidReview(user.getId(), film.getId()));
    reviewService.addLike(review.getReviewId(), user.getId());

    reviewService.removeLike(review.getReviewId(), user.getId());

    Review unrated = reviewService.getById(review.getReviewId());
    assertEquals(0, unrated.getUseful());
  }

  @Test
  void sortReviewsByUsefulRating() {
    Review badReview = reviewService.add(createValidReview(user.getId(), film.getId()));
    Review goodReview = reviewService.add(createValidReview(user.getId(), film.getId()));

    reviewService.addLike(goodReview.getReviewId(), user.getId());

    List<Review> reviews = reviewService.getAll(film.getId(), 10);

    assertEquals(goodReview.getReviewId(), reviews.get(0).getReviewId(),
        "Популярный отзыв должен быть первым");
  }

  @Test
  void addReviewWithNonExistentUserThrowsNotFound() {
    Review review = createValidReview(999, film.getId());
    assertThrows(NotFoundException.class, () -> reviewService.add(review));
  }

  private Review createValidReview(int userId, int filmId) {
    return Review.builder().content("This is a test review about the film.").isPositive(true)
        .userId(userId).filmId(filmId).build();
  }

  private Film createValidFilm() {
    return Film.builder().name("Groundhog Day").description(
            "Trapped in this time loop, he transforms from a selfish narcissist into a kinder person, ultimately finding love and breaking the curse.")
        .releaseDate(LocalDate.of(1993, 7, 12)).duration(Duration.ofMinutes(101))
        .mpa(new Mpa(1, "G")).build();
  }
}
