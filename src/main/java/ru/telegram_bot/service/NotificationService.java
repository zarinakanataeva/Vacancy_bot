package ru.telegram_bot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.telegram_bot.client.TrudVsemClient;
import ru.telegram_bot.exception.ClientInteractionException;
import ru.telegram_bot.exception.NotificationSchedulingException;
import ru.telegram_bot.model.BotUser;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class NotificationService {
    private final ScheduledExecutorService scheduler;
    private final Map<Long, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final TrudVsemClient apiClient;
    private final Clock clock;


    public NotificationService(TelegramClient telegramClient, TrudVsemClient apiClient) {
        this(telegramClient, apiClient, Clock.systemDefaultZone(), Executors.newScheduledThreadPool(10));
    }

    public NotificationService(TelegramClient telegramClient, TrudVsemClient apiClient, Clock clock, ScheduledExecutorService scheduler) {
        this.telegramClient = telegramClient;
        this.apiClient = apiClient;
        this.clock = clock;
        this.scheduler = scheduler;
    }

    public void schedule(BotUser user) {
        cancel(user.getId());
        if (user.getNotifyTime() == null || user.getChatId() == null) {
            return;
        }

        try {
            String[] timeParts = user.getNotifyTime().split(":");
            if (timeParts.length != 2) {
                throw new NotificationSchedulingException("Неверный формат времени уведомления: " + user.getNotifyTime());
            }
            LocalTime targetTime = LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
            LocalTime now = LocalTime.now(clock).plusHours(user.getUtcOffset());

            long initialDelay = Duration.between(now, targetTime).toSeconds();
            if (initialDelay < 0) {
                initialDelay += 24 * 60 * 60;
            }

            Runnable notificationTask = () -> {
                try {
                    List<String> jobs = apiClient.searchVacancies(user);
                    if (jobs.isEmpty()) {
                        System.out.println("Нет новых вакансий для пользователя " + user.getId());
                        return;
                    }

                    String text = "🔔 Новые вакансии для вас:\n\n" + String.join("\n\n", jobs);
                    telegramClient.execute(SendMessage.builder().chatId(user.getChatId()).text(text).build());
                } catch (ClientInteractionException e) {
                    System.err.println("Ошибка при поиске вакансий для пользователя " + user.getId() + ": " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Ошибка отправки уведомления для пользователя " + user.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            };

            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(notificationTask, initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
            tasks.put(user.getId(), future);
        } catch (NumberFormatException e) {
            throw new NotificationSchedulingException("Неверное числовое значение в формате времени уведомления для пользователя "
                    + user.getId() + ": " + user.getNotifyTime(), e);
        } catch (NotificationSchedulingException e) {
            throw e;
        } catch (Exception e) {
            throw new NotificationSchedulingException("Не удалось запланировать задачу уведомления для пользователя "
                    + user.getId() + ": " + e.getMessage(), e);
        }
    }

    public void cancel(Long userId) {
        ScheduledFuture<?> f = tasks.remove(userId);
        if (f != null) {
            f.cancel(true);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}