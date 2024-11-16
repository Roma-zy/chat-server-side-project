package ru.otus.chat.entities;

import java.sql.Timestamp;

public class UserRoom {
    private final Long userId;
    private final Long currentRoomId;
    private final Timestamp joinedAt;

    private UserRoom(UserRoomBuilder builder) {
        this.userId = builder.userId;
        this.currentRoomId = builder.currentRoomId;
        this.joinedAt = builder.joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCurrentRoomId() {
        return currentRoomId;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public static class UserRoomBuilder {
        private Long userId;
        private Long currentRoomId;
        private Timestamp joinedAt;

        public UserRoomBuilder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserRoomBuilder setCurrentRoomId(Long currentRoomId) {
            this.currentRoomId = currentRoomId;
            return this;
        }

        public UserRoomBuilder setJoinedAt(Timestamp joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public UserRoom build() {
            return new UserRoom(this);
        }
    }
}

