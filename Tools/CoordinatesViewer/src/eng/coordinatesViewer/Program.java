package eng.coordinatesViewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Program extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));

    stage.setTitle("FXML Welcome");
    stage.setScene(new Scene(root, 800, 600));
    stage.show();
  }
}
