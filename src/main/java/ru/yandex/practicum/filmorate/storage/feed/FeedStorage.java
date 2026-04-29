package ru.yandex.practicum.filmorate.storage.feed;

import java.util.Collection;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

public interface FeedStorage {

  void addEvent(Integer userId, Integer entityId, EventType eventType, Operation operation);

  Collection<Event> getFeed(Integer userId);
}
