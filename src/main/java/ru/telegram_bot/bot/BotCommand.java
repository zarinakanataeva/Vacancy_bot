package ru.telegram_bot.bot;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.model.BotUser;

public interface BotCommand {
    boolean canHandle(String text);
    BotApiMethod<?> execute(Update update, BotUser user);
}