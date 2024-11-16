package ru.otus.chat.entities;

import java.sql.Timestamp;

public class User {
    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final Long roleId;
    private final Boolean isBanned;
    private final Timestamp banUntil;
    private final Timestamp lastActive;

    private User(UserBuilder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.password = builder.password;
        this.nickname = builder.nickname;
        this.roleId = builder.roleId;
        this.isBanned = builder.isBanned;
        this.banUntil = builder.banUntil;
        this.lastActive = builder.lastActive;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Boolean isBanned() {
        return isBanned;
    }

    public Timestamp getBanUntil() {
        return banUntil;
    }

    public Timestamp getLastActive() {
        return lastActive;
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String nickname;
        private Long roleId;
        private Boolean isBanned;
        private Timestamp banUntil;
        private Timestamp lastActive;

        public UserBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserBuilder setRoleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserBuilder setIsBanned(Boolean isBanned) {
            this.isBanned = isBanned;
            return this;
        }

        public UserBuilder setBanUntil(Timestamp banUntil) {
            this.banUntil = banUntil;
            return this;
        }

        public UserBuilder setLastActive(Timestamp lastActive) {
            this.lastActive = lastActive;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
