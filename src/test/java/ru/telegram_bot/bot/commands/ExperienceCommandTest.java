package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceCommandTest {

    private final ExperienceCommand command = new ExperienceCommand();
    private final long userId = 1L;
    private final long chatId = 123L;

    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /experience")
    void canHandleShouldReturnTrueForExperienceCommand() {
        assertTrue(command.canHandle("/experience"));
        assertTrue(command.canHandle("/experience 5"));
        assertFalse(command.canHandle("experience"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен установить опыт '0' для 0 лет")
    void executeShouldSetExperienceForZeroYears() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience 0");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("0", user.getExperience());
        assertEquals("✅ Опыт установлен: 0 лет.", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен установить опыт '1' для 1-3 лет")
    void executeShouldSetExperienceFor1To3Years() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience 2");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("1", user.getExperience());
        assertEquals("✅ Опыт установлен: 2 лет.", response.getText());
    }

    @Test
    @DisplayName("execute: Должен установить опыт '3' для 3-6 лет")
    void executeShouldSetExperienceFor4To6Years() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience 5");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("3", user.getExperience());
        assertEquals("✅ Опыт установлен: 5 лет.", response.getText());
    }

    @Test
    @DisplayName("execute: Должен установить опыт '6' для более 6 лет")
    void executeShouldSetExperienceForMoreThan6Years() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience 10");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("6", user.getExperience());
        assertEquals("✅ Опыт установлен: 10 лет.", response.getText());
    }

    @Test
    @DisplayName("execute: Должен сообщить об ошибке, если опыт не указан")
    void executeShouldReturnErrorIfExperienceNotProvided() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertNull(user.getExperience());
        assertEquals("❌ Укажите количество лет опыта цифрой.", response.getText());
    }

    @Test
    @DisplayName("execute: Должен игнорировать нечисловые символы при установке опыта")
    void executeShouldIgnoreNonNumericChars() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience 3 года");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals("1", user.getExperience());
        assertEquals("✅ Опыт установлен: 3 лет.", response.getText());
    }
}