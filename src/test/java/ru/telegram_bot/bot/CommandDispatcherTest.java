
package ru.telegram_bot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.model.BotUser;

import static org.junit.jupiter.api.Assertions.*;

class CommandDispatcherTest {

    private CommandDispatcher dispatcher;
    private BotUser testUser;
    private Update mockUpdate;
    private final long userId = 1L;
    private final long chatId = 123L;

    @BeforeEach
    void setUp() {

        dispatcher = new CommandDispatcher();
        testUser = TestUtils.createTestUser(userId, chatId);
    }

    @Test
    @DisplayName("dispatch: Должен вызвать StartCommand для /start")
    void dispatchShouldCallStartCommandForSlashStart() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/start");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("""
                👋 Добро пожаловать!
                               
                Вы успешно зарегистрированы в Vacancy Tracker Bot!
                Используется часовой пояс UTC +3, для изменения введите часовой пояс в формате 'UTC +N'
                               
                Настройте фильтры поиска кнопками.
                                
                """, response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать StopCommand для /stop")
    void dispatchShouldCallStopCommandForSlashStop() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/stop");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertTrue(response.getText().contains("Вы уверены, что хотите сбросить"));
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать MenuCommand для /menu")
    void dispatchShouldCallMenuCommandForSlashMenu() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/menu");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("Открываю главное меню настроек...", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать RegionCommand для /region")
    void dispatchShouldCallRegionCommandForSlashRegion() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/region 77");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("✅ Регион установлен: Москва", response.getText());
        assertEquals("77", testUser.getRegionId());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать SalaryCommand для /salary")
    void dispatchShouldCallSalaryCommandForSlashSalary() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/salary 100000");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("✅ Зарплата установлена: 100000 руб.", response.getText());
        assertEquals(100000, testUser.getMinSalary());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать ExperienceCommand для /experience")
    void dispatchShouldCallExperienceCommandForSlashExperience() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/experience 5");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("✅ Опыт установлен: 5 лет.", response.getText());
        assertEquals("3", testUser.getExperience());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать KeywordCommand для /keyword")
    void dispatchShouldCallKeywordCommandForSlashKeyword() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/keyword Java");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("✅ Поиск по слову: Java", response.getText());
        assertEquals("Java", testUser.getKeyword());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать NotifyCommand для /notify")
    void dispatchShouldCallNotifyCommandForSlashNotify() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/notify 10 00");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("✅ Время уведомления: 10:00", response.getText());
        assertEquals("10:00", testUser.getNotifyTime());
    }

    @Test
    @DisplayName("dispatch: Должен вызвать UtcCommand для UTC +N")
    void dispatchShouldCallUtcCommandForUtcOffset() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "UTC +2");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("✅ Часовой пояс установлен: UTC +2", response.getText());
        assertEquals(2, testUser.getUtcOffset());
    }

    @Test
    @DisplayName("dispatch: Должен вернуть сообщение по умолчанию для неизвестной команды")
    void dispatchShouldReturnDefaultMessageForUnknownCommand() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/unknowncommand");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("❓ Неизвестная команда. \n Введите /menu или нажмите 🚀 Старт.", response.getText());
        assertEquals(String.valueOf(chatId), response.getChatId());
    }

    @Test
    @DisplayName("dispatch: Должен вернуть сообщение по умолчанию для произвольного текста")
    void dispatchShouldReturnDefaultMessageForArbitraryText() {
        mockUpdate = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "Привет, бот!");
        SendMessage response = (SendMessage) dispatcher.dispatch(mockUpdate, testUser);
        assertEquals("❓ Неизвестная команда. \n Введите /menu или нажмите 🚀 Старт.", response.getText());
    }
}
