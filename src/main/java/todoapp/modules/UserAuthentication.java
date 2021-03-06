package todoapp.modules;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import todoapp.controllers.ErrorMessageController;
import todoapp.controllers.MainController;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class UserAuthentication {

    public static AuthKey authKey;

    /***
     * This method is authenticating user
     * @param login user login
     * @param password user password
     * @param errorMessageController controller that will be used for error handling
     * @return true if successful or false if error
     */
    public static boolean authenticate(String login, String password, ErrorMessageController errorMessageController) {

        UserData data = new UserData(login, password);

        Gson gson =  new Gson();
        String jsonData = gson.toJson(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/login"))
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(jsonData))
                .build();

        HttpResponse<String> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200) {
                authKey = gson.fromJson(response.body(), AuthKey.class);
                return true;
            } else {
                Message errorMsg = gson.fromJson(response.body(), Message.class);
                errorMessageController.setMessage(errorMsg.message);
            }

        } catch (IOException | InterruptedException e) {
                errorMessageController.setMessage("Server connection error");
                e.printStackTrace();
        }
        return false;
    }


    /***
     * This method is loading a main view scene
     * @param AppStage App Window to display new scene
     * @param url url of FXML file with main app view
     * @throws IOException if an error occurred
     */
    @FXML
    public static void loadMainView(Stage AppStage, URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(url);

        MainController controller = new MainController(authKey);
        loader.setController(controller);

        Scene mainView = new Scene(loader.load());
        AppStage.setTitle("TODO App");
        mainView.lookup(".logo-text").requestFocus();
        AppStage.setScene(mainView);
    }
}
