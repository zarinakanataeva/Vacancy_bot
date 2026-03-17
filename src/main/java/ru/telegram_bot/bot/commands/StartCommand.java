package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;
import ru.telegram_bot.util.KeyboardFactory;

public class StartCommand implements BotCommand {
    @Override
    public boolean canHandle(String text) {
        return text.startsWith("/start") || text.equals("🚀 Старт");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("""
                        \uD83D\uDC4B Добро пожаловать!
                                                
                        Вы успешно зарегистрированы в Vacancy Tracker Bot!
                        Используется часовой пояс UTC +3, для изменения введите часовой пояс в формате 'UTC +N'
                                                
                        Настройте фильтры поиска кнопками.
                                                
                        """)
                .replyMarkup(KeyboardFactory.createReplyKeyboard())
                .build();
    }
}