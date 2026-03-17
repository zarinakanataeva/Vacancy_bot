package ru.telegram_bot.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegionProvider {
    private static final Map<String, String> regions = new LinkedHashMap<>();

    static {
        regions.put("01", "Адыгея");
        regions.put("02", "Башкортостан");
        regions.put("03", "Бурятия");
        regions.put("04", "Алтай");
        regions.put("05", "Дагестан");
        regions.put("06", "Ингушетия");
        regions.put("07", "Кабардино-Балкария");
        regions.put("08", "Калмыкия");
        regions.put("09", "Карачаево-Черкесия");
        regions.put("10", "Карелия");
        regions.put("11", "Коми");
        regions.put("12", "Марий Эл");
        regions.put("13", "Мордовия");
        regions.put("14", "Саха");
        regions.put("15", "Северная Осетия");
        regions.put("16", "Татарстан");
        regions.put("17", "Тыва");
        regions.put("18", "Удмуртия");
        regions.put("19", "Хакасия");
        regions.put("20", "Чеченская республика");
        regions.put("21", "Чувашская республика");
        regions.put("22", "Алтай");
        regions.put("23", "Краснодарский край");
        regions.put("24", "Красноярский край");
        regions.put("25", "Приморский край");
        regions.put("26", "Ставропольский край");
        regions.put("27", "Хабаровский край");
        regions.put("28", "Амурская область");
        regions.put("29", "Архангельская область");
        regions.put("30", "Астраханская область");
        regions.put("31", "Белгородская область");
        regions.put("32", "Брянская область");
        regions.put("33", "Владимирская область");
        regions.put("34", "Волгоградская область");
        regions.put("35", "Вологодская область");
        regions.put("36", "Воронежская область");
        regions.put("37", "Ивановская область");
        regions.put("38", "Иркутская область");
        regions.put("39", "Калининградская область");
        regions.put("40", "Калужская область");
        regions.put("41", "Камчатский край");
        regions.put("42", "Кемеровская область");
        regions.put("43", "Кировская область");
        regions.put("44", "Костромская область");
        regions.put("45", "Курганская область");
        regions.put("46", "Курская область");
        regions.put("47", "Ленинградская область");
        regions.put("48", "Липецкая область");
        regions.put("49", "Магаданская область");
        regions.put("50", "Московская область");
        regions.put("51", "Мурманская область");
        regions.put("52", "Нижегородская область");
        regions.put("53", "Новгородская область");
        regions.put("54", "Новосибирская область");
        regions.put("55", "Омская область");
        regions.put("56", "Оренбургская область");
        regions.put("57", "Орловская область");
        regions.put("58", "Пензенская область");
        regions.put("59", "Пермский край");
        regions.put("60", "Псковская область");
        regions.put("61", "Ростовская область");
        regions.put("622", "Рязанская область");
        regions.put("63", "Самарская область");
        regions.put("64", "Саратовская область");
        regions.put("65", "Сахалинская область");
        regions.put("66", "Свердловская область");
        regions.put("67", "Смоленская область");
        regions.put("68", "Тамбовская область");
        regions.put("69", "Тверская область");
        regions.put("70", "Томская область");
        regions.put("71", "Тульская область");
        regions.put("72", "Тюменская область");
        regions.put("73", "Ульяновская область");
        regions.put("74", "Челябинская область");
        regions.put("75", "Забайкальский край");
        regions.put("76", "Ярославская область");
        regions.put("77", "Москва");
        regions.put("78", "Санкт-Петербург");
        regions.put("79", "Еврейская автономная область");
        regions.put("82", "Республика Крым");
        regions.put("83", "Ненецкий автономный округ");
        regions.put("86", "Ханты-Мансийский автономный округ");
        regions.put("87", "Чукотский автономный округ");
        regions.put("89", "Ямало-ненецкий автономный округ");
        regions.put("92", "Севастополь");
    }

    public static Map<String, String> getAll() {
        return regions;
    }

    public String getRegionByNumber(String num) {
        return regions.get(num);
    }
}