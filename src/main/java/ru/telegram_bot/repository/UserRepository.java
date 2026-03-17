package ru.telegram_bot.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.telegram_bot.exception.RepositoryOperationException;
import ru.telegram_bot.model.BotUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String folderPath;

    public UserRepository() {
        this("data/users/");
    }

    public UserRepository(String folderPath) {
        this.folderPath = folderPath;
        File folder = new File(this.folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RepositoryOperationException("Не удалось создать директорию для пользователей: " + this.folderPath);
        }
    }

    public void save(BotUser user) {
        if (user == null || user.getId() == null) {
            throw new RepositoryOperationException("Пользователь или его ID не может быть null для сохранения.");
        }
        try {
            mapper.writeValue(new File(folderPath + user.getId() + ".json"), user);
        } catch (IOException e) {
            throw new RepositoryOperationException("Ошибка сохранения пользователя " + user.getId() + ": " + e.getMessage(), e);
        }
    }

    public Optional<BotUser> findById(Long id) {
        if (id == null) {
            throw new RepositoryOperationException("ID пользователя не может быть null для поиска.");
        }
        File file = new File(folderPath + id + ".json");
        if (!file.exists()) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(file, BotUser.class));
        } catch (IOException e) {
            throw new RepositoryOperationException("Ошибка чтения данных пользователя " + id + " из файла: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        if (id == null) {
            throw new RepositoryOperationException("ID пользователя не может быть null для удаления.");
        }
        File file = new File(folderPath + id + ".json");
        if (file.exists() && !file.delete()) {
            throw new RepositoryOperationException("Не удалось удалить файл пользователя " + id + " по пути: " + file.getAbsolutePath());
        }
    }

    public List<BotUser> findAll() {
        List<BotUser> users = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".json")) {
                    try {
                        users.add(mapper.readValue(f, BotUser.class));
                    } catch (IOException e) {
                        System.err.println("Предупреждение: не удалось прочитать файл пользователя "
                                + f.getName() + " при findAll: " + e.getMessage());
                    }
                }
            }
        }
        return users;
    }
}