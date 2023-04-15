import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateUserTest {

    private String myToken;

    @Before
    public void connect(){
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
    }

    @Test
    public void createUserTest(){
        Random random = new Random();
        String email = "something" + random.nextInt(10000000) + "@yandex.ru";
        String json = "{\"email\": \"" + email + "\", \"password\": \"aaa\", \"name\": \"Ivan\"}";
    Response response =
            given()
                    .header("Content-type", "application/json")
                    .body(json)
                    .post("api/auth/register");
        UserInfo userInfo;
        userInfo = response.as(UserInfo.class);
        //Сохраняем токен, для удаления пользовтеля после завершения тестов
        myToken = userInfo.getAccessToken().substring(7, userInfo.getAccessToken().length());

        response.then().statusCode(200).and().assertThat().body("success",equalTo(true));
    }
    @Test
    public void createAlreadyRegisteredUserGet403ResponseTest(){
        Random random = new Random();
        String email = "something" + random.nextInt(10000000) + "@yandex.ru";
        String json = "{\"email\": \"" + email + "\", \"password\": \"aaa\", \"name\": \"Ivan\"}";
        // Создание второго пользователя
        Response firstResponse =
        given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/auth/register");
        UserInfo userInfo;
        userInfo = firstResponse.as(UserInfo.class);
        myToken = userInfo.getAccessToken().substring(7, userInfo.getAccessToken().length());
        // Создание второго пользователя
        Response secondResponse =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/auth/register");
        secondResponse.then().statusCode(403);
        secondResponse.then().assertThat()
                .body("message",equalTo("User already exists"));
    }

    @Test
    public void createUserWithoutRequiredFieldGet403ResponseTest(){
        Random random = new Random();
        String email = "something" + random.nextInt(10000000) + "@yandex.ru";
        String json = "{\"email\": \"" + email + "\", \"password\": \"aaa\"}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/auth/register");
        response.then().statusCode(403);
        response.then().assertThat()
                .body("message",equalTo("Email, password and name are required fields"));
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
