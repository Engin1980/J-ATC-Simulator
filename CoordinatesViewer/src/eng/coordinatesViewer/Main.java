package eng.coordinatesViewer;

import eng.eSystem.utilites.ExceptionUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Main {

  public enum Mode {
    navaid,
    border,
    align
  }

  private AlignPoint[] aps = new AlignPoint[2];
  private String imgUrl = "file:D:/lfmm_mrva.png";
  private Image imgOrg;
  private Image imgRes;
  private double ratio = 1;
  @FXML
  private ComboBox<Mode> chkMode;

  @FXML
  private ScrollPane pnlScr;

  @FXML
  private Label lblPoint;
  @FXML
  private Label lblGps;

  @FXML
  public void initialize() {
    System.out.println(".init");

    chkMode.getItems().addAll(Mode.navaid, Mode.border, Mode.align);
  }

  @FXML
  public void btnLoadImage_click() {
    imgOrg = new Image(imgUrl);

    if (imgOrg.getException() != null) {
      System.out.println("Failed: " + ExceptionUtil.toFullString(imgOrg.getException()));
    } else {
      ratio = 1;
      updateRatio();
      updateView();
    }
  }

  @FXML
  void btnZoomIn_click() {
    ratio *= 0.9;
    updateRatio();
    updateView();
  }

  @FXML
  void btnZoomOut_click() {
    ratio /= 0.9;
    updateRatio();
    updateView();
  }

  private void updateRatio() {
    double w = imgOrg.getWidth();
    double h = imgOrg.getHeight();
    w *= ratio;
    h *= ratio;
    imgRes = new Image(imgUrl, w, h, true, false);
  }

  private void updateView() {
    Canvas cMap = new Canvas(imgRes.getWidth(), imgRes.getHeight());
    GraphicsContext gc = cMap.getGraphicsContext2D();
    gc.drawImage(imgRes, 0, 0);


    Canvas cPoint = new Canvas(imgRes.getWidth(), imgRes.getHeight());
    final GraphicsContext gcPoint = cPoint.getGraphicsContext2D();
    gcPoint.setFill(Color.BLUE);
    cPoint.addEventHandler(MouseEvent.MOUSE_MOVED,
        e -> updateLocationLabel(e.getX(), e.getY()));
    cPoint.addEventHandler(MouseEvent.MOUSE_PRESSED,
        e -> mapClicked(e.getX(), e.getY(), gcPoint));

    Pane pane = new Pane();
    pane.getChildren().add(cMap);
    pane.getChildren().add(cPoint);
    cPoint.toFront();

    pnlScr.setContent(pane);

  }

  private void updateLocationLabel(double x, double y) {
    lblPoint.setText(
        String.format("%.1f x %.1f", x, y));

    if (aps != null && aps[1] != null) {
      Point crds = convertPointToCoordinates(x, y);
      lblGps.setText(
          String.format("%.8f x %.8f", crds.x, crds.y));
    }
  }

  private Point convertPointToCoordinates(double x, double y) {
    Point p = null;

    AlignPoint u = aps[0];
    AlignPoint v = aps[1];

    double latA = (u.lat - v.lat) / (u.y - v.y);
    double latB = u.lat - latA * u.y;

    double lngA = (u.lng - v.lng) / (u.x - v.x);
    double lngB = u.lng - lngA * u.x;

    Point ret = new Point( latA * y + latB, lngA * x + lngB);
    return ret;
  }

  private void mapClicked(double x, double y, GraphicsContext gc) {
    double[] latLng = askForLatLng();
    if (latLng == null)
      return;

    if (aps[1] != null) {
      aps[0] = null;
      aps[1] = null;
    }

    if (aps[0] == null)
      aps[0] = new AlignPoint(x, y, latLng[0], latLng[1]);
    else {
      aps[1] = new AlignPoint(x, y, latLng[0], latLng[1]);
      chkMode.setValue(Mode.navaid);
    }
  }

  private double[] askForLatLng() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(Main.class.getResource("LatLngDialog.fxml"));
    AnchorPane page = null;
    try {
      page = (AnchorPane) loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Create the dialog Stage.
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Enter point coordinates");
    dialogStage.initModality(Modality.WINDOW_MODAL);
    //dialogStage.initOwner(primaryStage);
    Scene scene = new Scene(page);
    dialogStage.setScene(scene);

    // Set the person into the controller.
    LatLngDialog controller = loader.getController();
    controller.setDialogStage(dialogStage);

    // Show the dialog and wait until the user closes it
    dialogStage.showAndWait();

    return controller.getLatLng();
  }
}
