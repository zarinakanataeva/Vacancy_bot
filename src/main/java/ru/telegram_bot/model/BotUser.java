package ru.telegram_bot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotUser implements Serializable {
    private Long id;
    private Long chatId;
    private String username;
    private String regionId;
    private String regionName;
    private String experience;
    private Integer minSalary;
    private String keyword;
    private String notifyTime;
    private int utcOffset = 0;
    private int currentRegionPage = 0;
}