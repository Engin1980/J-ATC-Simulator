import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Program extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  public static Scene loadScene(String name) {
    name = name + ".fxml";
    FXMLLoader fxmlLoader = new FXMLLoader(Program.class.getResource(name));
    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load scene " + name, e);
    }
    Scene ret = new Scene(root);
    return ret;
  }

  public static Stage createStage(String sceneName, Modality modality) {
    Scene sc = loadScene(sceneName);
    Stage ret = new Stage();
    ret.initModality(modality);
    ret.initStyle(StageStyle.DECORATED);
    ret.setScene(sc);
    return ret;
  }

  @Override
  public void start(Stage stage) {
    Scene sc = loadScene("StartupMenu");
    stage.setScene(sc);
    stage.show();
  }
}
