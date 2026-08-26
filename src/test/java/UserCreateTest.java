import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserCreateTest extends BaseUrl {
    private final UserLogin userLogin = new UserLogin();

    // Создание уникального пользователя
    @Test
    public void shouldCreateUniqueUser () {
        User user = generateUniqueUser();
        Response response = registerUser(user);

        checkStatus200(response);
        checkSuccessTrue(response);
        checkTokenNotNull(response);
        checkUserData(response, user);

        // очистка
        String token = extractAccessToken(response);
        deleteUserIfTokenExists(token);
    }

    // Создание пользователя, который уже зарегистрирован
    @Test
    public void shouldNotCreateDuplicateUser () {
        User user = generateUniqueUser();
        registerUser(user);
        Response twoResponse = registerUser(user);
        checkStatus403(twoResponse);
        checkMessage(twoResponse,"User already exists");

    }

    // Создание пользователя без email
    @Test
    public void shouldNotCreateUserWithoutEmail () {
        User notEmail = new User(null,"password123","NotEmail");
        Response response = registerUser(notEmail);
        checkStatus403(response);
        checkMessage(response,"Email, password and name are required fields");
    }
    // Создание пользователя без password
    @Test public void shouldNotCreateUserWithoutPassword () {
        User notPassword = new User("test" + System.currentTimeMillis() + "@yandex.ru",null,"NotPassword");
        Response response = registerUser(notPassword);
        checkStatus403(response);
        checkMessage(response,"Email, password and name are required fields");
    }
    // Создание пользователя без name
    @Test
    public void shouldNotCreateUserWithoutName () {
        User notName = new User("test" + System.currentTimeMillis()+ "@yandex.ru","password123",null);
        Response response = registerUser(notName);
        checkStatus403(response);
        checkMessage(response,"Email, password and name are required fields");
    }

                  // ===== @Step методы =====
    @Step("Генерация уникального пользователя")
    private User generateUniqueUser () {
        return new User("user" + System.currentTimeMillis() + "@yandex.ru",
                "password123",
                "TestUser");
    }
    @Step("Регистрация пользователя")
    private Response registerUser (User user) {
        return userLogin.register(user);
    }
    @Step ("Проверка статус-кода 200")
    private void checkStatus200 (Response response) {
        assertEquals(200,response.statusCode());
    }
    @Step("Проверка статус-кода 403")
    private void checkStatus403(Response response) {
        assertEquals(403, response.statusCode());
    }
    @Step("Проверка, что success=true")
    private void checkSuccessTrue(Response response) {
        assertTrue(response.jsonPath().getBoolean("success"));
    }
    @Step("Проверка, что токены не пустые")
    private void checkTokenNotNull(Response response) {
        assertNotNull(response.jsonPath().getString("accessToken"));
        assertNotNull(response.jsonPath().getString("refreshToken"));
    }
    @Step("Проверка email и name в ответе")
    private void checkUserData(Response response, User user) {
        assertEquals(user.getEmail(), response.jsonPath().getString("user.email"));
        assertEquals(user.getName(), response.jsonPath().getString("user.name"));
    }
    @Step("Проверка сообщения об ошибке")
    private void checkMessage(Response response, String expectedMessage) {
        assertEquals(expectedMessage, response.jsonPath().getString("message"));
    }
    @Step("Извлечение accessToken из ответа")
    private String extractAccessToken(Response response) {
        return response.jsonPath().getString("accessToken");
    }
    @Step("Удаление пользователя, если токен существует")
    private void deleteUserIfTokenExists(String token) {
        if (token != null) {
            userLogin.deleteUser(token);
        }
    }
}
