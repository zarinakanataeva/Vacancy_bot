package ru.telegram_bot.bot;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.bot.commands.*;
import ru.telegram_bot.model.BotUser;

import java.util.ArrayList;
import java.util.List;

public class CommandDispatcher {
    private final List<BotCommand> commands = new ArrayList<>();

    public CommandDispatcher() {
        commands.add(new StartCommand());
        commands.add(new StopCommand());
        commands.add(new MenuCommand());
        commands.add(new RegionCommand());
        commands.add(new SalaryCommand());
        commands.add(new ExperienceCommand());
        commands.add(new KeywordCommand());
        commands.add(new NotifyCommand());
        commands.add(new UtcCommand());
    }

    public BotApiMethod<?> dispatch(Update update, BotUser user) {
        String text = update.getMessage().getText().trim();
        for (BotCommand command : commands) {
            if (command.canHandle(text)) {
                return command.execute(update, user);
            }
        }
        return new SendMessage(user.getChatId().toString(),
                "❓ Неизвестная команда. \n Введите /menu или нажмите 🚀 Старт.");
    }
}