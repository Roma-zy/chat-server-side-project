package ru.otus.chat.repositories;

import ru.otus.chat.entities.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class RoomRepository {
    public Room create(Room room) throws SQLException {
        String query = "INSERT INTO rooms (name, owner_id, password, last_used, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, room.getName());
            statement.setLong(2, room.getOwnerId());
            statement.setString(3, room.getPassword());
            statement.setTimestamp(4, room.getLastUsed());
            statement.setTimestamp(5, room.getCreatedAt());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating room failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new Room.RoomBuilder()
                            .setId(id)
                            .setName(room.getName())
                            .setOwnerId(room.getOwnerId())
                            .setPassword(room.getPassword())
                            .setLastUsed(room.getLastUsed())
                            .setCreatedAt(room.getCreatedAt())
                            .build();
                } else {
                    throw new SQLException("Creating room failed, no ID obtained.");
                }
            }
        }
    }


    public Room findByName(String name) throws SQLException {
        String query = "SELECT * FROM rooms WHERE name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapRoom(resultSet);
            }
        }
        return null;
    }


    public Room findRoomByUserId(Long userId) throws SQLException {
        String query = "SELECT r.* FROM rooms r " +
                "JOIN user_rooms ur ON ur.current_room_id = r.id " +
                "WHERE ur.user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapRoom(resultSet);
            }
        }
        return null;
    }

    public List<Room> findAllRooms() throws SQLException {
        String query = "SELECT id, name, owner_id, password, last_used, created_at FROM rooms";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Room> rooms = new ArrayList<>();

            while (resultSet.next()) {
                rooms.add(mapRoom(resultSet));
            }

            return rooms;
        }
    }

    public List<Room> findAllRoomsByUserId(Long userId) throws SQLException {
        String query = "SELECT r.* FROM rooms r WHERE r.owner_id = ?";
        List<Room> rooms = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                rooms.add(mapRoom(resultSet));
            }
        }
        return rooms;
    }

    public List<Room> findRoomsNotUsedSince(Timestamp date) throws SQLException {
        String query = "SELECT r.* FROM rooms r WHERE r.last_used < ?";

        List<Room> rooms = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setTimestamp(1, date);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                rooms.add(mapRoom(resultSet));
            }
        }
        return rooms;
    }

    public void deleteByIds(List<Long> ids) throws SQLException {
        if(ids.isEmpty()) return;
        String query = "DELETE FROM rooms WHERE id IN (" +
                String.join(",", ids.stream().map(id -> "?").toArray(String[]::new)) + ")";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < ids.size(); i++) {
                statement.setLong(i + 1, ids.get(i));
            }

            statement.executeUpdate();
        }
    }

    private Room mapRoom(ResultSet resultSet) throws SQLException {
        return new Room.RoomBuilder()
                .setId(resultSet.getLong("id"))
                .setName(resultSet.getString("name"))
                .setOwnerId(resultSet.getLong("owner_id"))
                .setPassword(resultSet.getString("password"))
                .setLastUsed(resultSet.getTimestamp("last_used"))
                .setCreatedAt(resultSet.getTimestamp("created_at"))
                .build();
    }
}
