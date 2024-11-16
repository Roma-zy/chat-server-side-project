package ru.otus.chat.services;

import ru.otus.chat.entities.Room;
import ru.otus.chat.repositories.RoomRepository;
import ru.otus.chat.repositories.UserRoomRepository;
import ru.otus.chat.server.Messages;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RoomService {
    private final int ROOM_LIMIT = 5;
    private final int ROOM_LIVE_DAYS = 1;
    private final RoomRepository roomRepository = new RoomRepository();
    private final UserRoomRepository userRoomRepository = new UserRoomRepository();

    public Room findRoomByName(String name) throws SQLException {
        return roomRepository.findByName(name);
    }

    public Room findRoomByUserId(Long id) throws IllegalArgumentException {
        try {
            return roomRepository.findRoomByUserId(id);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.ROOM_NOT_AVAILABLE);
        }
    }

    public Room createRoom(String name, String password, Long ownerId) throws IllegalArgumentException {
        List<Room> userRooms;
        try {
            userRooms = roomRepository.findAllRoomsByUserId(ownerId);
        } catch (SQLException е) {
            throw new IllegalArgumentException(Messages.INVALID_USER_ID);
        }

        if (userRooms.size() >= ROOM_LIMIT) {
            throw new  IllegalArgumentException(Messages.ROOM_LIMIT);
        }

        try {
            Room room = new Room.RoomBuilder()
                    .setName(name)
                    .setOwnerId(ownerId)
                    .setPassword(password)
                    .setLastUsed(Timestamp.valueOf(LocalDateTime.now()))
                    .setCreatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            return roomRepository.create(room);
        } catch (SQLException e) {
            throw new  IllegalArgumentException(String.format(Messages.ROOM_NAME_TAKEN, name));
        }
    }

    public List<Long> findUserIdsByRoomId(Long id) throws SQLException {
        return userRoomRepository.findUserIdsByRoomId(id);
    }

    public List<Room> findAllRooms() {
        try {
            return roomRepository.findAllRooms();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void deleteInactiveRooms() {
        try {
            List<Long> inactiveRoomsIds = roomRepository.
                    findRoomsNotUsedSince(Timestamp.valueOf(LocalDateTime.now().minusDays(ROOM_LIVE_DAYS)))
                    .stream()
                        .map(Room::getId)
                        .collect(Collectors.toList());


            roomRepository.deleteByIds(inactiveRoomsIds);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

