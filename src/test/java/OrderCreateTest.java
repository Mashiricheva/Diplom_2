import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OrderCreateTest extends BaseUrl {
    private final UserLogin userLogin = new UserLogin();
    private final OrderUser orderUser = new OrderUser();

// С авторизацией
    @Test
    public void shouldCreateOrderWithAuth () {
        String token = registerAndGetToken();
        Order order = new Order(getValidIngredientHashes());
        Response response = createOrder(order,token);

        checkStatus200(response);
        checkSuccessTrue(response);
        checkOrderNumberNotNull(response);

        deleteUserIfTokenExists(token);
    }
// Без авторизации
    @Test
    public void shouldNotCreateOrderWithoutAuth () {
        Order order = new Order(getValidIngredientHashes());
        Response response = createOrder(order,null);

        checkStatus401(response);
        checkMessage(response,"You should be authorised");

    }
// С ингридиентами
    @Test
    public void shouldCreateOrderWithIngredients () {
        String token = registerAndGetToken();
        Order order = new Order(getValidIngredientHashes());
        Response response = createOrder(order,token);

        checkStatus200(response);
        checkSuccessTrue(response);
        checkOrderNumberNotNull(response);

        deleteUserIfTokenExists(token);
    }
// Без ингридиентов
    @Test
    public void shouldNotCreateOrderWithoutIngredients () {
        String token = registerAndGetToken();
        Order order = new Order(List.of()); // пустой список
        Response response = createOrder(order,token);

        checkStatus400(response);
        checkMessage(response,"Ingredient ids must be provided");

        deleteUserIfTokenExists(token);
    }
// С неверным хешем ингредиентов
    @Test
    public void shouldNotCreateOrderWithWrongHashIngredients () {
        String token = registerAndGetToken();
        Order order = new Order(List.of("invalid_hash"));
        Response response = createOrder(order,token);

        checkStatus500(response);

        deleteUserIfTokenExists(token);
    }

    // ===== @Step методы =====//
    @Step("Регистрация пользователя и получение accessToken")
    private String registerAndGetToken() {
        User user = new User("order" + System.currentTimeMillis() + "@yandex.ru",
                "password123", "OrderUser");
        Response response = userLogin.register(user);
        return response.jsonPath().getString("accessToken");
    }

    @Step("Получение списка валидных хешей ингредиентов")
    private List<String> getValidIngredientHashes() {
        Response ingredientsResp = orderUser.getIngredients();
        List<String> hashes = ingredientsResp.jsonPath().getList("data._id");
        assertTrue("Список ингредиентов пуст", hashes.size() > 0);
        return hashes.subList(0, Math.min(2, hashes.size()));
    }

    @Step("Создание заказа")
    private Response createOrder(Order order, String token) {
        return orderUser.createOrder(order, token);
    }

    @Step("Проверка статус-кода 200")
    private void checkStatus200(Response response) {
        assertEquals(200, response.statusCode());
    }

    @Step("Проверка статус-кода 400")
    private void checkStatus400(Response response) {
        assertEquals(400, response.statusCode());
    }
    @Step ("Проверка статус-кода 401")
    private void checkStatus401 (Response response) {
        assertEquals(401, response.statusCode());
    }
    @Step("Проверка статус-кода 500")
    private void checkStatus500(Response response) {
        assertEquals(500, response.statusCode());
    }
    @Step("Проверка, что success=true")
    private void checkSuccessTrue(Response response) {
        assertTrue(response.jsonPath().getBoolean("success"));
    }
    @Step("Проверка, что номер заказа не пустой")
    private void checkOrderNumberNotNull(Response response) {
        assertNotNull(response.jsonPath().getString("order.number"));
    }
    @Step("Проверка сообщения об ошибке}")
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
