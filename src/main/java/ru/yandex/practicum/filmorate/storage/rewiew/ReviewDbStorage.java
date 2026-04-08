package ru.yandex.practicum.filmorate.storage.rewiew;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRawMapper;

@Repository
@Slf4j
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {

  private static final String FIND_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
  private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
  private static final String FIND_BY_FILM_ID_QUERY =
      "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
  private static final String INSERT_QUERY =
      "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
  private static final String UPDATE_QUERY =
      "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
  private static final String DELETE_QUERY = "DELETE FROM reviews WHERE review_id = ?";
  private static final String UPSERT_LIKE_QUERY =
      "MERGE INTO review_likes (review_id, user_id, is_like) KEY(review_id, user_id) VALUES (?, ?, ?)";
  private static final String DELETE_LIKE_QUERY =
      "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = ?";

  public ReviewDbStorage(JdbcTemplate jdbc, ReviewRawMapper mapper) {
    super(jdbc, mapper, Review.class);
  }

  @Override
  public Review add(Review review) {
    int id = insert(INSERT_QUERY,
        review.getContent(),
        review.getIsPositive(),
        review.getUserId(),
        review.getFilmId(),
        0
    );
    review.setReviewId(id);
    return review;
  }

  @Override
  public Review update(Review review) {
    int rowsUpdated = jdbc.update(UPDATE_QUERY,
        review.getContent(),
        review.getIsPositive(),
        review.getReviewId()
    );

    if (rowsUpdated == 0) {
      throw new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден");
    }

    return findById(review.getReviewId()).get();
  }

  @Override
  public void delete(int id) {
    jdbc.update(DELETE_QUERY, id);
  }

  @Override
  public Optional<Review> findById(int id) {
    return findOne(FIND_BY_ID_QUERY, id);
  }

  @Override
  public List<Review> findAll(int count) {
    return (List<Review>) findAll(FIND_ALL_QUERY, count);
  }

  @Override
  public List<Review> getReviewsByFilmId(int filmId, int count) {
    return (List<Review>) findAll(FIND_BY_FILM_ID_QUERY, filmId, count);
  }

  @Override
  public void addLike(int id, int userId) {
    jdbc.update(UPSERT_LIKE_QUERY, id, userId, true);
    updateUsefulRating(id);
  }

  @Override
  public void addDislike(int id, int userId) {
    jdbc.update(UPSERT_LIKE_QUERY, id, userId, false);
    updateUsefulRating(id);
  }

  @Override
  public void removeLike(int id, int userId) {
    jdbc.update(DELETE_LIKE_QUERY, id, userId, true);
    updateUsefulRating(id);
  }

  @Override
  public void removeDislike(int id, int userId) {
    jdbc.update(DELETE_LIKE_QUERY, id, userId, false);
    updateUsefulRating(id);
  }

  private void updateUsefulRating(int reviewId) {
    String sql = "UPDATE reviews SET useful = (" +
        "SELECT (SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = TRUE) - " +
        "(SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = FALSE)) " +
        "WHERE review_id = ?";
    jdbc.update(sql, reviewId, reviewId, reviewId);
  }
}
