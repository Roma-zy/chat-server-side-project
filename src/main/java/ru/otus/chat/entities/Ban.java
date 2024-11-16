package ru.otus.chat.entities;

import java.sql.Timestamp;

public class Ban {
    private final Long id;
    private final Long userId;
    private final Long bannedById;
    private final String banReason;
    private final Timestamp bannedUntil;
    private final Timestamp createdAt;

    private Ban(BanBuilder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.bannedById = builder.bannedById;
        this.banReason = builder.banReason;
        this.bannedUntil = builder.bannedUntil;
        this.createdAt = builder.createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBannedById() {
        return bannedById;
    }

    public String getBanReason() {
        return banReason;
    }

    public Timestamp getBannedUntil() {
        return bannedUntil;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public static class BanBuilder {
        private Long id;
        private Long userId;
        private Long bannedById;
        private String banReason;
        private Timestamp bannedUntil;
        private Timestamp createdAt;

        public BanBuilder setId(Long id) {
            this.id = id;
            return this;
        }
        public BanBuilder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BanBuilder setBannedById(Long bannedById) {
            this.bannedById = bannedById;
            return this;
        }

        public BanBuilder setBanReason(String banReason) {
            this.banReason = banReason;
            return this;
        }

        public BanBuilder setBannedUntil(Timestamp bannedUntil) {
            this.bannedUntil = bannedUntil;
            return this;
        }

        public BanBuilder setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Ban build() {
            return new Ban(this);
        }
    }
}
