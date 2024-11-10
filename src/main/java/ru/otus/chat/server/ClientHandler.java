package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                System.out.println("Клиент подключился ");
                //цикл аутентификации
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.startsWith("/exit")) {
                            sendMessage("/exitok");
                            break;
                        }
                        // /auth login password
                        if (message.startsWith("/auth ")) {
                            String[] elements = message.split(" ");
                            if (elements.length != 3) {
                                sendMessage("Неверный формат команды /auth ");
                                continue;
                            }
                            if (server.getAuthenticatedProvider()
                                    .authenticate(this,elements[1], elements[2])){
                                break;
                            }
                            continue;
                        }
                        // /reg login password username
                        if (message.startsWith("/reg ")) {
                            String[] elements = message.split(" ");
                            if (elements.length != 4) {
                                sendMessage("Неверный формат команды /reg ");
                                continue;
                            }
                            if (server.getAuthenticatedProvider()
                                    .registration(this,elements[1], elements[2], elements[3])){
                                break;
                            }
                            continue;
                        }
                    }
                    sendMessage("Перед работой необходимо пройти аутентификацию командой " +
                            "/auth login password или регистрацию командой /reg login password username");
                }
                System.out.println("Клиент "+ username+ " успешно прошел аутентификацию");

                while (true) {
                    boolean isBreak = false;
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        String[] messageArray = message.trim().split(" ");
                        String command = messageArray[0];
                        switch (command) {
                            case "/exit":
                                sendMessage("exit");
                                isBreak = true;
                                break;
                            case "/w":
                                if (messageArray.length <= 2) {
                                    sendMessage("Введите сообщеение!");
                                    break;
                                }
                                String messageWithoutCommand = String.join(" ", Arrays.copyOfRange(messageArray, 2, messageArray.length));

                                sendMessage("[private_to]" + messageArray[1] + " : " + messageWithoutCommand);
                                server.sendByUserNameMessage(
                                    messageArray[1],
                                    this,
                                    username + "[private_from] : " + messageWithoutCommand
                                );
                                break;
                            case "/kick":
                                if (messageArray.length <= 1) {
                                    sendMessage("Введите имя пользователя!");
                                    break;
                                }

                                server.kickByUserName(messageArray[1], this);
                                break;
                            default:
                                sendMessage("нет такой команды!");
                                break;
                        }
                    } else {
                        server.broadcastMessage(username + " : " + message);
                    }

                    if (isBreak) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
