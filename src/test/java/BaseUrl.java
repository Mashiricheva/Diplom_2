import io.restassured.RestAssured;
import org.junit.Before;
public abstract class BaseUrl {
    @Before
    public void setUpBase () {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
    }
}
