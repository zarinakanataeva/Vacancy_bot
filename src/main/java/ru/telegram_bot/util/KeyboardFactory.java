package ru.telegram_bot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.telegram_bot.model.BotUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyboardFactory {

    public static ReplyKeyboardMarkup createReplyKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("🚀 Старт");
        row.add("🛑 Стоп");

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .isPersistent(true)
                .build();
    }

    public static InlineKeyboardMarkup createMainMenu(BotUser user) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(createBtn("📍 Регион: " + (user.getRegionName() != null ? user.getRegionName() : "Все"), "menu_region")));
        rows.add(new InlineKeyboardRow(createBtn("⏳ Опыт: " + formatExperience(user.getExperience()), "menu_exp")));
        rows.add(new InlineKeyboardRow(createBtn("💰 Зарплата: " + (user.getMinSalary() != null ? user.getMinSalary() : "0"), "menu_salary")));
        rows.add(new InlineKeyboardRow(createBtn("🔍  Ключевое слово: " + (user.getKeyword() != null ? user.getKeyword() : "Все"), "menu_keyword")));
        rows.add(new InlineKeyboardRow(createBtn("🔔 Время нотификации: " + (user.getNotifyTime() != null ? user.getNotifyTime() : "Не задано"), "menu_notify")));
        rows.add(new InlineKeyboardRow(createBtn("✅ НАЧАТЬ ПОИСК", "btn_ready")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createKeywordKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(createBtn("Java", "key_set_Java"), createBtn("Python", "key_set_Python")));
        rows.add(new InlineKeyboardRow(createBtn("Инженер", "key_set_Инженер"), createBtn("С++", "key_set_С++")));
        rows.add(new InlineKeyboardRow(createBtn("Java junior", "key_set_Java junior"), createBtn("Software engineer", "key_set_Software engineer")));
        rows.add(new InlineKeyboardRow(createBtn("🏠 В меню", "menu_main")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createExperienceKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(createBtn("Без опыта", "exp_set_0")));
        rows.add(new InlineKeyboardRow(createBtn("1-3 года", "exp_set_1")));
        rows.add(new InlineKeyboardRow(createBtn("3-5 лет", "exp_set_3")));
        rows.add(new InlineKeyboardRow(createBtn("Более 5 лет", "exp_set_5")));
        rows.add(new InlineKeyboardRow(createBtn("🏠 В меню", "menu_main")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createSalaryKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(createBtn("50 000", "sal_set_50000"), createBtn("100 000", "sal_set_100000")));
        rows.add(new InlineKeyboardRow(createBtn("150 000", "sal_set_150000"), createBtn("200 000", "sal_set_200000")));
        rows.add(new InlineKeyboardRow(createBtn("🏠 В меню", "menu_main")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createRegionKeyboard(int page) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<Map.Entry<String, String>> allRegions = new ArrayList<>(RegionProvider.getAll().entrySet());
        int start = page * 10;
        int end = Math.min(start + 10, allRegions.size());

        for (int i = start; i < end; i++) {
            Map.Entry<String, String> entry = allRegions.get(i);
            rows.add(new InlineKeyboardRow(createBtn(entry.getValue(), "reg_set_" + entry.getKey() + "_" + entry.getValue())));
        }

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (page > 0) navRow.add(createBtn("⬅️ Назад", "reg_prev"));
        if (end < allRegions.size()) navRow.add(createBtn("Вперед ➡️", "reg_next"));
        if (!navRow.isEmpty()) rows.add(new InlineKeyboardRow(navRow));

        rows.add(new InlineKeyboardRow(createBtn("🏠 В меню", "menu_main")));

        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createNotifyKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        rows.add(new InlineKeyboardRow(
                createBtn("08:00", "time_set_08_00"),
                createBtn("10:00", "time_set_10_00")
        ));

        rows.add(new InlineKeyboardRow(
                createBtn("12:00", "time_set_12_00"),
                createBtn("14:00", "time_set_14_00")
        ));

        rows.add(new InlineKeyboardRow(
                createBtn("16:00", "time_set_16_00"),
                createBtn("18:00", "time_set_18_00")
        ));

        rows.add(new InlineKeyboardRow(createBtn("🏠 В меню", "menu_main")));

        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createAreYouSureKeyBoard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        rows.add(new InlineKeyboardRow(
                createBtn("ДА", "yes_sure"),
                createBtn("НЕТ", "not_sure")
        ));
        return new InlineKeyboardMarkup(rows);
    }


    private static InlineKeyboardButton createBtn(String text, String callback) {
        return InlineKeyboardButton.builder().text(text).callbackData(callback).build();
    }

    public static String formatExperience(String exp) {
        if (exp == null) return "Все";
        return switch (exp) {
            case "0" -> "Без опыта";
            case "1" -> "1-3 года";
            case "3" -> "3-6 лет";
            case "5" -> "5+ лет";
            default -> exp + " лет";
        };
    }
}