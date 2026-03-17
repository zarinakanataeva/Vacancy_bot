package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;
import ru.telegram_bot.util.KeyboardFactory;

public class StopCommand implements BotCommand {
    @Override
    public boolean canHandle(String text) {
        return text.equals("/stop") || text.equals("🛑 Стоп");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("⚠️ Вы уверены, что хотите сбросить все фильтры и остановить уведомления?")
                .replyMarkup(KeyboardFactory.createAreYouSureKeyBoard())
                .build();
    }
}