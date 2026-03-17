package ru.telegram_bot.exception;

public class VacancyBotException extends RuntimeException {
    public VacancyBotException(String message) {
        super(message);
    }

    public VacancyBotException(String message, Throwable cause) {
        super(message, cause);
    }
}