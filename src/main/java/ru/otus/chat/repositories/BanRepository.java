package ru.otus.chat.repositories;

import ru.otus.chat.entities.Ban;

import java.sql.*;

public class BanRepository {

    public Ban banUser(Ban ban) throws SQLException {
        String query = "INSERT INTO bans (user_id, banned_by_id, ban_reason, banned_until, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, ban.getUserId());
            statement.setLong(2, ban.getBannedById());
            statement.setString(3, ban.getBanReason());
            statement.setTimestamp(4, ban.getBannedUntil());
            statement.setTimestamp(5, ban.getCreatedAt());
            statement.executeUpdate();

            Long generatedId = null;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getLong(1);
                }
            }

            return new Ban.BanBuilder()
                    .setId(generatedId)
                    .setBanReason(ban.getBanReason())
                    .setUserId(ban.getUserId())
                    .setBannedUntil(ban.getBannedUntil())
                    .setBannedById(ban.getBannedById())
                    .setCreatedAt(ban.getCreatedAt())
                    .build();
        }
    }
}
