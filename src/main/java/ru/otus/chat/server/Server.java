package ru.otus.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticatedProvider authenticatedProvider;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        authenticatedProvider = new InDBProvider(this);
        authenticatedProvider.initialize();
    }

    public AuthenticatedProvider getAuthenticatedProvider() {
        return authenticatedProvider;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendByUserNameMessage(String userName, ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            if(Objects.equals(client.getUsername(), userName)) {
                client.sendMessage(message);
                return;
            }
        }

        sender.sendMessage("Такого пользователя нет!");
    }

    public synchronized void kickByUserName(String userName, ClientHandler clientHandler) {
        Role role = authenticatedProvider.getRoleByUserName(clientHandler.getUsername());
        if (role != Role.ADMIN) {
            clientHandler.sendMessage("[system] нет прав!");
        }
        for (int i = 0; i < clients.size(); i++) {
            if (Objects.equals(clients.get(i).getUsername(), userName)) {
                ClientHandler client = clients.get(i);
                clients.remove(i);
                client.sendMessage("[system] Вас удалили из чата!");
                client.disconnect();

                clientHandler.sendMessage("[system] Пользователь " + userName + " удален");

                return;
            }
        }
    }
}
