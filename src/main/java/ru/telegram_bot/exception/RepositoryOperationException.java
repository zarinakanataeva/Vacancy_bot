package ru.telegram_bot.exception;

public class RepositoryOperationException extends VacancyBotException {
    public RepositoryOperationException(String message) {
        super(message);
    }

    public RepositoryOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}