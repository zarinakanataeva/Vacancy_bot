package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

class UtcCommandTest {

    private final UtcCommand command = new UtcCommand();
    private final long userId = 1L;
    private final long chatId = 123L;

    @Test
    @DisplayName("canHandle: Должен возвращать true для команд начинающихся с UTC")
    void canHandleShouldReturnTrueForUtcCommands() {
        assertTrue(command.canHandle("UTC +3"));
        assertTrue(command.canHandle("utc -5"));
        assertFalse(command.canHandle("UT C"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен установить положительное смещение UTC")
    void executeShouldSetPositiveUtcOffset() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "UTC +3");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(3, user.getUtcOffset());
        assertEquals("✅ Часовой пояс установлен: UTC +3", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен установить отрицательное смещение UTC")
    void executeShouldSetNegativeUtcOffset() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "UTC -5");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(-5, user.getUtcOffset());
        assertEquals("✅ Часовой пояс установлен: UTC -5", response.getText());
    }

    @Test
    @DisplayName("execute: Должен работать с разным форматированием пробелов")
    void executeShouldHandleVariousSpaceFormatting() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "UTC+2");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(2, user.getUtcOffset());
        assertEquals("✅ Часовой пояс установлен: UTC +2", response.getText());
    }

    @Test
    @DisplayName("execute: Должен сообщить об ошибке при неверном формате")
    void executeShouldReturnErrorForInvalidFormat() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "UTC three");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(3, user.getUtcOffset());
        assertEquals("❌ Неверный формат! Введите например: UTC +2 или UTC -3", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен сообщить об ошибке при отсутствии числа")
    void executeShouldReturnErrorIfNoNumber() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "UTC +");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(3, user.getUtcOffset());
        assertEquals("❌ Неверный формат! Введите например: UTC +2 или UTC -3", response.getText());
    }
}