package ru.telegram_bot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.telegram_bot.model.BotUser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static Update createMockUpdateWithTextMessage(long userId, long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(message.getChat()).thenReturn(chat);
        when(chat.getId()).thenReturn(chatId);

        return update;
    }

    public static Update createMockUpdateWithCallbackQuery(long userId, long chatId, int messageId, String callbackData) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn(callbackData);

        when(user.getId()).thenReturn(userId);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        when(message.getChat()).thenReturn(chat);
        when(chat.getId()).thenReturn(chatId);

        return update;
    }

    public static BotUser createTestUser(Long id, Long chatId) {
        BotUser user = new BotUser();
        user.setId(id);
        user.setChatId(chatId);
        user.setUsername("testUsername");
        user.setUtcOffset(3);
        return user;
    }
}