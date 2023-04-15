import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class LogInTest {
    private String myToken;
    private String email;

    @Before
    public void connect(){
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
    public void loginWithRightCredGet200ResponseTest(){
        String json = "{\"email\": \"" + email + "\", \"password\": \"aaa\"}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/auth/login");
        response.then().statusCode(200);
        response.then().assertThat().body("success",equalTo(true));
    }

    @Test
    public void loginWithWrongCredGet401ResponseTest(){
        String json = "{\"email\": \"" + email + "\", \"password\": \"406\"}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/auth/login");
        response.then().statusCode(401);
        response.then().assertThat().body("message",equalTo("email or password are incorrect"));
    }

    @After
    public void deleteUser(){
        if (myToken==null){
            System.out.println("User not created");
        }
        else {
            given()
                    .auth().oauth2(myToken)
                    .delete("api/auth/user");
            myToken = null;
            System.out.println("User deleted");
        }
    }

}
