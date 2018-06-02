package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.recording.Settings;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.startup.extenders.NumericUpDownExtender;
import eng.jAtcSim.startup.extenders.SwingFactory;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RecordingPanel extends JPanel {

  private Settings sett;
  private NumericUpDownExtender nudInterval;
  private XmlFileSelectorExtender fleFolder;
  private JPanel pnlBefore;
  private JPanel pnlDuring;

  public RecordingPanel(Settings settings) {
    this.sett = settings;
    pnlBefore = buildBeforePanel();
    pnlDuring = buildDuringPanel();
    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.center, 4, pnlBefore, pnlDuring);
    adjustAvailibility();
  }

  private void adjustAvailibility() {
    ComponentUtils.adjustComponentTree(pnlBefore, q -> q.setEnabled(sett == null));
    ComponentUtils.adjustComponentTree(pnlDuring, q -> q.setEnabled(sett != null));
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

  private JPanel buildBeforePanel() {
    JPanel ret = new JPanel();
    ret.setBorder(BorderFactory.createTitledBorder("Before recording:"));

    nudInterval = new NumericUpDownExtender(new JSpinner(), 1, 3600, 1, 1);
    fleFolder = new XmlFileSelectorExtender(SwingFactory.FileDialogType.folder);
    if (sett != null) {
      nudInterval.setValue(sett.getInterval());
      fleFolder.setFileName(sett.getPath());
    }


    JPanel tmpA = LayoutManager.createFormPanel(2, 2,
        new JLabel("Target folder:"),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, 4, fleFolder.getTextControl(), fleFolder.getButtonControl()),
        new JLabel("Interval (sim sec):"),
        nudInterval.getControl());

    JPanel tmpB = LayoutManager.createBorderedPanel(4,
        SwingFactory.createButton("Start recording", this::btnStartRecording_click));

    LayoutManager.fillBoxPanel(ret, LayoutManager.eHorizontalAlign.center, 4, tmpA, tmpB);

    return ret;
  }

  private void btnStartRecording_click(ActionEvent actionEvent) {
  }

  private void btnViewResultFolder_click(ActionEvent actionEvent) {
  }

  private void btnStopRecording_click(ActionEvent actionEvent) {
  }
}
