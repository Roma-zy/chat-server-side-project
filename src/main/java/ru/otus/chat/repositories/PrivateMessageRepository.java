package ru.otus.chat.repositories;

import ru.otus.chat.entities.PrivateMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrivateMessageRepository {
    public PrivateMessage save(PrivateMessage privateMessage) throws SQLException {
        String query = "INSERT INTO private_messages (sender_id, receiver_id, content, sent_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, privateMessage.getSenderId());
            statement.setLong(2, privateMessage.getReceiverId());
            statement.setString(3, privateMessage.getContent());
            statement.setTimestamp(4, privateMessage.getSentAt());
            statement.executeUpdate();

            Long generatedId = null;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getLong(1);
                }
            }

            return new PrivateMessage.PrivateMessageBuilder()
                    .setId(generatedId)
                    .setSenderId(privateMessage.getSenderId())
                    .setReceiverId(privateMessage.getReceiverId())
                    .setContent(privateMessage.getContent())
                    .setSentAt(privateMessage.getSentAt())
                    .build();
        }
    }

    public List<PrivateMessage> findMessagesByUser(Long userId, int limit) throws SQLException {
        if (limit < 1) {
            limit = 50;
        }

        String query = "SELECT * FROM private_messages WHERE sender_id = ? OR receiver_id = ? ORDER BY sent_at ASC LIMIT ?";

        List<PrivateMessage> messages = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setInt(3, limit);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PrivateMessage message = new PrivateMessage.PrivateMessageBuilder()
                        .setId(resultSet.getLong("id"))
                        .setSenderId(resultSet.getLong("sender_id"))
                        .setReceiverId(resultSet.getLong("receiver_id"))
                        .setContent(resultSet.getString("content"))
                        .setSentAt(resultSet.getTimestamp("sent_at"))
                        .build();
                messages.add(message);
            }
        }
        return messages;
    }
}
