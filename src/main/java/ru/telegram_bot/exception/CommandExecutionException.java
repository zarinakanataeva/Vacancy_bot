package ru.telegram_bot.exception;

public class CommandExecutionException extends VacancyBotException {
    public CommandExecutionException(String message) {
        super(message);
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}