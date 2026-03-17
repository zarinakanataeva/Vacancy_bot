package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;

public class ExperienceCommand implements BotCommand {
    @Override
    public boolean canHandle(String text) {
        return text.startsWith("/experience");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        String val = update.getMessage().getText().replaceAll("[^0-9]", "");
        if (!val.isEmpty()) {
            int years = Integer.parseInt(val);
            String apiExp = (years == 0) ? "0" : (years <= 3) ? "1" : (years <= 6) ? "3" : "6";
            user.setExperience(apiExp);
            return new SendMessage(user.getChatId().toString(), "✅ Опыт установлен: " + val + " лет.");
        }
        return new SendMessage(user.getChatId().toString(), "❌ Укажите количество лет опыта цифрой.");
    }
}