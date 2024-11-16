package ru.otus.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.chat.entities.*;
import ru.otus.chat.services.AdminService;
import ru.otus.chat.services.MessageService;
import ru.otus.chat.services.RoomService;
import ru.otus.chat.services.UserService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClientHandler {
    private final Logger logger = LogManager.getLogger(ClientHandler.class);

    private User user;
    private Room room;
    private volatile boolean isRunning = true;

    private final UserService userService = new UserService();
    private final MessageService messageService = new MessageService();
    private final RoomService roomService = new RoomService();
    private final AdminService adminService = new AdminService();
    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    // TODO Ошибка при дисконекте
    private final ScheduledExecutorService inactivityChecker = Executors.newSingleThreadScheduledExecutor();
    private static final long INACTIVITY_LIMIT = 20 * 60 * 1000;
    private static final long SPAM_LIMIT = 10 * 1000;

    public User getUser() {
        return user;
    }

    public Room getRoom() {
        return room;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        start();
    }

    private void start() {
        inactivityChecker.scheduleAtFixedRate(this::checkInactivity, 1, 1, TimeUnit.MINUTES);
        new Thread(() -> {
            try {
                logger.info(Messages.CLIENT_CONNECTION_SUCCESS);

                while (user == null) {
                    String message = in.readUTF();

                    String[] messageArray = message.trim().split(" ");
                    String command = messageArray[0];
                    String[] params = Arrays.copyOfRange(messageArray, 1, messageArray.length);

                    switch (command) {
                        case "/exit":
                            exit();
                            return;
                        case "/auth":
                            authenticate(params);
                            break;
                        case "/registration":
                            registration(params);
                            break;
                        default:
                            systemSendMessage(Messages.NEED_AUTH_OR_REGISTRATION);
                            break;
                    }
                }
                updateLastActivityTime();
                logger.info(Messages.AUTH_SUCCESS, user.getUsername());
                sendHistoryMessages();

                while (isRunning) {
                    if (socket.isClosed()) {
                        return;
                    }
                    String message = in.readUTF();

                    if (message.startsWith("/")) {
                        String[] messageArray = message.trim().split(" ");
                        String command = messageArray[0];
                        String[] params = Arrays.copyOfRange(messageArray, 1, messageArray.length);

                        switch (command) {
                            case "/exit":
                                exit();
                                return;
                            case "/w":
                                privateMessage(params);
                                break;
                            case "/kick":
                                kick(params);
                                break;
                            case "/create_room":
                                createRoom(params);
                                break;
                            case "/room_list":
                                roomList();
                                break;
                            case "/enter":
                                checkout(params);
                                break;
                            case "/main_room":
                                mainRoom();
                                break;
                            case "/changenick":
                                changeNick(params);
                                break;
                            case "/status":
                                sendStatus();
                                break;
                            case "/ban":
                                banUser(params);
                                break;
                            case "/shutdown":
                                shutdown();
                                break;
                            default:
                                systemSendMessage(Messages.NO_COMMAND_ERROR);
                                break;
                        }
                        updateLastActivityTime();
                        continue;
                    }

                    if(user.isBanned()) {
                        systemSendMessage(Messages.BLOCKED_ERROR);
                        continue;
                    }

                    if(SPAM_LIMIT > new Timestamp(System.currentTimeMillis()).getTime() - user.getLastActive().getTime()) {
                        systemSendMessage(Messages.SPAM_ERROR);
                        continue;
                    }
                    updateLastActivityTime();
                    if(room != null) {
                        CommonMessage objCommonMessage = messageService.sendMessage(user.getId(), message, room.getId());
                        List<Long> usersIdsInRoom = roomService.findUserIdsByRoomId(room.getId());
                        usersIdsInRoom.forEach((id) -> {
                            server.sendByUserIdMessage(id, this.getUser(), objCommonMessage, false);
                        });
                    } else {
                        CommonMessage objCommonMessage = messageService.sendMessage(user.getId(), message);
                        server.broadcastMessage(objCommonMessage, user);
                    }
                }
            } catch (IOException e) {
                logger.error(Messages.ERROR, e);
                e.printStackTrace();
            } catch (SQLException e) {
                logger.error(Messages.ERROR, e);
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void disconnect() {
        isRunning = false;
        try {
            in.close();
        } catch (IOException e) {
            logger.error(Messages.ERROR, e);
            throw new RuntimeException(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            logger.error(Messages.ERROR, e);
            throw new RuntimeException(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            logger.error(Messages.ERROR, e);
            throw new RuntimeException(e);
        }
        server.unsubscribe(this);
    }

    public void sendClientMessage(Message message, User user, Boolean isPrivate) {
        try {
            out.writeUTF(messageService.messageFormat(message, user, isPrivate));
        } catch (IOException e) {
            logger.error(Messages.ERROR, e);
            e.printStackTrace();
        }
    }

    private void authenticate(String... params) {
        if (params.length != 2) {
            systemSendMessage(Messages.WRONG_AUTH_FORMAT_ERROR);
            return;
        }
        try {
            user = userService.authenticate(params[0], params[1]);
            if(user != null) {
                room = roomService.findRoomByUserId(user.getId());
                server.subscribe(this);
            }
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }
    }

    private void registration(String... params) {
        if (params.length != 3) {
            systemSendMessage(Messages.WRONG_REG_FORMAT_ERROR);
            return;
        }

        try {
            user = userService.register(params[0], params[1], params[2]);
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }
    }

    public void exit() {
        systemSendMessage(Messages.EXIT_OK_MESSAGE);
        disconnect();
    }

    private void privateMessage(String... params) {
        if (params.length == 0) {
            systemSendMessage(Messages.WRONG_PRIVATE_MESSAGE_FORMAT_ERROR);
            return;
        }

        if (params.length == 1) {
            systemSendMessage(Messages.WRONG_PRIVATE_MESSAGE_FORMAT_NO_MESSAGE_ERROR);
            return;
        }
        String messageWithoutCommand = String.join(" ", Arrays.copyOfRange(params, 1, params.length));

        try {
            User receiver = userService.getUserByNickName(params[0]);
            PrivateMessage privateMessage = messageService.sendPrivateMessage(
                    user.getId(),
                    receiver.getId(),
                    messageWithoutCommand
            );
            sendClientMessage(
                    privateMessage,
                    user,
                    true
            );

            server.sendByUserIdMessage(
                    receiver.getId(),
                    this.getUser(),
                    privateMessage,
                    true
            );
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }
    }

    private void kick(String... params) {
        if (!isAdmin()) {
            systemSendMessage(Messages.NO_COMMAND_ERROR);
            return;
        }

        if (params.length == 0) {
            systemSendMessage(Messages.WRONG_FORMAT_NO_NICK_ERROR);
            return;
        }

        try {
            User kickedUser = userService.getUserByNickName(params[0]);
            server.kick(kickedUser, this);
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }

    }

    private void createRoom(String... params) {
        String resultMessage;
        String password = params.length < 2 ? null : params[1];
        if (params.length < 1) {
            systemSendMessage(Messages.WRONG_CREATE_ROOM_FORMAT_ERROR);
            return;
        }

        try {
            Room room = roomService.createRoom(params[0], password, user.getId());

            resultMessage = String.format(
                    Messages.SUCCESS_ROOM_CREATED,
                    room.getName(),
                    room.getPassword()
            );
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            resultMessage = e.getMessage();
        }
        systemSendMessage(resultMessage);
    }

    private void roomList() {
        List<Room> rooms = roomService.findAllRooms();
        StringBuilder result = new StringBuilder();
        result.append(Messages.ROOM_LIST);
        rooms.forEach((room) -> {
            result.append(String.format(Messages.IN_LIST, room.getName()));
        });

        systemSendMessage(result.toString());
    }

    private void checkout(String... params) {
        String resultMessage;
        String password = params.length < 2 ? null : params[1];
        if (params.length < 1) {
            systemSendMessage(Messages.WRONG_CHECKOUT_FORMAT_ERROR);
            return;
        }

        try {
            room = userService.changeRoom(user.getId(), params[0], password);
            resultMessage = String.format(Messages.SUCCESS_ROOM_CHANGED, room.getName());
            sendHistoryMessages();
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            resultMessage = e.getMessage();
        }
        systemSendMessage(resultMessage);
    }

    private void mainRoom() {
        try {
            userService.cleanRoom(user.getId());
            room = null;
        } catch (RuntimeException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }

        systemSendMessage(Messages.SUCCESS_MAIN_ROOM_CHANGED);
        sendHistoryMessages();
    }

    private void changeNick(String... params) {
        if (params.length != 1) {
            systemSendMessage(Messages.WRONG_CHANGE_NICK_FORMAT_ERROR);
            return;
        }
        try {
            user = userService.changeNickname(user, params[0]);
            systemSendMessage(String.format(Messages.SUCCESS_CHANGE_NICK_CHANGED, user.getNickname()));
        } catch (RuntimeException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }
    }

    private void sendHistoryMessages() {
        try {
            List<CommonMessage> historyCommonMessages;
            List<PrivateMessage> historyPrivateMessages;

            if (room != null) {
                historyCommonMessages = messageService.getRoomMessages(room.getId(), 100);
            } else  {
                historyCommonMessages = messageService.getCommonRoomMessages(100);
            }

            historyPrivateMessages = messageService.getPrivateMessages(user.getId(), 100);

            List<Message> allMessages = new ArrayList<>();
            allMessages.addAll(historyCommonMessages);
            allMessages.addAll(historyPrivateMessages);


            allMessages.sort(Comparator.comparing(Message::getSentAt));

            Set<Long> senderIds = allMessages.stream()
                    .map(Message::getSenderId)
                    .collect(Collectors.toSet());

            List<Long> senderIdsList = new ArrayList<>(senderIds);
            Map<Long, User> userMap = userService.getByIds(senderIdsList);

            allMessages.forEach((Message oldCommonMessage) -> {
                User sender = userMap.get(oldCommonMessage.getSenderId());
                sendClientMessage(oldCommonMessage, sender, oldCommonMessage instanceof PrivateMessage);
            });
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        }
    }

    public void systemSendMessage(String message) {
        try {
            out.writeUTF(messageService.systemMessageFormat(message));
        } catch (IOException e) {
            logger.error(Messages.ERROR, e);
            e.printStackTrace();
        }
    }

    private void sendStatus () {
        StringBuilder statusBuilder = new StringBuilder();
        statusBuilder.append(String.format(
                Messages.STATUS_TITLE,
                room != null ? room.getName() : Messages.MAIN_ROOM_NAME
        ));
        statusBuilder.append(String.format(Messages.STATUS_NICK, user.getNickname()));
        statusBuilder.append(String.format(
                Messages.STATUS_LIST_TITLE,
                room != null ? room.getName() : Messages.MAIN_ROOM_NAME
        ));

        server.getClients().forEach(client -> {
            if (room != null && client.getRoom() != null && client.getRoom().getId().equals(this.room.getId())) {
                statusBuilder.append(String.format(Messages.IN_LIST, client.getUser().getNickname()));
            }

            if (room == null && client.getRoom() == null) {
                statusBuilder.append(String.format(Messages.IN_LIST, client.getUser().getNickname()));
            }
        });

        systemSendMessage(statusBuilder.toString());
    }

    private void banUser(String... params) {
        if (!isAdmin()) {
            systemSendMessage(Messages.NO_COMMAND_ERROR);
            return;
        }

        if (params.length != 3) {
            systemSendMessage(Messages.WRONG_BAN_FORMAT_ERROR);
            return;
        }

        try {
            User willBeBannedUser = userService.getUserByNickName(params[0]);

            Timestamp time = "0".equals(params[2]) ? null : new Timestamp(System.currentTimeMillis() + Long.parseLong(params[2]) * 60 * 1000);
            User bannedUser = adminService.banUser(
                    user,
                    willBeBannedUser,
                    params[1],
                    time
            );

            ClientHandler handlerOfBannedUser = server.getClients().stream()
                    .filter(client -> client.getUser().getId().equals(bannedUser.getId()))
                    .findFirst()
                    .orElse(null);

            if (handlerOfBannedUser != null) {
                handlerOfBannedUser.setUser(bannedUser);

                handlerOfBannedUser.systemSendMessage(Messages.BLOCKED);
            }

            systemSendMessage(String.format(Messages.SUCCESS_BLOCKED, bannedUser.getNickname()));
        } catch (NumberFormatException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(Messages.WRONG_BAN_MINUTES_ERROR);
        } catch (IllegalArgumentException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(e.getMessage());
        } catch (NullPointerException e) {
            logger.error(Messages.ERROR, e);
            systemSendMessage(Messages.USER_NOT_FOUND);
        }
    }

    private boolean isAdmin () {
        return Objects.equals(user.getRoleId(), Role.ADMIN.getId());
    }

    private void checkInactivity() {
        if (System.currentTimeMillis() - user.getLastActive().getTime() > INACTIVITY_LIMIT) {
            logger.info(Messages.CLIENT_DISCONNECTED_INACTIVITY);
            logger.info(Messages.CLIENT_DISCONNECTED_INACTIVITY_MESSAGE);
            disconnect();
            inactivityChecker.shutdown();
        }
    }

    private void updateLastActivityTime() {
        user = userService.updateLastActive(user);
    }

    private void shutdown() {
        if (!isAdmin()) {
            systemSendMessage(Messages.NO_COMMAND_ERROR);
            return;
        }
        server.shutdown();
    }
}
