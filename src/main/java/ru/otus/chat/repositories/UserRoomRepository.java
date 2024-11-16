package ru.otus.chat.repositories;

import ru.otus.chat.entities.UserRoom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRoomRepository {
    public UserRoom findUserRoomByUserId(Long userId) throws SQLException {
        String query = "SELECT * FROM user_rooms WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new UserRoom.UserRoomBuilder()
                        .setUserId(resultSet.getLong("user_id"))
                        .setCurrentRoomId(resultSet.getLong("current_room_id"))
                        .setJoinedAt(resultSet.getTimestamp("joined_at"))
                        .build();
            }
        }
        return null;
    }

    public void addUserRoom(Long userId, Long roomId, Timestamp joinedAt) throws SQLException {
        String query = "INSERT INTO user_rooms (user_id, current_room_id, joined_at) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setLong(2, roomId);
            statement.setTimestamp(3, joinedAt);
            statement.executeUpdate();
        }
    }

    public void cleanRoom(Long userId) throws SQLException {
        String query = "DELETE FROM user_rooms WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    public void changeUserRoom(Long userId, Long newRoomId, Timestamp joinedAt) throws SQLException {
        String query = "UPDATE user_rooms SET current_room_id = ?, joined_at = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, newRoomId);
            statement.setTimestamp(2, joinedAt);
            statement.setLong(3, userId);
            statement.executeUpdate();
        }
    }

    public List<Long> findUserIdsByRoomId(Long roomId) throws SQLException {
        String query = "SELECT user_id FROM user_rooms WHERE current_room_id = ?";
        List<Long> userIds = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, roomId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                userIds.add(resultSet.getLong("user_id"));
            }
        }

        return userIds;
    }
}

