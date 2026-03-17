package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

public class KeywordCommandTest {

    private final KeywordCommand command = new KeywordCommand();
    private final long userId = 1L;
    private final long chatId = 123L;

    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /keyword")
    void canHandleShouldReturnTrueForKeywordCommand() {
        assertTrue(command.canHandle("/keyword"));
        assertTrue(command.canHandle("/keyword Python"));
        assertFalse(command.canHandle("keyword"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен установить ключевое слово")
    void executeShouldSetKeyword() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/keyword Java developer");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("Java developer", user.getKeyword());
        assertEquals("✅ Поиск по слову: Java developer", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен выдать ошибку если ключевое слово не указано")
    void executeShouldThrowErrorIfKeywordNotProvided() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/keyword ");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertNull(user.getKeyword());
        assertEquals("❌ Введите ключевое слово.", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }
}