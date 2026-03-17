package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class StartCommandTest {

    private final StartCommand command = new StartCommand();

    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /start")
    void canHandleShouldReturnTrueForStartCommand() {
        assertTrue(command.canHandle("/start"));
        assertTrue(command.canHandle("🚀 Старт"));
        assertFalse(command.canHandle("/stop"));
    }

    @Test
    @DisplayName("execute: Должен отправить приветственное сообщение с ReplyKeyboard")
    void executeShouldSendWelcomeMessageWithReplyKeyboard() {
        long userId = 1L;
        long chatId = 123L;

        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/start");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(String.valueOf(chatId), response.getChatId());
        assertEquals("""
                👋 Добро пожаловать!
                                
                Вы успешно зарегистрированы в Vacancy Tracker Bot!
                Используется часовой пояс UTC +3, для изменения введите часовой пояс в формате 'UTC +N'
                                
                Настройте фильтры поиска кнопками.
                                
                """, response.getText());
        assertNotNull(response.getReplyMarkup());
        assertInstanceOf(ReplyKeyboardMarkup.class, response.getReplyMarkup());
    }
}



