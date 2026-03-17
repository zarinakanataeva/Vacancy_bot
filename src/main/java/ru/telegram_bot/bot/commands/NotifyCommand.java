package ru.telegram_bot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.BotCommand;
import ru.telegram_bot.exception.CommandExecutionException;
import ru.telegram_bot.model.BotUser;

public class NotifyCommand implements BotCommand {
    @Override
    public boolean canHandle(String text) {
        return text.startsWith("/notify");
    }

    @Override
    public BotApiMethod<?> execute(Update update, BotUser user) {
        try {
            String[] p = update.getMessage().getText().split("\\s+");
            if (p.length != 3) {
                throw new CommandExecutionException("Неверный формат времени. Ожидается: ЧЧ ММ");
            }
            int hour = Integer.parseInt(p[1]);
            int minute = Integer.parseInt(p[2]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new CommandExecutionException("Неверный диапазон для часов (0-23) или минут (0-59).");
            }
            user.setNotifyTime(String.format("%02d:%02d", hour, minute));
            return new SendMessage(user.getChatId().toString(), "✅ Время уведомления: " + user.getNotifyTime());
        } catch (NumberFormatException e) {
            throw new CommandExecutionException("Неверный формат чисел для часов или минут.", e);
        } catch (CommandExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandExecutionException("Произошла ошибка при установке времени уведомления.", e);
        }
    }
}