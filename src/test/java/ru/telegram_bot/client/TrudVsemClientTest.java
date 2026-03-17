
package ru.telegram_bot.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.telegram_bot.exception.ClientInteractionException;
import ru.telegram_bot.model.BotUser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrudVsemClientTest {

    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HttpResponse<String> mockHttpResponse;

    private ObjectMapper objectMapper;
    private TrudVsemClient trudVsemClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        trudVsemClient = new TrudVsemClient(mockHttpClient, objectMapper);
    }

    private BotUser createTestUser() {
        BotUser user = new BotUser();
        user.setId(1L);
        user.setChatId(123L);
        user.setRegionId("77");
        user.setRegionName("Москва");
        user.setExperience("1");
        user.setMinSalary(100000);
        user.setKeyword("Java");
        return user;
    }

    private String createJsonResponse(int count, int salaryMin, String company, String jobName, String vacUrl) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        ObjectNode results = root.putObject("results");
        ArrayNode vacancies = results.putArray("vacancies");

        for (int i = 0; i < count; i++) {
            ObjectNode vacancyItem = vacancies.addObject();
            ObjectNode vacancy = vacancyItem.putObject("vacancy");
            vacancy.put("salary_min", salaryMin);
            vacancy.put("job-name", jobName + (count > 1 ? " " + i : ""));
            vacancy.put("vac_url", vacUrl + (count > 1 ? i : ""));
            vacancy.putObject("company").put("name", company + (count > 1 ? " " + i : ""));
        }
        return root.toString();
    }

    @Test
    @DisplayName("searchVacancies: Должен вернуть список вакансий при успешном ответе API")
    void searchVacanciesShouldReturnVacanciesOnSuccess() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        String jsonResponse = createJsonResponse(2, 120000, "Company", "Developer", "http://example.com/job");

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenReturn(mockHttpResponse);

        List<String> results = trudVsemClient.searchVacancies(user);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).contains("🏢 Company 0"));
        assertTrue(results.get(0).contains("📌 Developer 0"));
        assertTrue(results.get(0).contains("💰 120000 руб."));
        assertTrue(results.get(0).contains("🔗 http://example.com/job0"));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());
        String expectedUrl = "https://opendata.trudvsem.ru/api/v1/vacancies/region/77?limit=10&text=Java&experience=1";
        assertEquals(expectedUrl, requestCaptor.getValue().uri().toString());
    }

    @Test
    @DisplayName("searchVacancies: Должен вернуть пустой список, если API не возвращает вакансий")
    void searchVacanciesShouldReturnEmptyListIfNoVacancies() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        String jsonResponse = "{\"results\": {\"vacancies\": []}}";

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenReturn(mockHttpResponse);

        List<String> results = trudVsemClient.searchVacancies(user);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchVacancies: Должен бросить ClientInteractionException при HTTP ошибке")
    void searchVacanciesShouldThrowExceptionOnHttpError() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        when(mockHttpResponse.statusCode()).thenReturn(500);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenReturn(mockHttpResponse);

        ClientInteractionException thrown = assertThrows(ClientInteractionException.class, () -> trudVsemClient.searchVacancies(user));
        assertTrue(thrown.getMessage().contains("Ошибка HTTP 500 при доступе к TrudVsem API."));
    }


    @Test
    @DisplayName("searchVacancies: Должен бросить ClientInteractionException при сетевой ошибке (IOException)")
    void searchVacanciesShouldThrowExceptionOnNetworkError() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenThrow(new IOException("Network down"));

        ClientInteractionException thrown = assertThrows(ClientInteractionException.class, () -> trudVsemClient.searchVacancies(user));
        assertTrue(thrown.getMessage().contains("Ошибка сети или ввода/вывода при доступе к TrudVsem API: Network down"));
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    @DisplayName("searchVacancies: Должен бросить ClientInteractionException при InterruptedException")
    void searchVacanciesShouldThrowExceptionOnInterruptedException() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenThrow(new InterruptedException("Interrupted"));

        ClientInteractionException thrown = assertThrows(ClientInteractionException.class, () -> trudVsemClient.searchVacancies(user));
        assertTrue(thrown.getMessage().contains("Ошибка сети или ввода/вывода при доступе к TrudVsem API: Interrupted"));
        assertInstanceOf(InterruptedException.class, thrown.getCause());
    }

    @Test
    @DisplayName("searchVacancies: Должен формировать URL без regionId, если он отсутствует")
    void searchVacanciesShouldFormUrlWithoutRegionIdIfNull() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        user.setRegionId(null);
        user.setRegionName(null);

        String jsonResponse = createJsonResponse(1, 100000, "Comp", "Job", "http://link");
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenReturn(mockHttpResponse);

        trudVsemClient.searchVacancies(user);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());
        assertTrue(requestCaptor.getValue().uri().toString().startsWith("https://opendata.trudvsem.ru/api/v1/vacancies?"));
        assertFalse(requestCaptor.getValue().uri().toString().contains("/region/"));
    }

    @Test
    @DisplayName("searchVacancies: Должен формировать URL без keyword, если он отсутствует")
    void searchVacanciesShouldFormUrlWithoutKeywordIfNull() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        user.setKeyword(null);

        String jsonResponse = createJsonResponse(1, 100000, "Comp", "Job", "http://link");
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenReturn(mockHttpResponse);

        trudVsemClient.searchVacancies(user);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());
        assertFalse(requestCaptor.getValue().uri().toString().contains("&text="));
    }

    @Test
    @DisplayName("searchVacancies: Должен формировать URL без experience, если он отсутствует")
    void searchVacanciesShouldFormUrlWithoutExperienceIfNull() throws IOException, InterruptedException {
        BotUser user = createTestUser();
        user.setExperience(null);

        String jsonResponse = createJsonResponse(1, 100000, "Comp", "Job", "http://link");
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass()))).thenReturn(mockHttpResponse);

        trudVsemClient.searchVacancies(user);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());
        assertFalse(requestCaptor.getValue().uri().toString().contains("&experience="));
    }
}
