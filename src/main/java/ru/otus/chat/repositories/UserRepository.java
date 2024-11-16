package ru.otus.chat.repositories;

import ru.otus.chat.entities.User;


import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserRepository {

    public User save(User user) throws SQLException {
        String query = "INSERT INTO users (username, password, nickname, role_id, is_banned, ban_until, last_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getNickname());
            statement.setLong(4, user.getRoleId());
            statement.setBoolean(5, user.isBanned());
            statement.setTimestamp(6, user.getBanUntil());
            statement.setTimestamp(7, user.getLastActive());

            statement.executeUpdate();

            Long generatedId = null;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getLong(1);
                }
            }

            return new User.UserBuilder()
                    .setId(generatedId)
                    .setUsername(user.getUsername())
                    .setPassword(user.getPassword())
                    .setNickname(user.getNickname())
                    .setRoleId(user.getRoleId())
                    .setIsBanned(user.isBanned())
                    .setBanUntil(user.getBanUntil())
                    .setLastActive(user.getLastActive())
                    .build();
        }
    }

    public void unbanUsers(Timestamp time) throws SQLException  {
        String query = "UPDATE users SET is_banned = false, ban_until = NULL WHERE ban_until < ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, time);
            statement.executeUpdate();
        }
    }

    public User update(User user) throws SQLException {
        String query = "UPDATE users SET username = ?, password = ?, nickname = ?, role_id = ?, last_active = ?, is_banned = ?, ban_until = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getNickname());
            preparedStatement.setLong(4, user.getRoleId());
            preparedStatement.setTimestamp(5, user.getLastActive());
            preparedStatement.setBoolean(6, user.isBanned());
            preparedStatement.setTimestamp(7, user.getBanUntil());

            preparedStatement.setLong(8, user.getId());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                return new User.UserBuilder()
                        .setId(user.getId())
                        .setUsername(user.getUsername())
                        .setPassword(user.getPassword())
                        .setNickname(user.getNickname())
                        .setRoleId(user.getRoleId())
                        .setIsBanned(user.isBanned())
                        .setBanUntil(user.getBanUntil())
                        .setLastActive(user.getLastActive())
                        .build();
            }
        }

        return null;
    }

    public User findByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new User.UserBuilder()
                        .setId(resultSet.getLong("id"))
                        .setUsername(resultSet.getString("username"))
                        .setPassword(resultSet.getString("password"))
                        .setNickname(resultSet.getString("nickname"))
                        .setRoleId(resultSet.getLong("role_id"))
                        .setIsBanned(resultSet.getBoolean("is_banned"))
                        .setBanUntil(resultSet.getTimestamp("ban_until"))
                        .setLastActive(resultSet.getTimestamp("last_active"))
                        .build();
            }
        }
        return null;
    }

    public User findByNickName(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE nickname = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new User.UserBuilder()
                        .setId(resultSet.getLong("id"))
                        .setUsername(resultSet.getString("username"))
                        .setPassword(resultSet.getString("password"))
                        .setNickname(resultSet.getString("nickname"))
                        .setRoleId(resultSet.getLong("role_id"))
                        .setIsBanned(resultSet.getBoolean("is_banned"))
                        .setBanUntil(resultSet.getTimestamp("ban_until"))
                        .setLastActive(resultSet.getTimestamp("last_active"))
                        .build();
            }
        }
        return null;
    }

    public List<User> findByIds(List<Long> ids) throws SQLException {
        if(ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String query = "SELECT * FROM users WHERE id IN (" + placeholders + ")";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < ids.size(); i++) {
                statement.setLong(i + 1, ids.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            List<User> users = new ArrayList<>();

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            return users;
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User.UserBuilder()
                .setId(resultSet.getLong("id"))
                .setUsername(resultSet.getString("username"))
                .setPassword(resultSet.getString("password"))
                .setNickname(resultSet.getString("nickname"))
                .setRoleId(resultSet.getLong("role_id"))
                .setIsBanned(resultSet.getBoolean("is_banned"))
                .setBanUntil(resultSet.getTimestamp("ban_until"))
                .setLastActive(resultSet.getTimestamp("last_active"))
                .build();
    }
}
