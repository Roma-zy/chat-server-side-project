package ru.otus.chat.repositories;

import ru.otus.chat.entities.CommonMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    public CommonMessage save(CommonMessage commonMessage) throws SQLException {
        String query = "INSERT INTO messages (room_id, sender_id, content, sent_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (commonMessage.getRoomId() != null) {
                statement.setLong(1, commonMessage.getRoomId());
            } else {
                statement.setNull(1, Types.BIGINT);
            }
            statement.setLong(2, commonMessage.getSenderId());
            statement.setString(3, commonMessage.getContent());
            statement.setTimestamp(4, commonMessage.getSentAt());
            statement.executeUpdate();

            Long generatedId = null;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getLong(1);
                }
            }

            return new CommonMessage.MessageBuilder()
                    .setId(generatedId)
                    .setRoomId(commonMessage.getRoomId())
                    .setSenderId(commonMessage.getSenderId())
                    .setContent(commonMessage.getContent())
                    .setSentAt(commonMessage.getSentAt())
                    .build();
        }
    }

    public List<CommonMessage> findMessagesByRoomId(Long roomId, int limit) throws SQLException {
        if (limit < 1) {
            limit = 50;
        }

        String query = "SELECT * FROM messages WHERE room_id = ? ORDER BY sent_at ASC LIMIT ?";

        List<CommonMessage> commonMessages = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, roomId);
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                CommonMessage commonMessage = new CommonMessage.MessageBuilder()
                        .setId(resultSet.getLong("id"))
                        .setRoomId(resultSet.getLong("room_id") != 0 ? resultSet.getLong("room_id") : null)
                        .setSenderId(resultSet.getLong("sender_id"))
                        .setContent(resultSet.getString("content"))
                        .setSentAt(resultSet.getTimestamp("sent_at"))
                        .build();
                commonMessages.add(commonMessage);
            }
        }
        return commonMessages;
    }

    public List<CommonMessage> findMessagesInCommonRoom(int limit) throws SQLException {
        if (limit < 1) {
            limit = 50;
        }

        String query = "SELECT * FROM messages WHERE room_id IS NULL ORDER BY sent_at ASC LIMIT ?";

        List<CommonMessage> commonMessages = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, limit);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                CommonMessage commonMessage = new CommonMessage.MessageBuilder()
                        .setId(resultSet.getLong("id"))
                        .setRoomId(null)
                        .setSenderId(resultSet.getLong("sender_id"))
                        .setContent(resultSet.getString("content"))
                        .setSentAt(resultSet.getTimestamp("sent_at"))
                        .build();
                commonMessages.add(commonMessage);
            }
        }
        return commonMessages;
    }
}