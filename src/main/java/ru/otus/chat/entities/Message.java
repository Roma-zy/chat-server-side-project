package ru.otus.chat.entities;

import java.sql.Timestamp;

public interface Message {
    Long getId();
    Long getSenderId();
    String getContent();
    Timestamp getSentAt();
}
