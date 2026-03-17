package ru.telegram_bot.exception;

public class ClientInteractionException extends VacancyBotException {
    public ClientInteractionException(String message) {
        super(message);
    }

    public ClientInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}