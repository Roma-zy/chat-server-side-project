package ru.otus.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.chat.entities.CommonMessage;
import ru.otus.chat.entities.Message;
import ru.otus.chat.entities.User;
import ru.otus.chat.services.RoomService;
import ru.otus.chat.services.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private final Logger logger = LogManager.getLogger();
    private final int port;
    private final List<ClientHandler> clients;

    private final UserService userService = new UserService();
    private final RoomService roomService = new RoomService();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Пул для планировщика

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info(Messages.SERVER_STARTED, port);

            startUnbanScheduler();
            startClearRoomsScheduler();

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            logger.error(Messages.ERROR, e);
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void broadcastMessage(CommonMessage commonMessage, User sender) {
        for (ClientHandler client : clients) {
            client.sendClientMessage(
                commonMessage,
                sender,
                false
            );
        }
    }

    public synchronized void sendByUserIdMessage(Long id, User sender, Message message, Boolean isPrivate) {
        for (ClientHandler client : clients) {
            if(Objects.equals(client.getUser().getId(), id)) {
                client.sendClientMessage(
                        message,
                        sender,
                        isPrivate
                );
                return;
            }
        }
    }

    public synchronized void kick(User user, ClientHandler clientHandler) {
        for (int i = 0; i < clients.size(); i++) {
            if (Objects.equals(clients.get(i).getUser().getId(), user.getId())) {
                ClientHandler client = clients.get(i);
                clients.remove(i);
                client.systemSendMessage(Messages.YOU_ARE_DELETED);
                client.exit();

                clientHandler.systemSendMessage(String.format(Messages.USER_DELETED, user.getNickname()));

                return;
            }
        }

        clientHandler.systemSendMessage(String.format(Messages.USER_OFFLINE, user.getNickname()));
    }

    public synchronized void shutdown() {
        scheduler.shutdown();
        for (ClientHandler client : new ArrayList<>(clients)) {
            client.systemSendMessage(Messages.SERVER_OFF_MESSAGE);
            client.exit();
        }
        System.exit(0);
    }

    private void unbanExpiredUsers() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        userService.unbanUsers(now);
    }

    private void startUnbanScheduler() {
        scheduler.scheduleAtFixedRate(this::unbanExpiredUsers, 0, 1, TimeUnit.MINUTES);
    }

    private void clearRooms() {
        roomService.deleteInactiveRooms();
    }

    private void startClearRoomsScheduler() {
        scheduler.scheduleAtFixedRate(this::clearRooms, 0, 1, TimeUnit.DAYS);
    }
}
