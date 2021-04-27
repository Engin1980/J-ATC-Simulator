package eng.coordinatesViewer;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eXmlSerialization.XmlSerializer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main {

  public enum Mode {
    navaid,
    border,
    align
  }

  @FXML
  TextArea txtOut;
  private Project project;
  private AlignPoint[] aps = null;
  private String url = "file:D:/lfmm_mrva.png";
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
    chkMode.setValue(Mode.navaid);
  }

  @FXML
  public void btnLoadImage_click() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load project from:");
    File file = fileChooser.showOpenDialog(null);
    String url = "file:" + file.toString().replace("\\", "/");
    project = Project.create(url);
    project.getRedrawRequiredEvent().add(() -> updateView());
    updateView();
  }

  @FXML
  public void btnZoomReset_click() {
    project.zoomReset();
  }

  @FXML
  public void btnLoadProject_click() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load project from:");
    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      XmlSerializer ser = new XmlSerializer();
      try {
        XDocument doc = XDocument.load(file.toString());
        this.project = ser.deserialize(doc.getRoot(), Project.class);
      } catch (Exception e) {
        throw new ApplicationException("Unable to load " + file.toString());
      }
      this.project.reinit();
      project.getRedrawRequiredEvent().add(() -> updateView());
      updateView();
    }
  }

  @FXML
  public void btnSaveProject_click() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save project as: ");

    File res = fileChooser.showSaveDialog(null);
    if (res != null) {
      XmlSerializer ser = new XmlSerializer();
      XDocument doc = new XDocument(new XElement("root"));
      ser.serialize(this.project, doc.getRoot());
      try {
        doc.save(res.toString());
      } catch (EXmlException e) {
        throw new ApplicationException("Unable to save " + res.toString());
      }
    }
  }

  @FXML
  void btnZoomIn_click() {
    project.zoomIn();
  }

  @FXML
  void btnZoomOut_click() {
    project.zoomOut();
  }

  private void updateView() {
    Image imgRes = project.getImage();

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

    project.getBorderPoints().forEach(q-> drawPoint(q, Color.DARKBLUE, gc ));
    project.getNavaidPoints().forEach(q-> drawPoint(q, Color.DARKRED, gc ));
  }

  private void updateLocationLabel(double x, double y) {
    Point p = new Point(x, y);
    p = project.convertRelativeToAbsolutePoint(p);
    lblPoint.setText(
        String.format("%.0f x %.0f", p.x, p.y));

    p = project.convertPointToCoordinate(p);
    lblGps.setText(
        String.format("%.8f x %.8f", p.x, p.y));
  }

  private void mapClicked(double x, double y, GraphicsContext gc) {
    switch (chkMode.getValue()) {
      case align:
        doAlignPoint(new Point(x, y));
        break;
      case navaid:
        doNavaidPoint(new Point(x, y), gc);
        break;
      case border:
        doBorderPoint(new Point(x, y), gc);
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void doNavaidPoint(Point point, GraphicsContext gc) {
    String name = askForName();
    if (name == null) return;

    Point absolutePoint = project.convertRelativeToAbsolutePoint(point);
    Point aligned = project.getNavaidPoints().tryAlignToExisting(absolutePoint, project.getZoomRatio());
    if (aligned != null) {
      absolutePoint = aligned;
    } else {
      drawPoint(absolutePoint, Color.RED, gc);
      project.getNavaidPoints().add(absolutePoint);
    }
    Point coordinate = project.convertPointToCoordinate(absolutePoint);
    String line = String.format("<navaid name=\"%s\" kind=\"fix\" coordinate=\"%.6f %.6f\" />", name, coordinate.x, coordinate.y);
    line = line.replace(',', '.' );
    printLine(line);
  }

  private void doBorderPoint(Point point, GraphicsContext gc) {
    Point absolutePoint = project.convertRelativeToAbsolutePoint(point);
    Point aligned = project.getBorderPoints().tryAlignToExisting(absolutePoint, project.getZoomRatio());
    if (aligned != null) {
      absolutePoint = aligned;
    } else {
      drawPoint(absolutePoint, Color.BLUE, gc);
      project.getBorderPoints().add(absolutePoint);
    }
    Point coordinate = project.convertPointToCoordinate(absolutePoint);
    String line = String.format("<point coordinate=\"%.6f %.6f\" />", coordinate.x, coordinate.y);
    line = line.replace(',', '.');
    printLine(line);
  }

  private void drawPoint(Point absolutePoint, Color color, GraphicsContext gc) {
    Point relativePoint = project.convertAbsoluteToRelativepoint(absolutePoint);
    gc.setFill(color);
    gc.fillOval(relativePoint.x - 10, relativePoint.y - 10, 20, 20);
  }

  private void printLine(String line) {
    txtOut.appendText("\n" + line);
  }

  private void doAlignPoint(Point point) {
    double[] latLng = askForLatLng();
    if (latLng == null)
      return;

    point = project.convertRelativeToAbsolutePoint(point);
    if (aps == null) {
      aps = new AlignPoint[2];
      aps[0] = new AlignPoint(point.x, point.y, latLng[0], latLng[1]);
    } else {
      aps[1] = new AlignPoint(point.x, point.y, latLng[0], latLng[1]);
      project.alignGps(aps[0], aps[1]);
      chkMode.setValue(Mode.navaid);
      aps = null;
    }
  }

  private double[] askForLatLng() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(Main.class.getResource("LatLngDialog.fxml"));
    AnchorPane page = null;
    try {
      page = loader.load();
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

  private String askForName() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(Main.class.getResource("NavaidDialog.fxml"));
    AnchorPane page = null;
    try {
      page = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Create the dialog Stage.
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Enter navaid info");
    dialogStage.initModality(Modality.WINDOW_MODAL);
    Scene scene = new Scene(page);
    dialogStage.setScene(scene);

    // Set the person into the controller.
    NavaidDialog controller = loader.getController();
    controller.setDialogStage(dialogStage);

    // Show the dialog and wait until the user closes it
    dialogStage.showAndWait();

    return controller.getNavaidName();
  }
}
