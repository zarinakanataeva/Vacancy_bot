package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

class StopCommandTest {

    private final StopCommand command = new StopCommand();

    @Test
    @DisplayName("canHandle: Должен возвращать true для /stop")
    void canHandleShouldReturnTrueForStopCommands() {
        assertTrue(command.canHandle("/stop"));
        assertTrue(command.canHandle("🛑 Стоп"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен отправить сообщение с подтверждением и InlineKeyboard")
    void executeShouldSendConfirmationMessageWithInlineKeyboard() {
        long userId = 1L;
        long chatId = 123L;

        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/stop");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(String.valueOf(chatId), response.getChatId());
        assertEquals("⚠️ Вы уверены, что хотите сбросить все фильтры и остановить уведомления?", response.getText());
        assertNotNull(response.getReplyMarkup());
        assertInstanceOf(InlineKeyboardMarkup.class, response.getReplyMarkup());
    }
}