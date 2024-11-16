package ru.otus.chat.services;

import ru.otus.chat.entities.Room;
import ru.otus.chat.entities.User;
import ru.otus.chat.entities.UserRoom;
import ru.otus.chat.repositories.UserRepository;
import ru.otus.chat.repositories.UserRoomRepository;
import ru.otus.chat.server.Messages;
import ru.otus.chat.server.Role;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserService {
    private final UserRepository userRepository = new UserRepository();
    private final RoomService roomService = new RoomService();
    private final UserRoomRepository userRoomRepository = new UserRoomRepository();

    public User register(String username, String password, String nickname) throws IllegalArgumentException {
        if (username.length() < 2) {
            throw new IllegalArgumentException(Messages.MIN_NAME_LENGTH);
        }
        if (password.length() < 2) {
            throw new IllegalArgumentException(Messages.MIN_PASSWORD_LENGTH);
        }
        if (nickname.length() < 2) {
            throw new IllegalArgumentException(Messages.MIN_NICKNAME_LENGTH);
        }

        try {
            User user = new User.UserBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .setNickname(nickname)
                    .setRoleId(Role.USER.getId())
                    .setLastActive(Timestamp.valueOf(LocalDateTime.now()))
                    .setIsBanned(false)
                    .build();
            if (user != null) {
                return userRepository.save(user);
            }
            throw new IllegalArgumentException(Messages.REGISTRATION_ERROR);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.REGISTRATION_ERROR);
        }
    }

    public User authenticate(String username, String password) throws IllegalArgumentException {
        try {
            User user = userRepository.findByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
            throw new IllegalArgumentException(Messages.AUTH_WRONG_LOGIN_PASSWORD);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.UNKNOWN_ERROR);
        }
    }

    public User getUserByNickName(String nickName) throws IllegalArgumentException {
        try {
            User user = userRepository.findByNickName(nickName);

            if (user == null) throw new IllegalArgumentException(Messages.USER_NOT_FOUND);
            return user;
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.USER_NOT_FOUND);
        }
    }

    public Map<Long, User> getByIds(List<Long> ids) throws IllegalArgumentException {
        try {
            List<User> users = userRepository.findByIds(ids);
            return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.UNKNOWN_ERROR);
        }
    }

    public User changeNickname(User user, String newNickname) throws RuntimeException {
        try {
            User updateUser = new User.UserBuilder()
                    .setRoleId(user.getRoleId())
                    .setNickname(newNickname)
                    .setPassword(user.getPassword())
                    .setId(user.getId())
                    .setUsername(user.getUsername())
                    .setIsBanned(user.isBanned())
                    .setBanUntil(user.getBanUntil())
                    .setLastActive(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            return userRepository.update(updateUser);
        } catch (SQLException e) {
            throw new RuntimeException(Messages.CHANGE_NICKNAME_ERROR);
        }
    }

    public User banUser(User user, Timestamp until) throws IllegalArgumentException {
        try {
            User updateUser = new User.UserBuilder()
                    .setRoleId(user.getRoleId())
                    .setNickname(user.getNickname())
                    .setPassword(user.getPassword())
                    .setId(user.getId())
                    .setUsername(user.getUsername())
                    .setIsBanned(true)
                    .setBanUntil(until)
                    .setLastActive(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            return userRepository.update(updateUser);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.USER_NOT_FOUND);
        }
    }

    public void unbanUsers(Timestamp time) throws IllegalArgumentException {
        try {
            userRepository.unbanUsers(time);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User updateLastActive(User user) {
        try {
            return userRepository.update(new User.UserBuilder()
                    .setId(user.getId())
                    .setUsername(user.getUsername())
                    .setPassword(user.getPassword())
                    .setNickname(user.getNickname())
                    .setRoleId(user.getRoleId())
                    .setLastActive(Timestamp.valueOf(LocalDateTime.now()))
                    .setIsBanned(user.isBanned())
                    .setBanUntil(user.getBanUntil())
                    .build());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public Room changeRoom(Long userId, String roomName, String password) {
        try {
            Room room = roomService.findRoomByName(roomName);

            if (room == null) {
                throw new IllegalArgumentException(Messages.NO_COMMAND_ERROR);
            }

            if (!Objects.equals(password, room.getPassword())) {
                throw new IllegalArgumentException(Messages.AUTH_WRONG_LOGIN_PASSWORD);
            }

            UserRoom userRoom = userRoomRepository.findUserRoomByUserId(userId);

            if (userRoom != null) {
                userRoomRepository.changeUserRoom(userId, room.getId(), Timestamp.valueOf(LocalDateTime.now()));
                return room;
            }

            userRoomRepository.addUserRoom(userId, room.getId(), Timestamp.valueOf(LocalDateTime.now()));
            return room;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(Messages.UNKNOWN_ERROR);
        }
    }

    public void cleanRoom(Long userId) {
        try {
            userRoomRepository.cleanRoom(userId);
        } catch (SQLException e) {
            throw new RuntimeException(Messages.UNKNOWN_ERROR);
        }
    }
}
