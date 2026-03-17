package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

public class SalaryCommandTest {

    private final SalaryCommand command = new SalaryCommand();
    private final long userId = 1L;
    private final long chatId = 123L;

    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /salary")
    void canHandleShouldReturnTrueForSalaryCommand() {
        assertTrue(command.canHandle("/salary"));
        assertTrue(command.canHandle("/salary 10000"));
        assertFalse(command.canHandle("salary"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен установить минимальную зарплату")
    void executeShouldSetMinSalary() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/salary 150000");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(150000, user.getMinSalary());
        assertEquals("✅ Зарплата установлена: 150000 руб.", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен игнорировать нечисловые символы при указании зарплаты")
    void executeShouldIgnoreNonNumericChars() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/salary 150000 руб");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertEquals(150000, user.getMinSalary());
        assertEquals("✅ Зарплата установлена: 150000 руб.", response.getText());
    }

    @Test
    @DisplayName("execute: Должен выдавать ошибку если зарплата не указана")
    void executeShouldThrowErrorIfSalaryNotProvided() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/salary");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertNull(user.getMinSalary());
        assertEquals("❌ Введите числовое значение зарплаты.", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

}
