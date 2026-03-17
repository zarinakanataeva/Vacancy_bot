package ru.telegram_bot.exception;

public class NotificationSchedulingException extends VacancyBotException {
    public NotificationSchedulingException(String message) {
        super(message);
    }

    public NotificationSchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}