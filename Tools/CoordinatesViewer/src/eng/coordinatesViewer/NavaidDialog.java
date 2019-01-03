package eng.coordinatesViewer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NavaidDialog {
  private Stage dialogStage;
  private String navaidName;
  @FXML
  private TextField txtNavaidName;

  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public String getNavaidName() {
    return navaidName;
  }

  @FXML
  void btnOk_click() {
    this.navaidName = txtNavaidName.getText().toUpperCase().trim();
    this.dialogStage.close();
  }
}
