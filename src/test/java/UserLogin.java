import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserLogin {
    // Регистрация пользователя
    public Response register(User user) {
        return given()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .post(ApiConstants.REGISTER);
    }
    // Авторизация
    public  Response login (User user) {
        return given()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .post(ApiConstants.LOGIN);
    }
    // Удаление пользователя
    public  Response deleteUser (String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .delete(ApiConstants.USER);
    }
}
