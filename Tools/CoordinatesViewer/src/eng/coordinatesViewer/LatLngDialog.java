package eng.coordinatesViewer;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LatLngDialog {

  @FXML
  private TextField txtLatitude;

  @FXML
  private TextField txtLongitude;

  private Stage dialogStage;
  private double[] ret = null;

  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public double[] getLatLng() {
    return ret;
  }

  @FXML
  private void btnOk_click() {
    double lat = 0;
    double lng = 0;
    try {
      lat = Double.parseDouble(txtLatitude.getText());
      txtLatitude.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    } catch (Exception ex) {
      txtLatitude.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
      return;
    }

    try {
      lng = Double.parseDouble(txtLongitude.getText());
      txtLongitude.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    } catch (Exception ex) {
      txtLatitude.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
      return;
    }

    ret = new double[2];
    ret[0] = lat;
    ret[1] = lng;

    dialogStage.close();
  }
}
