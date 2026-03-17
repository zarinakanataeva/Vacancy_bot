package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;

public class KeywordCommand implements BotCommand {
    @Override
    public boolean canHandle(String text) {
        return text.startsWith("/keyword");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        String val = update.getMessage().getText().replace("/keyword", "").trim();
        if (!val.isEmpty()) {
            user.setKeyword(val);
            return new SendMessage(user.getChatId().toString(), "✅ Поиск по слову: " + val);
        }
        return new SendMessage(user.getChatId().toString(), "❌ Введите ключевое слово.");
    }
}