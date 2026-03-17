package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;

public class SalaryCommand implements BotCommand {

    @Override
    public boolean canHandle(String text) {
        return text.startsWith("/salary");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        String val = update.getMessage().getText().replaceAll("[^0-9]", "");
        if (!val.isEmpty()) {
            user.setMinSalary(Integer.parseInt(val));
            return new SendMessage(user.getChatId().toString(), "✅ Зарплата установлена: " + val + " руб.");
        }
        return new SendMessage(user.getChatId().toString(), "❌ Введите числовое значение зарплаты.");
    }
}