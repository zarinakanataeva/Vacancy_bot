package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;
import ru.telegram_bot.util.RegionProvider;

public class RegionCommand implements BotCommand {
    private final RegionProvider reg = new RegionProvider();

    @Override
    public boolean canHandle(String text) {
        return text.startsWith("/region");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        String code = update.getMessage().getText().replace("/region", "").trim();
        String val = reg.getRegionByNumber(code);
        String response = val != null && !val.isEmpty() ? "✅ Регион установлен: " + val : "❌ Код региона не найден.";

        if (val != null) {
            user.setRegionId(code);
            user.setRegionName(val);
        }

        return new SendMessage(user.getChatId().toString(), response);
    }
}