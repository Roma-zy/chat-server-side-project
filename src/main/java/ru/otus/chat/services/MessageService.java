package ru.otus.chat.services;

import ru.otus.chat.entities.CommonMessage;
import ru.otus.chat.entities.Message;
import ru.otus.chat.entities.PrivateMessage;
import ru.otus.chat.entities.User;
import ru.otus.chat.repositories.MessageRepository;
import ru.otus.chat.repositories.PrivateMessageRepository;
import ru.otus.chat.server.Messages;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

public class MessageService {
    private final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private final String MESSAGE_PATTERN = "[%s]:[%s]: %s";
    private final String SYSTEM_MESSAGE_PATTERN = "[PRIVATE] [%s]:[SYSTEM]: %s";
    private final String PRIVATE = "[PRIVATE] ";
    private final MessageRepository messageRepository = new MessageRepository();
    private final PrivateMessageRepository privateMessageRepository = new PrivateMessageRepository();

    public CommonMessage sendMessage(Long senderId, String content) throws SQLException {
        CommonMessage commonMessage = new CommonMessage.MessageBuilder()
                .setSenderId(senderId)
                .setContent(content)
                .setSentAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        return messageRepository.save(commonMessage);
    }

    public CommonMessage sendMessage(Long senderId, String content, Long roomId) throws SQLException {
        CommonMessage commonMessage = new CommonMessage.MessageBuilder()
                .setRoomId(roomId)
                .setSenderId(senderId)
                .setContent(content)
                .setSentAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        return messageRepository.save(commonMessage);
    }

    public PrivateMessage sendPrivateMessage(Long senderId, Long receiverId, String content) throws IllegalArgumentException {
        try {
            PrivateMessage privateMessage = new PrivateMessage.PrivateMessageBuilder()
                    .setSenderId(senderId)
                    .setReceiverId(receiverId)
                    .setContent(content)
                    .setSentAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            return privateMessageRepository.save(privateMessage);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.USER_NOT_FOUND);
        }
    }

    public List<CommonMessage> getRoomMessages(Long roomId, int limit) {
        try {
            return messageRepository.findMessagesByRoomId(roomId, limit);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.MESSAGE_HISTORY_NOT_AVAILABLE);
        }
    }

    public List<CommonMessage> getCommonRoomMessages(int limit) {
        try {
            return messageRepository.findMessagesInCommonRoom(limit);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.MESSAGE_HISTORY_NOT_AVAILABLE);
        }
    }

    public List<PrivateMessage> getPrivateMessages(Long id, int limit) {
        try {
            return privateMessageRepository.findMessagesByUser(id, limit);
        } catch (SQLException e) {
            throw new IllegalArgumentException(Messages.MESSAGE_HISTORY_NOT_AVAILABLE);
        }
    }

    public String messageFormat(Message message, User user, Boolean isPrivate) {
        StringBuilder resultBuilder = new StringBuilder();
        if (isPrivate) resultBuilder.append(PRIVATE);
        resultBuilder.append(String.format(
                MESSAGE_PATTERN,
                timestampToString(message.getSentAt()),
                user.getNickname(),
                message.getContent()
        ));

        return resultBuilder.toString();
    }

    public String systemMessageFormat(String message) {
        return String.format(
                SYSTEM_MESSAGE_PATTERN,
                timestampToString(Timestamp.valueOf(LocalDateTime.now())),
                message
        );
    }

    private String timestampToString(Timestamp timestamp) {
        return new SimpleDateFormat(DATE_PATTERN).format(timestamp);
    }
}
