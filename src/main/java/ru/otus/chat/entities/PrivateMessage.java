package ru.otus.chat.entities;

import java.sql.Timestamp;

public class PrivateMessage implements Message {
    private final Long id;
    private final Long senderId;
    private final Long receiverId;
    private final String content;
    private final Timestamp sentAt;

    private PrivateMessage(PrivateMessageBuilder builder) {
        this.id = builder.id;
        this.senderId = builder.senderId;
        this.receiverId = builder.receiverId;
        this.content = builder.content;
        this.sentAt = builder.sentAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public static class PrivateMessageBuilder {
        private Long id;
        private Long senderId;
        private Long receiverId;
        private String content;
        private Timestamp sentAt;

        public PrivateMessageBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public PrivateMessageBuilder setSenderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public PrivateMessageBuilder setReceiverId(Long receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public PrivateMessageBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        public PrivateMessageBuilder setSentAt(Timestamp sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public PrivateMessage build() {
            return new PrivateMessage(this);
        }
    }
}
