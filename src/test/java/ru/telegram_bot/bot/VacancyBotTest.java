
package ru.telegram_bot.bot;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.telegram_bot.TestUtils;
import ru.telegram_bot.exception.CommandExecutionException;
import ru.telegram_bot.model.BotUser;
import ru.telegram_bot.repository.UserRepository;
import ru.telegram_bot.service.NotificationService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VacancyBotTest {

    @Mock
    private TelegramClient mockTelegramClient;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private NotificationService mockNotificationService;
    @Mock
    private CommandDispatcher mockCommandDispatcher;

    @InjectMocks
    private VacancyBot vacancyBot;

    private final long userId = 1L;
    private final long chatId = 123L;
    private final int messageId = 456;
    private BotUser testUser;

    private ArgumentCaptor<BotApiMethod<?>> messageCaptor;

    @BeforeEach
    void setUp() {
        testUser = TestUtils.createTestUser(userId, chatId);
        testUser.setUsername("testuser");
        testUser.setRegionId("77");
        testUser.setRegionName("Москва");

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(mockUserRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(userId)) {
                return Optional.of(testUser);
            }
            return Optional.empty();
        });

        messageCaptor = ArgumentCaptor.forClass(BotApiMethod.class);
    }

    private void verifyExecuteCalls(int times, Class<?>... expectedTypes) throws TelegramApiException {
        verify(mockTelegramClient, times(times)).execute(messageCaptor.capture());
        List<BotApiMethod<?>> capturedMessages = messageCaptor.getAllValues();

        assertEquals(times, capturedMessages.size(), "Неверное количество вызовов execute.");

        for (int i = 0; i < times; i++) {
            if (i < expectedTypes.length) {
                assertTrue(expectedTypes[i].isInstance(capturedMessages.get(i)),
                        "Сообщение №" + (i + 1) + " имеет неверный тип. Ожидалось: "
                                + expectedTypes[i].getSimpleName() +
                                ", Получено: " + capturedMessages.get(i).getClass().getSimpleName());
            }
        }
    }

    @Test
    @DisplayName("handleText: Должен вызвать диспетчер и сохранить пользователя")
    void handleTextShouldCallDispatcherAndSaveUser() throws Exception {
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/test");
        SendMessage commandResponse = SendMessage.builder().chatId(chatId).text("Command handled").build();

        ArgumentCaptor<BotUser> botUserCaptor = ArgumentCaptor.forClass(BotUser.class);

        when(mockCommandDispatcher.dispatch(eq(update), any(BotUser.class)))
                .thenReturn((BotApiMethod) commandResponse);

        vacancyBot.consume(update);

        verify(mockCommandDispatcher).dispatch(eq(update), botUserCaptor.capture());
        BotUser capturedBotUser = botUserCaptor.getValue();

        assertEquals(userId, capturedBotUser.getId());
        assertEquals(chatId, capturedBotUser.getChatId());

        verifyExecuteCalls(1, SendMessage.class);

        SendMessage executedMessage = (SendMessage) messageCaptor.getValue();
        assertEquals("Command handled", executedMessage.getText());
        assertEquals(String.valueOf(chatId), executedMessage.getChatId());

        verify(mockUserRepository).save(capturedBotUser);
        verify(mockUserRepository, never()).delete(anyLong());
    }

    @Test
    @DisplayName("handleText: Должен отправить меню для команды /start")
    void handleTextShouldSendInlineMenuForStartCommand() throws Exception {
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/start");
        SendMessage commandResponse = SendMessage.builder().chatId(chatId).text("Welcome").build();

        ArgumentCaptor<BotUser> botUserCaptor = ArgumentCaptor.forClass(BotUser.class);
        when(mockCommandDispatcher.dispatch(eq(update), any(BotUser.class)))
                .thenReturn((BotApiMethod) commandResponse);

        vacancyBot.consume(update);

        verify(mockCommandDispatcher).dispatch(eq(update), botUserCaptor.capture());
        BotUser capturedBotUser = botUserCaptor.getValue();
        assertEquals(userId, capturedBotUser.getId());
        assertEquals(chatId, capturedBotUser.getChatId());

        verifyExecuteCalls(2, SendMessage.class, SendMessage.class);

        SendMessage sentCommandResponse = (SendMessage) messageCaptor.getAllValues().get(0);
        assertEquals("Welcome", sentCommandResponse.getText());

        SendMessage menuMessage = (SendMessage) messageCaptor.getAllValues().get(1);
        assertEquals("📋 Ваши текущие фильтры:", menuMessage.getText());
        assertNotNull(menuMessage.getReplyMarkup());
        assertInstanceOf(InlineKeyboardMarkup.class, menuMessage.getReplyMarkup());

        verify(mockUserRepository).save(capturedBotUser);
    }

    @Test
    @DisplayName("handleText: Должен отправить меню для команды /menu")
    void handleTextShouldSendInlineMenuForMenuCommand() throws Exception {
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/menu");
        SendMessage commandResponse = SendMessage.builder().chatId(chatId).text("Opening menu").build();

        ArgumentCaptor<BotUser> botUserCaptor = ArgumentCaptor.forClass(BotUser.class);
        doReturn(commandResponse).when(mockCommandDispatcher).dispatch(eq(update), any(BotUser.class));

        vacancyBot.consume(update);

        verify(mockCommandDispatcher).dispatch(eq(update), botUserCaptor.capture());
        BotUser capturedBotUser = botUserCaptor.getValue();
        assertEquals(userId, capturedBotUser.getId());
        assertEquals(chatId, capturedBotUser.getChatId());

        verifyExecuteCalls(2, SendMessage.class, SendMessage.class);

        SendMessage menuMessage = (SendMessage) messageCaptor.getAllValues().get(1);
        assertEquals("📋 Ваши текущие фильтры:", menuMessage.getText());
        verify(mockUserRepository).save(capturedBotUser);
    }

    @Test
    @DisplayName("handleText: Должен обработать VacancyBotException и отправить сообщение об ошибке пользователю")
    void handleTextShouldHandleVacancyBotExceptionAndSendError() throws Exception {
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/error_command");

        ArgumentCaptor<BotUser> botUserCaptor = ArgumentCaptor.forClass(BotUser.class);
        when(mockCommandDispatcher.dispatch(eq(update), any(BotUser.class)))
                .thenThrow(new CommandExecutionException("Test error from command"));

        String stderr = SystemLambda.tapSystemErr(() -> vacancyBot.consume(update));

        verify(mockCommandDispatcher).dispatch(eq(update), botUserCaptor.capture());
        BotUser capturedBotUser = botUserCaptor.getValue();
        assertEquals(userId, capturedBotUser.getId());
        assertEquals(chatId, capturedBotUser.getChatId());

        verifyExecuteCalls(1, SendMessage.class);
        SendMessage errorMessage = (SendMessage) messageCaptor.getValue();
        assertEquals("❌ Test error from command", errorMessage.getText());
        assertEquals("", stderr);

        verify(mockUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleText: Должен обработать непредвиденное исключение и отправить общее сообщение об ошибке")
    void handleTextShouldHandleUnexpectedExceptionAndSendGenericError() throws Exception {
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/critical");

        ArgumentCaptor<BotUser> botUserCaptor = ArgumentCaptor.forClass(BotUser.class);
        when(mockCommandDispatcher.dispatch(eq(update), any(BotUser.class)))
                .thenThrow(new RuntimeException("Critical system error"));

        String stderr = SystemLambda.tapSystemErr(() -> vacancyBot.consume(update));

        verify(mockCommandDispatcher).dispatch(eq(update), botUserCaptor.capture());
        BotUser capturedBotUser = botUserCaptor.getValue();
        assertEquals(userId, capturedBotUser.getId());
        assertEquals(chatId, capturedBotUser.getChatId());

        verifyExecuteCalls(1, SendMessage.class);
        SendMessage errorMessage = (SendMessage) messageCaptor.getValue();
        assertEquals("❌ Произошла ошибка. Пожалуйста, попробуйте позже.", errorMessage.getText());
        assertTrue(stderr.contains("Critical system error"));

        verify(mockUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleCallback: Должен вернуться в главное меню при menu_main")
    void handleCallbackShouldReturnToMainMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "menu_main");
        vacancyBot.consume(update);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("📋 Ваши фильтры:", editedMessage.getText());
        assertEquals(messageId, editedMessage.getMessageId());
        assertNotNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен открыть меню уведомлений при menu_notify")
    void handleCallbackShouldOpenNotifyMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "menu_notify");
        vacancyBot.consume(update);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("⏰ Выберите время или введите: /notify ЧЧ ММ", editedMessage.getText());
        assertNotNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен установить время уведомления и вернуться в меню")
    void handleCallbackShouldSetNotifyTimeAndReturnToMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "time_set_10_30");
        vacancyBot.consume(update);
        assertEquals("10:30", testUser.getNotifyTime());
        verifyExecuteCalls(3, EditMessageText.class, SendMessage.class, SendMessage.class);

        assertInstanceOf(EditMessageText.class, messageCaptor.getAllValues().get(0));
        assertEquals("📋 Ваши фильтры:", ((EditMessageText) messageCaptor.getAllValues().get(0)).getText());

        assertInstanceOf(SendMessage.class, messageCaptor.getAllValues().get(1));
        assertEquals("✅ Время установлено на: 10:30", ((SendMessage) messageCaptor.getAllValues().get(1)).getText());
        verify(mockUserRepository).save(testUser);

        assertInstanceOf(SendMessage.class, messageCaptor.getAllValues().get(2));

        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен открыть меню региона")
    void handleCallbackShouldOpenRegionMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "menu_region");
        vacancyBot.consume(update);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("Выберите регион:", editedMessage.getText());
        assertNotNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен установить регион и вернуться в меню")
    void handleCallbackShouldSetRegionAndReturnToMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "reg_set_77_Москва");
        vacancyBot.consume(update);
        assertEquals("77", testUser.getRegionId());
        assertEquals("Москва", testUser.getRegionName());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("📋 Ваши фильтры:", editedMessage.getText());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен увеличить страницу региона при reg_next")
    void handleCallbackShouldIncrementRegionPage() throws Exception {
        testUser.setCurrentRegionPage(0);
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "reg_next");
        vacancyBot.consume(update);
        assertEquals(1, testUser.getCurrentRegionPage());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("Выберите регион:", editedMessage.getText());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен уменьшить страницу региона при reg_prev (но не ниже 0)")
    void handleCallbackShouldDecrementRegionPage() throws Exception {
        testUser.setCurrentRegionPage(1);
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "reg_prev");
        vacancyBot.consume(update);
        assertEquals(0, testUser.getCurrentRegionPage());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("Выберите регион:", editedMessage.getText());

        update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "reg_prev");
        vacancyBot.consume(update);
        assertEquals(0, testUser.getCurrentRegionPage());
        verify(mockUserRepository, times(2)).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен открыть меню опыта")
    void handleCallbackShouldOpenExperienceMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "menu_exp");
        vacancyBot.consume(update);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("Выберите опыт:", editedMessage.getText());
        assertNotNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен установить опыт и вернуться в меню")
    void handleCallbackShouldSetExperienceAndReturnToMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "exp_set_3");
        vacancyBot.consume(update);
        assertEquals("3", testUser.getExperience());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("📋 Ваши фильтры:", editedMessage.getText());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен открыть меню зарплаты")
    void handleCallbackShouldOpenSalaryMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "menu_salary");
        vacancyBot.consume(update);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("Выберите минимальную зарплату:", editedMessage.getText());
        assertNotNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен установить зарплату и вернуться в меню")
    void handleCallbackShouldSetSalaryAndReturnToMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "sal_set_150000");
        vacancyBot.consume(update);
        assertEquals(150000, testUser.getMinSalary());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("📋 Ваши фильтры:", editedMessage.getText());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен открыть меню ключевых слов")
    void handleCallbackShouldOpenKeywordMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "menu_keyword");
        vacancyBot.consume(update);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("Выберите профессию или введите /keyword [текст]:", editedMessage.getText());
        assertNotNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен установить ключевое слово и вернуться в меню")
    void handleCallbackShouldSetKeywordAndReturnToMenu() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "key_set_Java");
        vacancyBot.consume(update);
        assertEquals("Java", testUser.getKeyword());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("📋 Ваши фильтры:", editedMessage.getText());
        verify(mockUserRepository).save(testUser);
    }


    @Test
    @DisplayName("handleCallback: Должен удалить данные пользователя при yes_sure")
    void handleCallbackShouldDeleteUserOnYesSure() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "yes_sure");
        vacancyBot.consume(update);
        verify(mockNotificationService).cancel(userId);
        verify(mockUserRepository).delete(userId);
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("🗑 Ваши данные успешно удалены. Поиск остановлен.", editedMessage.getText());
        assertNull(editedMessage.getReplyMarkup());
        verify(mockUserRepository, never()).save(any(BotUser.class));
    }

    @Test
    @DisplayName("handleCallback: Должен отменить удаление при not_sure")
    void handleCallbackShouldCancelDeletionOnNotSure() throws Exception {
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "not_sure");
        vacancyBot.consume(update);
        verify(mockUserRepository).save(testUser);
        verify(mockNotificationService, never()).cancel(anyLong());
        verify(mockUserRepository, never()).delete(anyLong());
        verifyExecuteCalls(1, EditMessageText.class);
        EditMessageText editedMessage = (EditMessageText) messageCaptor.getValue();
        assertEquals("✅ Удаление отменено. Ваши фильтры сохранены.", editedMessage.getText());
        assertNotNull(editedMessage.getReplyMarkup());
    }

    @Test
    @DisplayName("handleCallback: Должен запустить поиск при btn_ready, если время установлено")
    void handleCallbackShouldStartSearchOnBtnReadyIfNotifyTimeSet() throws Exception {
        testUser.setNotifyTime("10:00");
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "btn_ready");
        vacancyBot.consume(update);
        verify(mockNotificationService).schedule(testUser);
        verifyExecuteCalls(1, SendMessage.class);
        SendMessage searchMessage = (SendMessage) messageCaptor.getValue();
        assertTrue(searchMessage.getText().contains("🚀 Поиск запущен!"));
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("handleCallback: Должен попросить установить время при btn_ready, если время не установлено")
    void handleCallbackShouldAskForTimeOnBtnReadyIfNotifyTimeNotSet() throws Exception {
        testUser.setNotifyTime(null);
        Update update = TestUtils.createMockUpdateWithCallbackQuery(userId, chatId, messageId, "btn_ready");
        vacancyBot.consume(update);
        verify(mockNotificationService, never()).schedule(any(BotUser.class));
        verifyExecuteCalls(1, SendMessage.class);
        SendMessage warningMessage = (SendMessage) messageCaptor.getValue();
        assertTrue(warningMessage.getText().contains("⚠️ Сначала укажите время уведомления"));
        verify(mockUserRepository).save(testUser);
    }

    @Test
    @DisplayName("sendErrorMessage: Должен логировать ошибку, если не может отправить сообщение об ошибке")
    void sendErrorMessageShouldLogErrorIfItCannotSendError() throws Exception {
        Update update = TestUtils.createMockUpdateWithTextMessage(userId, chatId, "/error");
        ArgumentCaptor<BotUser> botUserCaptor = ArgumentCaptor.forClass(BotUser.class);

        when(mockCommandDispatcher.dispatch(eq(update), any(BotUser.class)))
                .thenThrow(new CommandExecutionException("Test error"));
        doThrow(new RuntimeException("Failed to send error message")).when(mockTelegramClient).execute(any(SendMessage.class));

        String stderr = SystemLambda.tapSystemErr(() -> vacancyBot.consume(update));

        verify(mockCommandDispatcher).dispatch(eq(update), botUserCaptor.capture());

        verifyExecuteCalls(1, SendMessage.class);
        assertTrue(stderr.contains("Не удалось отправить сообщение об ошибке пользователю: Test error"));
        verify(mockUserRepository, never()).save(any());
    }
}
