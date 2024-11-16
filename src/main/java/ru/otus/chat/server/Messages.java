package ru.otus.chat.server;

public class Messages {
    public static final String ERROR = "Ошибка";

    public static final String SERVER_STARTED = "Сервер запущен на порту: {}.";
    public static final String YOU_ARE_DELETED = "Вас удалили из чата.";
    public static final String USER_DELETED = "Пользователь %s удален.";
    public static final String USER_OFFLINE = "Пользователь %s оффлайн.";
    public static final String SERVER_OFF_MESSAGE = "Сервер завершает работу. Вы будете отключены.";

    public static final String CLIENT_CONNECTION_SUCCESS = "Клиент подключился.";
    public static final String NEED_AUTH_OR_REGISTRATION = "Перед работой необходимо пройти аутентификацию командой /auth username password или регистрацию командой /reg username password nickname.";
    public static final String AUTH_SUCCESS = "Клиент {} успешно прошел аутентификацию.";

    public static final String NO_COMMAND_ERROR = "Нет такой команды.";
    public static final String BLOCKED_ERROR = "Вы заблокированы.";
    public static final String SPAM_ERROR = "Прекратите спамит.";
    public static final String BLOCKED = "Вы заблокированы.";
    public static final String USER_NOT_FOUND = "Пользователь не найден.";
    public static final String CLIENT_DISCONNECTED_INACTIVITY = "Клиент отключен из-за неактивности.";
    public static final String CLIENT_DISCONNECTED_INACTIVITY_MESSAGE = "Вы были отключены за неактивность.";
    public static final String EXIT_OK_MESSAGE = "/exitok";
    public static final String MAIN_ROOM_NAME = "Main";

    public static final String WRONG_AUTH_FORMAT_ERROR = "Неверный формат команды /auth.";
    public static final String WRONG_REG_FORMAT_ERROR = "Неверный формат команды /registration.";
    public static final String WRONG_CREATE_ROOM_FORMAT_ERROR = "Неверный формат команды /create_room.";
    public static final String WRONG_CHECKOUT_FORMAT_ERROR = "Неверный формат команды /checkout.";
    public static final String WRONG_CHANGE_NICK_FORMAT_ERROR = "Неверный формат команды /change_nick.";
    public static final String WRONG_BAN_FORMAT_ERROR = "Неверный формат команды /ban.";
    public static final String WRONG_PRIVATE_MESSAGE_FORMAT_ERROR = "Введите nickName получателя и сообщение.";
    public static final String WRONG_PRIVATE_MESSAGE_FORMAT_NO_MESSAGE_ERROR = "Введите сообщеение.";
    public static final String WRONG_FORMAT_NO_NICK_ERROR = "Введите nickName пользователя.";
    public static final String WRONG_BAN_MINUTES_ERROR = "Передайте число в минутах для установки времени бана.";

    public static final String SUCCESS_ROOM_CREATED = "Комната: %s создана. Пароль: %s.";
    public static final String SUCCESS_ROOM_CHANGED = "Вы перешли в комнату: %s.";
    public static final String SUCCESS_MAIN_ROOM_CHANGED = "Вы перешли в общую комнату.";
    public static final String SUCCESS_CHANGE_NICK_CHANGED = "Ник изменен на: %s.";
    public static final String SUCCESS_BLOCKED = "Пользователь %s заблокирован.";
    public static final String ROOM_LIST = "Список комнат: \n";
    public static final String IN_LIST = "   - %s\n";
    public static final String STATUS_TITLE = "Вы находитесть в комнате %s.\n";
    public static final String STATUS_NICK = "Ваш никнэйм: %s.\n";
    public static final String STATUS_LIST_TITLE = "Список активных пользователей в комнате: %s.\n";

    public static final String MESSAGE_HISTORY_NOT_AVAILABLE = "История сообщений не доступна.";
    public static final String ROOM_NOT_AVAILABLE = "История сообщений не доступна.";
    public static final String ROOM_LIMIT = "Достигнут лимит комнат.";
    public static final String INVALID_USER_ID = "Не корректный userId.";
    public static final String ROOM_NAME_TAKEN = "Имя %s занято! Придумайте другое.";
    public static final String MIN_NAME_LENGTH = "Минимальная длиннв имени 2 символа.";
    public static final String MIN_PASSWORD_LENGTH = "Минимальная длинна password 2 символа.";
    public static final String MIN_NICKNAME_LENGTH = "Минимальная длинна nickname 2 символа.";
    public static final String REGISTRATION_ERROR = "Ошибка регистрации.";
    public static final String AUTH_WRONG_LOGIN_PASSWORD = "Не верный username или password.";
    public static final String CHANGE_NICKNAME_ERROR = "Не удалось сменить ник.";
    public static final String UNKNOWN_ERROR = "Произошла ошибка при обработке запроса. Повторите попытку позже.";
}
