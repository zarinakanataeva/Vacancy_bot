package ru.telegram_bot.exception;

public class ConfigException extends VacancyBotException {
    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}