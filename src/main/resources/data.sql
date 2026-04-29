DELETE FROM MPA_RATINGS;
DELETE FROM GENRES;

INSERT INTO MPA_RATINGS (ID, NAME)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');

INSERT INTO GENRES (ID, NAME)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');

SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE review_likes;
TRUNCATE TABLE reviews;

ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1;

SET REFERENTIAL_INTEGRITY TRUE;