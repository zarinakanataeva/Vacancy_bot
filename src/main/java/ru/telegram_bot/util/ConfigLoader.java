package ru.telegram_bot.util;


import ru.telegram_bot.exception.ConfigException;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private final Properties properties = new Properties();

    public ConfigLoader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new ConfigException("Файл config.properties не найден в папке resources!");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new ConfigException("Ошибка при загрузке конфигурации: " + e.getMessage(), e);
        }
    }

    public String getToken() {
        String token = properties.getProperty("bot.token");
        if (token == null || token.isBlank()) {
            throw new ConfigException("Параметр 'bot.token' отсутствует или пуст в config.properties");
        }
        return token;
    }

    public String getBotName() {
        String name = properties.getProperty("bot.name");
        if (name == null || name.isBlank()) {
            throw new ConfigException("Параметр 'bot.name' отсутствует или пуст в config.properties");
        }
        return name;
    }
}