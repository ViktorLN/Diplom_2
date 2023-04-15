import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CreateOrderTest {

    private List<Data> data;
    private String firstIngredient;
    private String secondIngredient;
    private String lastIngredient;
    private String myToken;

    @Before
    public void connect() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        IngredientsData ingredientsData =
                given()
                        .header("Content-type", "application/json")
                        .get("api/ingredients")
                        .body().as(IngredientsData.class);
        data = ingredientsData.getData();
        firstIngredient = data.get(0).get_id();
        secondIngredient = data.get(data.size()-1).get_id();
        lastIngredient = data.get(data.size()/2).get_id();
    }

    @Test
    public void createOrderWithAuthorizationGet200ResponseTest(){
        // Создаём пользователя
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
        // Делаем заказ
        String orderJson = "{\"ingredients\": [\"" + firstIngredient + "\",\"" + secondIngredient + "\",\"" + lastIngredient + "\"]}";
        Response orderResponse =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(myToken)
                        .body(orderJson)
                        .post("api/orders");
        orderResponse.then().statusCode(200).and().body("order", notNullValue());
    }

    @Test
    public void createOrderWithOutAuthorizationGet200ResponseTest() {
        String json = "{\"ingredients\": [\"" + firstIngredient + "\"]}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/orders");
        response.then().statusCode(200).and().body("order", notNullValue());
    }

    @Test
    public void createOrderWithIngredientsTest(){
        String json = "{\"ingredients\": [\"" + firstIngredient + "\",\"" + secondIngredient + "\",\"" + lastIngredient + "\"]}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/orders");
        response.then().statusCode(200).and().body("order", notNullValue());
    }

    @Test
    public void createOrderWithOutIngredientsGet400ResponseTest(){
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body("")
                        .post("api/orders");
        response.then().statusCode(400)
                .and().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithWrongHashGet500ResponseTest(){
        String json = "{\"ingredients\": [\"Булка\",\"Котлета\",\"Сыр\"]}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body(json)
                        .post("api/orders");
        response.then().statusCode(500);
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
