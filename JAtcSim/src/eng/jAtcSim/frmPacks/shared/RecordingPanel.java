package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.recording.Settings;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.startup.extenders.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class RecordingPanel extends JPanel {

  private NumericUpDownExtender nudInterval;
  private NumericUpDownExtender nudWidth;
  private NumericUpDownExtender nudHeight;
  private NumericUpDownExtender nudJpgQuality;
  private XmlFileSelectorExtender fleFolder;
  private JPanel pnlBefore;
  private JPanel pnlDuring;
  private EventAnonymous<Settings> recordingStarted = new EventAnonymous<>();
  private EventAnonymousSimple recordingStopped = new EventAnonymousSimple();
  private EventAnonymousSimple viewRecordingFolderRequest = new EventAnonymousSimple();
  private XComboBoxExtender<String> cmbImageType;
  private XmlFileSelectorExtender fleFfmpeg;
  private XmlFileSelectorExtender fleVideoIn;
  private NumericUpDownExtender nudFPS;
  private XmlFileSelectorExtender fleVideoOut;
  private XComboBoxExtender<String> cmbVideoImageType;

  public RecordingPanel(Settings settings) {
    pnlBefore = buildBeforePanel(settings);
    pnlDuring = buildDuringPanel();
//    JPanel pnlVideo = buildVideoPanel(settings);
    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.center, 4, pnlBefore, pnlDuring); //, pnlVideo);
    adjustAvailibility(settings != null);
  }

  public EventAnonymous<Settings> getRecordingStarted() {
    return recordingStarted;
  }

  public EventAnonymousSimple getRecordingStopped() {
    return recordingStopped;
  }

  public EventAnonymousSimple getViewRecordingFolderRequest() {
    return viewRecordingFolderRequest;
  }

  private void adjustAvailibility(boolean isRecording) {
    ComponentUtils.adjustComponentTree(pnlBefore, q -> q.setEnabled(!isRecording));
    ComponentUtils.adjustComponentTree(pnlDuring, q -> q.setEnabled(isRecording));
  }

  private JPanel buildDuringPanel() {
    JPanel ret = new JPanel();
    ret.setBorder(BorderFactory.createTitledBorder("During recording:"));

    LayoutManager.fillBoxPanel(ret, LayoutManager.eHorizontalAlign.center, 4,
        LayoutManager.createBorderedPanel(4,
            SwingFactory.createButton("Stop recording", this::btnStopRecording_click)),
        LayoutManager.createBorderedPanel(4,
            SwingFactory.createButton("View results", this::btnViewResultFolder_click)));

    return ret;
  }

  private JPanel buildBeforePanel(Settings sett) {
    JPanel ret = new JPanel();
    ret.setBorder(BorderFactory.createTitledBorder("Before recording:"));

    nudInterval = new NumericUpDownExtender(new JSpinner(), 1, 3600, 1, 1);
    fleFolder = new XmlFileSelectorExtender(SwingFactory.FileDialogType.folder);
    nudWidth = new NumericUpDownExtender(new JSpinner(), 100, 4000, 1600, 100);
    nudHeight = new NumericUpDownExtender(new JSpinner(), 100, 4000, 900, 100);
    nudJpgQuality = new NumericUpDownExtender(new JSpinner(), 0, 100, 95, 5);
    cmbImageType = new XComboBoxExtender<>(new String[]{"jpg", "png", "bmp"});
    if (sett != null) {
      nudInterval.setValue(sett.getInterval());
      fleFolder.setFileName(sett.getPath());
      nudWidth.setValue(sett.getWidth());
      nudHeight.setValue(sett.getHeight());
      cmbImageType.setSelectedItem(sett.getImageType());
    }

    JPanel tmpA = LayoutManager.createFormPanel(6, 2,
        new JLabel("Target folder:"),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, 4, fleFolder.getTextControl(), fleFolder.getButtonControl()),
        new JLabel("Interval (sim sec):"),
        nudInterval.getControl(),
        new JLabel("Window width (px):"),
        nudWidth.getControl(),
        new JLabel("Window height (px):"),
        nudHeight.getControl(),
        new JLabel("Image type:"),
        cmbImageType.getControl(),
        new JLabel("JPG quality %:"),
        nudJpgQuality.getControl()
    );

    JPanel tmpB = LayoutManager.createBorderedPanel(4,
        SwingFactory.createButton("Start recording", this::btnStartRecording_click));

    LayoutManager.fillBoxPanel(ret, LayoutManager.eHorizontalAlign.center, 4, tmpA, tmpB);

    return ret;
  }

//  private JPanel buildVideoPanel(Settings sett) {
//
//    fleFfmpeg = new XmlFileSelectorExtender(SwingFactory.FileDialogType.ffmpeg);
//    fleVideoIn = new XmlFileSelectorExtender(SwingFactory.FileDialogType.folder);
//    fleVideoOut = new XmlFileSelectorExtender(SwingFactory.FileDialogType.video);
//    nudFPS = new NumericUpDownExtender(new JSpinner(), 1, 100, 24, 1);
//    cmbVideoImageType = new XComboBoxExtender<>(new String[]{"jpg", "png", "bmp"});
//    cmbImageType.getSelectedItemChanged().add(this::cmbImageType_selectedItemChanged);
//
//    if (sett != null){
//      fleVideoIn.setFileName(sett.getPath());
//      fleVideoOut.setFileName(
//          Paths.get(sett.getPath(), "videoOut.mp4").toString());
//    }
//
//    JPanel ret = LayoutManager.createFormPanel(5, 2,
//        new JLabel("Location of ffmpeg.exe:"),
//        fleFfmpeg.getControl(),
//        new JLabel("Location of images:"),
//        fleVideoIn.getControl(),
//        new JLabel("Images type:"),
//        cmbVideoImageType.getControl(),
//        new JLabel("Output video file:"),
//        fleVideoOut.getControl(),
//        new JLabel("FPS:"),
//        nudFPS.getControl()
//    );
//
//    ret = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center  , 4,ret,
//        SwingFactory.createButton("Convert", this::btnConvert_click));
//
//    ret.setBorder(BorderFactory.createTitledBorder("Video converter:"));
//    return ret;
//  }

  private void cmbImageType_selectedItemChanged(Object o) {
    cmbVideoImageType.setSelectedItem(cmbImageType.getSelectedItem());
  }

  private void btnConvert_click(ActionEvent actionEvent) {
    String inPath = Paths.get(fleVideoIn.getFileName(), "%05d." + cmbVideoImageType.getSelectedItem()).toString();
    ProcessBuilder pb = new ProcessBuilder("cmd",
        fleFfmpeg.getFileName(),
        "-r", "1",
        "-i", inPath,
        "-r", Integer.toString(nudFPS.getValue()),
        fleVideoOut.getFileName());
    try {
      pb.start();
    } catch (IOException e) {
      MessageBox.show("Failed to start conversion. " + ExceptionUtils.toFullString(e, " => "), "Conversion failed....");
    }
  }

  private void btnStartRecording_click(ActionEvent actionEvent) {
    if (!checkSanity()) return;
    Settings sett = new Settings(
        fleFolder.getFileName(), nudInterval.getValue(), nudWidth.getValue(), nudHeight.getValue(),
        cmbImageType.getSelectedItem(),
        nudJpgQuality.getValue() / 100f);
    recordingStarted.raise(sett);
    adjustAvailibility(true);
  }

  private boolean checkSanity() {
    File f = fleFolder.getFile();
    if (f.exists() == false) {
      MessageBox.show("Path " + f.getAbsolutePath() + " does not exist.", "Error...");
      return false;
    }
    if (f.canWrite() == false) {
      MessageBox.show("Path " + f.getAbsolutePath() + " is not writable.", "Error...");
      return false;
    }
    if (NumberUtils.isBetweenOrEqual(nudInterval.getMinimum(), nudInterval.getValue(), nudInterval.getMaximum()) == false) {
      MessageBox.show("Interval is not in valid range.", "Error...");
      return false;
    }
    if (NumberUtils.isBetweenOrEqual(nudWidth.getMinimum(), nudWidth.getValue(), nudWidth.getMaximum()) == false) {
      MessageBox.show("Width is not in valid range.", "Error...");
      return false;
    }
    if (NumberUtils.isBetweenOrEqual(nudHeight.getMinimum(), nudHeight.getValue(), nudHeight.getMaximum()) == false) {
      MessageBox.show("Height is not in valid range.", "Error...");
      return false;
    }
    if (cmbImageType.getSelectedIndex() < 0) {
      MessageBox.show("Image type not selected.", "Error...");
      return false;
    }
    return true;
  }

  private void btnViewResultFolder_click(ActionEvent actionEvent) {
    viewRecordingFolderRequest.raise();
  }

  private void btnStopRecording_click(ActionEvent actionEvent) {
    recordingStopped.raise();
    adjustAvailibility(false);
  }
}
