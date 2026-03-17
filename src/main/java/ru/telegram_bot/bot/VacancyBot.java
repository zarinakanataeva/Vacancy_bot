
package ru.telegram_bot.bot;

import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.telegram_bot.exception.VacancyBotException;
import ru.telegram_bot.model.BotUser;
import ru.telegram_bot.repository.UserRepository;
import ru.telegram_bot.service.NotificationService;
import ru.telegram_bot.util.KeyboardFactory;

public class VacancyBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient client;
    private final UserRepository repository;
    private final NotificationService notificationService;
    private final CommandDispatcher dispatcher;

    public VacancyBot(TelegramClient client, UserRepository repository,
                      NotificationService notificationService, CommandDispatcher dispatcher) {
        this.client = client;
        this.repository = repository;
        this.notificationService = notificationService;
        this.dispatcher = dispatcher;
    }


    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleText(update);
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (VacancyBotException e) {
            sendErrorMessage(update, e.getMessage());
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage(update, "Произошла ошибка. Пожалуйста, попробуйте позже.");
        }
    }

    private void handleText(Update update) {
        long userId = update.getMessage().getFrom().getId();

        BotUser user = repository.findById(userId).orElse(new BotUser());
        user.setId(userId);
        user.setChatId(update.getMessage().getChatId());

        BotApiMethod<?> result = dispatcher.dispatch(update, user);

        try {
            if (result != null) {
                client.execute(result);
            }

            String text = update.getMessage().getText();
            if (text.contains("/start") || text.equals("🚀 Старт") || text.equals("/menu")) {
                sendInlineSettingsMenu(user.getChatId(), user);
            }
        } catch (Exception e) {
            throw new VacancyBotException("Ошибка взаимодействия с Telegram API при обработке текста: " + e.getMessage(), e);
        }

        repository.save(user);
    }

    private void handleCallback(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();

        BotUser user = repository.findById(userId).orElse(new BotUser());
        user.setChatId(chatId);

        try {
            if (data.equals("menu_main")) {
                editMenu(chatId, msgId, user);
            } else if (data.equals("menu_notify")) {
                editMsg(chatId, msgId, "⏰ Выберите время или введите: /notify ЧЧ ММ", KeyboardFactory.createNotifyKeyboard());

            } else if (data.startsWith("time_set_")) {
                String time = data.split("_")[2] + ":" + data.split("_")[3];
                user.setNotifyTime(time);
                editMenu(chatId, msgId, user);
                sendMsg(chatId, "✅ Время установлено на: " + time);
                sendInlineSettingsMenu(chatId, user);

            } else if (data.equals("menu_region")) {
                editMsg(chatId, msgId, "Выберите регион:", KeyboardFactory.createRegionKeyboard(user.getCurrentRegionPage()));

            } else if (data.startsWith("reg_set_")) {
                String[] p = data.split("_");
                user.setRegionId(p[2]);
                user.setRegionName(p[3]);
                editMenu(chatId, msgId, user);

            } else if (data.equals("reg_next")) {
                user.setCurrentRegionPage(user.getCurrentRegionPage() + 1);
                editMsg(chatId, msgId, "Выберите регион:", KeyboardFactory.createRegionKeyboard(user.getCurrentRegionPage()));

            } else if (data.equals("reg_prev")) {
                user.setCurrentRegionPage(Math.max(0, user.getCurrentRegionPage() - 1));
                editMsg(chatId, msgId, "Выберите регион:", KeyboardFactory.createRegionKeyboard(user.getCurrentRegionPage()));

            } else if (data.equals("menu_exp")) {
                editMsg(chatId, msgId, "Выберите опыт:", KeyboardFactory.createExperienceKeyboard());

            } else if (data.startsWith("exp_set_")) {
                user.setExperience(data.split("_")[2]);
                editMenu(chatId, msgId, user);

            } else if (data.equals("menu_salary")) {
                editMsg(chatId, msgId, "Выберите минимальную зарплату:", KeyboardFactory.createSalaryKeyboard());

            } else if (data.startsWith("sal_set_")) {
                user.setMinSalary(Integer.parseInt(data.split("_")[2]));
                editMenu(chatId, msgId, user);

            } else if (data.equals("menu_keyword")) {
                editMsg(chatId, msgId, "Выберите профессию или введите /keyword [текст]:", KeyboardFactory.createKeywordKeyboard());

            } else if (data.startsWith("key_set_")) {
                user.setKeyword(data.split("_")[2]);
                editMenu(chatId, msgId, user);

            } else if (data.equals("yes_sure")) {
                notificationService.cancel(userId);
                repository.delete(userId);
                editMsg(chatId, msgId, "🗑 Ваши данные успешно удалены. Поиск остановлен.", null);
                return;

            } else if (data.equals("not_sure")) {
                editMsg(chatId, msgId, "✅ Удаление отменено. Ваши фильтры сохранены.", KeyboardFactory.createMainMenu(user));

            } else if (data.equals("btn_ready")) {
                if (user.getNotifyTime() == null) {
                    sendMsg(chatId, "⚠️ Сначала укажите время уведомления (кнопка 'Время' или /notify)");

                } else {
                    notificationService.schedule(user);
                    sendMsg(chatId, "🚀 Поиск запущен! Ваши фильтры: \n\n" +
                            "📍Регион: " + (user.getRegionName() != null ? user.getRegionName() : "Не задан") + "\n" +
                            "⏳Опыт: " + KeyboardFactory.formatExperience(user.getExperience()) + "\n" +
                            "💰Мин. ЗП: " + (user.getMinSalary() != null ? user.getMinSalary() + " руб." : "Не задана") + "\n" +
                            "🔍Ключ. слово: " + (user.getKeyword() != null ? user.getKeyword() : "Не задано") + "\n" +
                            "🔔Время: " + user.getNotifyTime());
                }
            }
        } catch (Exception e) {
            throw new VacancyBotException("Ошибка взаимодействия с Telegram API при обработке коллбэка: " + e.getMessage(), e);
        }

        repository.save(user);
    }


    private void sendErrorMessage(Update update, String errorText) {
        Long chatId = null;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        if (chatId != null) {
            try {
                client.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("❌ " + errorText)
                        .replyMarkup(KeyboardFactory.createReplyKeyboard())
                        .build());
            } catch (Exception ignored) {
                System.err.println("Не удалось отправить сообщение об ошибке пользователю: " + errorText);
            }
        } else {
            System.err.println("Не удалось определить chatId для отправки сообщения об ошибке: " + errorText);
        }
    }

    private void sendInlineSettingsMenu(long chatId, BotUser user) {
        try {
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("📋 Ваши текущие фильтры:")
                    .replyMarkup(KeyboardFactory.createMainMenu(user))
                    .build());
        } catch (Exception e) {
            throw new VacancyBotException("Ошибка при отправке меню: " + e.getMessage(), e);
        }
    }

    private void editMenu(long chatId, int msgId, BotUser user) {
        editMsg(chatId, msgId, "📋 Ваши фильтры:", KeyboardFactory.createMainMenu(user));
    }

    private void editMsg(long chatId, int msgId, String text, InlineKeyboardMarkup kb) {
        try {
            client.execute(EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(msgId)
                    .text(text)
                    .replyMarkup(kb)
                    .build());
        } catch (Exception e) {
            throw new VacancyBotException("Ошибка при редактировании сообщения: " + e.getMessage(), e);
        }
    }

    private void sendMsg(long chatId, String text) {
        try {
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(KeyboardFactory.createReplyKeyboard())
                    .build());
        } catch (Exception e) {
            throw new VacancyBotException("Ошибка при отправке сообщения: " + e.getMessage(), e);
        }
    }
}
