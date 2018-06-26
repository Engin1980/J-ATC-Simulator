package eng.jAtcImagesToVideoConverter;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ExceptionUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

interface Validator<T> {
  boolean test(T value);
}

public class FrmMain {

  @FXML
  private Button btnOut;

  @FXML
  private TextField txtFfmpeg;

  @FXML
  private TextField txtCmd;

  @FXML
  private Slider sldFramerate;

  @FXML
  private TextField txtOut;

  @FXML
  private ComboBox<String> cmbExtension;

  @FXML
  private Button btnConvert;

  @FXML
  private Button btnIn;

  @FXML
  private TextField txtFramerate;

  @FXML
  private Button btnFfmpeg;

  @FXML
  private TextArea txtPrint;

  @FXML
  private TextField txtIn;
  private Stage stage;

  private List<TextFieldValidator> validators = new ArrayList();

  public void setStageAndSetupListeners(Stage primaryStage) {
    this.stage = primaryStage;
  }

  @FXML
  public void initialize() {
    sldFramerate.valueProperty().addListener(
        (observable, oldValue, newValue) -> txtFramerate.setText(Integer.toString(newValue.intValue())));

    validators.add(
        new TextFieldValidator(txtFfmpeg, q -> q.toLowerCase().endsWith("ffmpeg.exe") && new File(q).exists())
    );
    validators.add(
        new TextFieldValidator(txtIn, q -> new File(q).exists() && new File(q).isDirectory())
    );
    validators.add(
        new TextFieldValidator(txtFramerate, q -> Pattern.compile("\\d+").matcher(q).find())
    );
    validators.add(
        new TextFieldValidator(txtOut, q ->
        {
          try {
            Path p = Paths.get(q);
            File f = p.getParent().toFile();
            return f.exists();
          } catch (Throwable t) {
            return false;
          }
        }
        ));

    cmbExtension.getItems().addAll("png", "jpg", "bmp");

    loadPref();
  }

  @FXML
  void btnConvert_click(ActionEvent event) {
//    Runtime r = Runtime.getRuntime();
//    try {
//      r.exec("cmd /c echo nazdar");
//    } catch (IOException e) {
//      System.out.println("Fucking fail." + e.getClass().getName() + ": " + e.getMessage());
//    }

    IList<String> cmds = new EList();

    cmds.add(txtFfmpeg.getText());

    cmds.add("-r");
    cmds.add(txtFramerate.getText());

    cmds.add("-i");
    Path pIn = Paths.get(txtIn.getText(), "%05d." + cmbExtension.getValue());
    cmds.add(pIn.toString());

    cmds.add("-q:v");
    cmds.add("3");

    cmds.add("-c:v");
    cmds.add("libx264");

    cmds.add(txtOut.getText());

    EStringBuilder esb = new EStringBuilder();
    esb.appendItems(cmds, " " );
    txtCmd.setText(esb.toString());

    ProcessBuilder builder = new ProcessBuilder(cmds.toList());
    builder.redirectErrorStream(true);
    final Process process;
    try {
      process = builder.start();
      watch(process);
    } catch (IOException e) {
      txtPrint.setText("Nepodařilo se spustit ffmpeg. " + ExceptionUtils.toFullString(e));
      return;
    }
  }

  @FXML
  void btnSaveSettings_click(ActionEvent event) {
    savePref();
  }

  private void watch(final Process process) {
    new Thread(() -> {
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      try {
        while ((line = input.readLine()) != null) {
          txtPrint.appendText(line);
          txtPrint.appendText("\n");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  @FXML
  void btnFfmpeg_click(ActionEvent event) {
    String tmp = getFile(txtFfmpeg.getText(), "ffmpeg.exe");
    if (tmp != null)
      txtFfmpeg.setText(tmp);
  }

  private String getFile(String initial, String type) {
    FileChooser fc = new FileChooser();
    fc.setTitle("Select file");
    if (initial != null && initial.length() > 0)
      fc.setInitialFileName(initial);
    fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("File", type));
    File fle = fc.showOpenDialog(stage);
    if (fle == null)
      return null;
    else
      return fle.getAbsolutePath();
  }

  @FXML
  void btnIn_click(ActionEvent event) {
    String tmp = getFolder(txtIn.getText());
    if (tmp != null)
      txtIn.setText(tmp);
  }

  private String getFolder(String initial) {
    DirectoryChooser fc = new DirectoryChooser();
    fc.setTitle("Select file");
    if (initial != null && initial.length() > 0)
      fc.setInitialDirectory(new File(initial));
    File fle = fc.showDialog(stage);
    if (fle == null)
      return null;
    else
      return fle.getAbsolutePath();
  }

  @FXML
  void btnOut_click(ActionEvent event) {
    String tmp = getFile(txtOut.getText(), ".mp4");
    if (tmp != null)
      txtOut.setText(tmp);
  }

  private void loadPref(){
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    String tmp;

    tmp = prefs.get("ffmpeg", "");
    txtFfmpeg.setText(tmp);

    tmp = prefs.get("in", "");
    txtIn.setText(tmp);

    tmp = prefs.get("framerate", "24");
    txtFramerate.setText(tmp);

    tmp = prefs.get("inExt", "png");
    cmbExtension.setValue(tmp);

    tmp = prefs.get("out", "");
    txtOut.setText(tmp);
  }

  private void savePref(){
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    prefs.put("ffmpeg", txtFfmpeg.getText());
    prefs.put("in", txtIn.getText());
    prefs.put("framerate",txtFramerate.getText() );
    prefs.put("inExt", cmbExtension.getValue());
    prefs.put("out", txtOut.getText());
  }
}



class TextFieldValidator {
  private final TextField txt;
  private final Validator<String> validator;

  public TextFieldValidator(TextField txt, Validator<String> validator) {
    this.txt = txt;
    this.validator = validator;
    this.txt.textProperty().addListener((observable, oldValue, newValue) -> {
      if (validator.test(newValue)) {
        txt.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
      } else {
        txt.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
      }
    });
  }
}