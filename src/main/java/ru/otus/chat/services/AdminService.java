package ru.otus.chat.services;

import ru.otus.chat.entities.Ban;
import ru.otus.chat.entities.User;
import ru.otus.chat.repositories.BanRepository;

import java.sql.SQLException;
import java.sql.Timestamp;

public class AdminService {
    private final BanRepository banRepository = new BanRepository();
    private final UserService userService = new UserService();

    public User banUser(User admin, User user, String reason, Timestamp until) throws IllegalArgumentException {
        try {
            banRepository.banUser(new Ban.BanBuilder()
                    .setBannedById(admin.getId())
                    .setUserId(user.getId())
                    .setBannedUntil(until)
                    .setBannedById(admin.getId())
                    .setBanReason(reason)
                    .build());

            return userService.banUser(user, until);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
