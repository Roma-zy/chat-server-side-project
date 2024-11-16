package ru.otus.chat.entities;

import java.sql.Timestamp;

public class CommonMessage implements Message {
    private final Long id;
    private final Long roomId;
    private final Long senderId;
    private final String content;
    private final Timestamp sentAt;

    private CommonMessage(MessageBuilder builder) {
        this.id = builder.id;
        this.roomId = builder.roomId;
        this.senderId = builder.senderId;
        this.content = builder.content;
        this.sentAt = builder.sentAt;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public static class MessageBuilder {
        private Long id;
        private Long roomId;
        private Long senderId;
        private String content;
        private Timestamp sentAt;

        public MessageBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public MessageBuilder setRoomId(Long roomId) {
            this.roomId = roomId;
            return this;
        }

        public MessageBuilder setSenderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public MessageBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        public MessageBuilder setSentAt(Timestamp sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public CommonMessage build() {
            return new CommonMessage(this);
        }
    }
}
