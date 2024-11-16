package ru.otus.chat.entities;

import java.sql.Timestamp;

public class Room {
    private final Long id;
    private final String name;
    private final Long ownerId;
    private final String password;
    private final Timestamp lastUsed;
    private final Timestamp createdAt;

    private Room(RoomBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.ownerId = builder.ownerId;
        this.password = builder.password;
        this.lastUsed = builder.lastUsed;
        this.createdAt = builder.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getPassword() {
        return password;
    }

    public Timestamp getLastUsed() {
        return lastUsed;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public static class RoomBuilder {
        private Long id;
        private String name;
        private Long ownerId;
        private String password;
        private Timestamp lastUsed;
        private Timestamp createdAt;

        public RoomBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public RoomBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public RoomBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public RoomBuilder setLastUsed(Timestamp lastUsed) {
            this.lastUsed = lastUsed;
            return this;
        }

        public RoomBuilder setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Room build() {
            return new Room(this);
        }
    }
}
