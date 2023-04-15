import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserDataChangeTest {
    private String myToken;
    private String email;

    @Before
    public void connect() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        Random random = new Random();
        email = "something" + random.nextInt(10000000) + "@yandex.ru";
        String json = "{\"email\": \"" + email + "\", \"password\": \"aaa\", \"name\": \"Ivan\"}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/auth/register");
        UserInfo userInfo;
        userInfo = response.as(UserInfo.class);
        myToken = userInfo.getAccessToken().substring(7, userInfo.getAccessToken().length());
    }

    @Test
    public void changeUserDataWithAuthorizationTest(){
        Response changeNameResponse =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(myToken)
                        .body("{\"name\": \"Aleksandr\"}")
                        .patch("api/auth/user");

        changeNameResponse.then().statusCode(200);
        changeNameResponse.then().assertThat().body("user.name",equalTo("Aleksandr"));

        Response changeEmailResponse =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(myToken)
                        .body("{\"email\": \"test122999-data@yandex.ru\"}")
                        .patch("api/auth/user");
        changeEmailResponse.then().statusCode(200);
        changeEmailResponse.then().assertThat().body("user.email",equalTo("test122999-data@yandex.ru"));
    }

    @Test
    public void changeUserDataWithOutAuthorizationTest(){
        Response changeNameResponse =
                given()
                        .header("Content-type", "application/json")
                        .body("{\"name\": \"Aleksandr\"}")
                        .patch("api/auth/user");
        changeNameResponse.then().statusCode(401);
        changeNameResponse.then().assertThat().body("message",equalTo("You should be authorised"));

        Response changeEmailResponse =
                given()
                        .header("Content-type", "application/json")
                        .body("{\"email\": \"test122999-data@yandex.ru\"}")
                        .patch("api/auth/user");
        changeEmailResponse.then().statusCode(401);
        changeEmailResponse.then().assertThat().body("message",equalTo("You should be authorised"));
    }

    @After
    public void deleteUser() {
        if (myToken == null) {
            System.out.println("User not created");
        } else {
            given()
                    .auth().oauth2(myToken)
                    .delete("api/auth/user");
            myToken = null;
            System.out.println("User deleted");
        }
    }
}
