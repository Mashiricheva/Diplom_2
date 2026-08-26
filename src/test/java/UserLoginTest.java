import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

public class UserLoginTest  extends BaseUrl {
    private final UserLogin userLogin = new UserLogin();
    private User registeredUser;
    @Before
    public void setUpUser () {
        registeredUser = new User( "user" + System.currentTimeMillis() + "@yandex.ru",
                "password123",
                "TestUser");
        userLogin.register(registeredUser);
    }
    // вход под существующим пользователем
    @Test
    public void shouldLoginExistingUser () {
        Response response =loginUser(registeredUser);
        checkStatus200(response);
        checkSuccessTrue(response);
        checkTokenNotNull(response);
        // очистка
        String token = extractAccessToken(response);
        deleteUserIfTokenExists(token);
    }
// Неправильный логин
    @Test
    public void  shouldNotLoginNonexistentLogin () {
        User wrongLogin = new User("wrong@yandex.ru", registeredUser.getPassword(), registeredUser.getName());
        Response response = loginUser(wrongLogin);
        checkStatus401(response);
        checkMessage(response,"email or password are incorrect");
    }

    // Неправильный пароль
    @Test
    public void shouldNotLoginNonexistentPassword () {
        User wrongPassword = new User(registeredUser.getEmail(),"wrongPassword",registeredUser.getName());
        Response response = loginUser(wrongPassword);
        checkStatus401(response);
        checkMessage(response,"email or password are incorrect");
    }

    // Несуществующий логин и пароль
    @Test
    public void shouldNotLoginNonexistentLoginAndPassword () {
        User wrong = new User("wrong@yandex.ru","wrongPassword","wrong");
        Response response = loginUser(wrong);
        checkStatus401(response);
        checkMessage(response,"email or password are incorrect");
    }

              // ===== @Step методы =====
    @Step ("Логин пользователя")
    private Response loginUser (User user) {
        return  userLogin.login(user);
    }
    @Step ("Проверка статус-кода 200")
    private void checkStatus200 (Response response) {
        assertEquals(200, response.statusCode());
    }
    @Step ("Проверка статус-кода 401")
    private void checkStatus401 (Response response) {
        assertEquals(401,response.statusCode());
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
