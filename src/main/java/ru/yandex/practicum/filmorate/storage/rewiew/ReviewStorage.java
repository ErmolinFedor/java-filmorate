package ru.yandex.practicum.filmorate.storage.rewiew;

import java.util.List;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.Review;

public interface ReviewStorage {

  Review add(Review review);

  Review update(Review review);

  void delete(int id);

  Optional<Review> findById(int id);

  List<Review> findAll(int count);

  List<Review> getReviewsByFilmId(int filmId, int count);

  void addLike(int id, int userId);

  void addDislike(int id, int userId);

  void removeLike(int id, int userId);

  void removeDislike(int id, int userId);

}
