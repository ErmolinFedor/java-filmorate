package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {

  private Integer eventId;
  private Integer entityId;
  private Integer userId;
  private EventType eventType;
  private Operation operation;
  private Long timestamp;
}
