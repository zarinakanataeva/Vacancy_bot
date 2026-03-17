
package ru.telegram_bot.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.telegram_bot.exception.RepositoryOperationException;
import ru.telegram_bot.model.BotUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private static final String TEST_FOLDER = "data/test_users/";
    private UserRepository userRepository;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() throws IOException {
        Path testPath = Path.of(TEST_FOLDER);
        if (Files.exists(testPath)) {
            cleanUp();
        }
        Files.createDirectories(testPath);
        userRepository = new UserRepository(TEST_FOLDER);
        mapper = new ObjectMapper();
    }

    @AfterEach
    void cleanUp() throws IOException {
        Path testPath = Path.of(TEST_FOLDER);
        if (Files.exists(testPath)) {
            Files.walk(testPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private BotUser createTestUser(Long id) {
        BotUser user = new BotUser();
        user.setId(id);
        user.setChatId(id + 1000L);
        user.setUsername("user" + id);
        return user;
    }

    @Test
    @DisplayName("save: Должен успешно сохранить нового пользователя")
    void saveShouldSuccessfullySaveNewUser() {
        BotUser user = createTestUser(1L);
        userRepository.save(user);

        File userFile = new File(TEST_FOLDER + "1.json");
        assertTrue(userFile.exists());
        assertTrue(userFile.length() > 0);
    }

    @Test
    @DisplayName("save: Должен обновить существующего пользователя")
    void saveShouldUpdateExistingUser() {
        BotUser user = createTestUser(1L);
        user.setUsername("old_username");
        userRepository.save(user);

        user.setUsername("new_username");
        userRepository.save(user);

        Optional<BotUser> foundUser = userRepository.findById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals("new_username", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("save: Должен бросить исключение при null пользователе")
    void saveShouldThrowExceptionForNullUser() {
        assertThrows(RepositoryOperationException.class, () -> userRepository.save(null));
    }

    @Test
    @DisplayName("save: Должен бросить исключение при null ID пользователя")
    void saveShouldThrowExceptionForNullUserId() {
        BotUser user = new BotUser();
        user.setChatId(123L);
        assertThrows(RepositoryOperationException.class, () -> userRepository.save(user));
    }

    @Test
    @DisplayName("findById: Должен найти существующего пользователя")
    void findByIdShouldFindExistingUser() throws IOException {
        BotUser user = createTestUser(2L);
        mapper.writeValue(new File(TEST_FOLDER + "2.json"), user);

        Optional<BotUser> foundUser = userRepository.findById(2L);
        assertTrue(foundUser.isPresent());
        assertEquals(user.getId(), foundUser.get().getId());
        assertEquals(user.getUsername(), foundUser.get().getUsername());
    }

    @Test
    @DisplayName("findById: Должен вернуть Optional.empty() для несуществующего пользователя")
    void findByIdShouldReturnEmptyForNonExistentUser() {
        Optional<BotUser> foundUser = userRepository.findById(99L);
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("findById: Должен бросить исключение при ID пользователя null")
    void findByIdShouldThrowExceptionForNullUserId() {
        assertThrows(RepositoryOperationException.class, () -> userRepository.findById(null));
    }

    @Test
    @DisplayName("findById: Должен бросить исключение для поврежденного файла")
    void findByIdShouldThrowExceptionForCorruptedFile() throws IOException {
        File corruptedFile = new File(TEST_FOLDER + "3.json");
        Files.writeString(corruptedFile.toPath(), "{ \"id\": 3, \"username\": \"corrupted\", }");

        assertThrows(RepositoryOperationException.class, () -> userRepository.findById(3L));
    }

    @Test
    @DisplayName("delete: Должен успешно удалить существующего пользователя")
    void deleteShouldDeleteExistingUser() throws IOException {
        BotUser user = createTestUser(4L);
        mapper.writeValue(new File(TEST_FOLDER + "4.json"), user);
        assertTrue(new File(TEST_FOLDER + "4.json").exists());

        userRepository.delete(4L);
        assertFalse(new File(TEST_FOLDER + "4.json").exists());
    }

    @Test
    @DisplayName("delete: Должен игнорировать удаление несуществующего пользователя")
    void deleteShouldIgnoreNonExistentUser() {
        assertDoesNotThrow(() -> userRepository.delete(999L));
    }


    @Test
    @DisplayName("findAll: Должен найти всех пользователей в директории")
    void findAllUsers() throws IOException {
        mapper.writeValue(new File(TEST_FOLDER + "10.json"), createTestUser(10L));
        mapper.writeValue(new File(TEST_FOLDER + "11.json"), createTestUser(11L));

        Files.writeString(new File(TEST_FOLDER + "some.txt").toPath(), "plain text");

        List<BotUser> users = userRepository.findAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(10L)));
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(11L)));
    }

    @Test
    @DisplayName("findAll: Должен вернуть пустой список, если директория пуста")
    void findAllShouldReturnEmptyListIfDirectoryIsEmpty() {
        List<BotUser> users = userRepository.findAll();
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("findAll: Должен логировать предупреждение для поврежденных файлов и продолжать")
    void findAllShouldLogAndContinue() throws Exception {
        mapper.writeValue(new File(TEST_FOLDER + "20.json"), createTestUser(20L));
        File corruptedFile = new File(TEST_FOLDER + "21.json");
        Files.writeString(corruptedFile.toPath(), "{ \"id\": 21, \"username\": \"corrupted\", }");

        String stderr = SystemLambda.tapSystemErr(() -> {
            List<BotUser> users = userRepository.findAll();
            assertEquals(1, users.size());
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(20L)));
        });
        assertTrue(stderr.contains("Предупреждение: не удалось прочитать файл пользователя 21.json при findAll"));
    }

}
