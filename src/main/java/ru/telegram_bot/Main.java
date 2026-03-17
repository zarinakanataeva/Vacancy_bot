package ru.telegram_bot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import ru.telegram_bot.bot.CommandDispatcher;
import ru.telegram_bot.bot.VacancyBot;
import ru.telegram_bot.client.TrudVsemClient;
import ru.telegram_bot.exception.ConfigException;
import ru.telegram_bot.exception.VacancyBotException;
import ru.telegram_bot.repository.UserRepository;
import ru.telegram_bot.service.NotificationService;
import ru.telegram_bot.util.ConfigLoader;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Загрузка конфигурации...");

            ConfigLoader config = new ConfigLoader();
            String token = config.getToken();
            String botName = config.getBotName();

            UserRepository repository = new UserRepository();
            TrudVsemClient apiClient = new TrudVsemClient();
            OkHttpTelegramClient telegramClient = new OkHttpTelegramClient(token);
            NotificationService notificationService = new NotificationService(telegramClient, apiClient);
            CommandDispatcher dispatcher = new CommandDispatcher();
            repository.findAll().forEach(notificationService::schedule);

            try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
                botsApplication.registerBot(token, new VacancyBot(telegramClient, repository, notificationService, dispatcher));

                System.out.println("Бот [" + botName + "] успешно запущен и готов к работе.");

                Thread.currentThread().join();
            }

        } catch (ConfigException e) {
            System.err.println("ОШИБКА КОНФИГУРАЦИИ: " + e.getMessage());
            System.err.println("Проверьте файл src/main/resources/config.properties");
            System.exit(1);
        } catch (VacancyBotException e) {
            System.err.println("ОШИБКА ПРИЛОЖЕНИЯ: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ОШИБКА В MAIN: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}