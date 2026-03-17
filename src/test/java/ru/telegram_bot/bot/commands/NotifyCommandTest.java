package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.exception.CommandExecutionException;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

public class NotifyCommandTest {

    private final NotifyCommand command = new NotifyCommand();
    private final long userId = 1L;
    private final long chatId = 123L;

    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /notify")
    void canHandleShouldReturnTrueForNotifyCommand() {
        assertTrue(command.canHandle("/notify"));
        assertTrue(command.canHandle("/notify 12 00"));
        assertFalse(command.canHandle("notify"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен установить время уведомления в корректном формате")
    void executeShouldSetNotifyTimeCorrectly() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/notify 09 30");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("09:30", user.getNotifyTime());
        assertEquals("✅ Время уведомления: 09:30", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен бросить исключение при неверном количестве аргументов")
    void executeShouldThrowExceptionForInvalidArgumentCount() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/notify 09");

        CommandExecutionException thrown = assertThrows(CommandExecutionException.class, () -> command.execute(update, user));
        assertTrue(thrown.getMessage().contains("Неверный формат времени. Ожидается: ЧЧ ММ"));
        assertNull(user.getNotifyTime());
    }

    @Test
    @DisplayName("execute: Должен бросить исключение при неверном формате чисел")
    void executeShouldThrowExceptionForInvalidNumberFormat() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/notify ab 30");

        CommandExecutionException thrown = assertThrows(CommandExecutionException.class, () -> command.execute(update, user));
        assertTrue(thrown.getMessage().contains("Неверный формат чисел для часов или минут."));
        assertNull(user.getNotifyTime());
    }

    @Test
    @DisplayName("execute: Должен бросить исключение при неверном диапазоне часов")
    void executeShouldThrowExceptionForInvalidHourRange() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/notify 25 30");

        CommandExecutionException thrown = assertThrows(CommandExecutionException.class, () -> command.execute(update, user));
        assertTrue(thrown.getMessage().contains("Неверный диапазон для часов (0-23) или минут (0-59)."));
        assertNull(user.getNotifyTime());
    }

    @Test
    @DisplayName("execute: Должен бросить исключение при неверном диапазоне минут")
    void executeShouldThrowExceptionForInvalidMinuteRange() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/notify 10 65");

        CommandExecutionException thrown = assertThrows(CommandExecutionException.class, () -> command.execute(update, user));
        assertTrue(thrown.getMessage().contains("Неверный диапазон для часов (0-23) или минут (0-59)."));
        assertNull(user.getNotifyTime());
    }
}
