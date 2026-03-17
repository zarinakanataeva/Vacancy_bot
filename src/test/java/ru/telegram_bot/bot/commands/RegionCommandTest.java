package ru.telegram_bot.bot.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegionCommandTest {

    private final RegionCommand command = new RegionCommand();
    private final long userId = 1L;
    private final long chatId = 123L;


    @Test
    @DisplayName("canHandle: Должен возвращать true для команды /region")
    void canHandleShouldReturnTrueForRegionCommand() {
        assertTrue(command.canHandle("/region"));
        assertTrue(command.canHandle("/region 92"));
        assertFalse(command.canHandle("region"));
        assertFalse(command.canHandle("/start"));
    }

    @Test
    @DisplayName("execute: Должен выдавать ошибку при указании невалидного региона")
    void executeShouldThrowErrorIfRegionInvalid() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/region 1000");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertNull(user.getRegionId());
        assertNull(user.getRegionName());
        assertEquals("❌ Код региона не найден.", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("execute: Должен выдавать ошибку если регион не указан")
    void executeShouldThrowErrorIfRegionNotProvided() {
        BotUser user = TestUtils.createTestUser(userId, chatId);
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/region");

        SendMessage response = (SendMessage) command.execute(update, user);

        assertNull(user.getRegionId());
        assertNull(user.getRegionName());
        assertEquals("❌ Код региона не найден.", response.getText());
    }

}
