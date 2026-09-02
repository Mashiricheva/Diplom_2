import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderUser {
    // Создание заказа
    public Response createOrder(Order order, String accessToken) {
        var request = given()
                .header("Content-Type", "application/json")
                .body(order);
        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }
        return request.when().post(ApiConstants.ORDERS);
    }
// Получение списка ингридиентов
    public Response getIngredients () {
        return given()
                .when()
                .get(ApiConstants.INGREDIENTS);
    }
}
