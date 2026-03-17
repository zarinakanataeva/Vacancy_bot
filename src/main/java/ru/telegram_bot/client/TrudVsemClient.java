package ru.telegram_bot.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.telegram_bot.exception.ClientInteractionException;
import ru.telegram_bot.model.BotUser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TrudVsemClient {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public TrudVsemClient() {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build(),
                new ObjectMapper());
    }

    public TrudVsemClient(HttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<String> searchVacancies(BotUser user) {
        List<String> results = new ArrayList<>();
        try {
            StringBuilder urlBuilder = new StringBuilder("https://opendata.trudvsem.ru/api/v1/vacancies");
            if (user.getRegionId() != null && !user.getRegionId().isEmpty())
                urlBuilder.append("/region/").append(user.getRegionId());

            urlBuilder.append("?limit=10");
            if (user.getKeyword() != null && !user.getKeyword().isEmpty())
                urlBuilder.append("&text=").append(user.getKeyword().replace(" ", "%20"));
            if (user.getExperience() != null && !user.getExperience().isEmpty())
                urlBuilder.append("&experience=").append(user.getExperience());

            URI uri = new URI(urlBuilder.toString());
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ClientInteractionException("Ошибка HTTP " + response.statusCode() + " при доступе к TrudVsem API. URL: " + uri);
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode vacancies = root.path("results").path("vacancies");

            if (vacancies.isArray()) {
                for (JsonNode v : vacancies) {
                    JsonNode vac = v.path("vacancy");
                    int salary = vac.path("salary_min").asInt();

                    if (user.getMinSalary() != null && salary > 0 && salary < user.getMinSalary()) continue;

                    results.add("🏢 " + vac.path("company").path("name").asText("Не указано") +
                            "n📌 " + vac.path("job-name").asText("Не указано") +
                            "n💰 " + (salary > 0 ? salary + " руб." : "з/п не указана") +
                            "n🔗 " + vac.path("vac_url").asText("Ссылка отсутствует"));
                }
            }
        } catch (URISyntaxException e) {
            throw new ClientInteractionException("Неверный формат URL для TrudVsem API: " + e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            throw new ClientInteractionException("Ошибка сети или ввода/вывода при доступе к TrudVsem API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClientInteractionException("Непредвиденная ошибка при поиске вакансий на TrudVsem: " + e.getMessage(), e);
        }
        return results;
    }
}