package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.model.BotUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtcCommand implements BotCommand {
    @Override
    public boolean canHandle(String text) {
        return text.toUpperCase().startsWith("UTC");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        String text = update.getMessage().getText().toUpperCase();
        Pattern pattern = Pattern.compile("UTC\\s*([+-])\\s*(\\d+)");
        Matcher matcher = pattern.matcher(text);

        String response;
        if (matcher.find()) {
            String sign = matcher.group(1);
            int hours = Integer.parseInt(matcher.group(2));
            int offset = sign.equals("-") ? -hours : hours;
            user.setUtcOffset(offset);
            response = "✅ Часовой пояс установлен: UTC " + sign + hours;
        } else {
            response = "❌ Неверный формат! Введите например: UTC +2 или UTC -3";
        }
        return new SendMessage(user.getChatId().toString(), response);
    }
}