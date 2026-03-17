package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;
import ru.telegram_bot.util.KeyboardFactory;

import static org.junit.jupiter.api.Assertions.*;

class MenuCommandTest {

    private final MenuCommand command = new MenuCommand();

    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /menu")
    void canHandleShouldReturnTrueForMenuCommand() {
        assertTrue(command.canHandle("/menu"));
        assertFalse(command.canHandle("/start"));
        assertFalse(command.canHandle("menu"));
    }
}