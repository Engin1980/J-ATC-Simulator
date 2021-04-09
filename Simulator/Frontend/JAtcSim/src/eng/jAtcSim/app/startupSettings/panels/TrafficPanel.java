/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.swing.extenders.DisplayItem;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.app.extenders.NumericUpDownExtender;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.frmPacks.shared.FrmTrafficBarGraph;
import eng.jAtcSim.newLib.gameSim.game.sources.SourceFactory;
import eng.jAtcSim.newLib.gameSim.game.sources.TrafficSource;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TrafficPanel extends JStartupPanel {

  private JButton btnAnalyseTraffic;
  private javax.swing.JCheckBox chkAllowDelays;
  private ComboBoxExtender<DisplayItem<Double>> cmbEmergencyProbability;
  private XmlFileSelectorExtender fleTraffic;
  private NumericUpDownExtender nudMaxPlanes;
  private NumericUpDownExtender nudTrafficDensity;

  public TrafficPanel() {
    initComponents();
  }

  @Override
  public void fillBySettings(StartupSettings settings) {

    fleTraffic.setFileName(settings.files.trafficXmlFile);

    nudMaxPlanes.setValue(settings.traffic.maxPlanes);
    nudTrafficDensity.setValue((int) (settings.traffic.densityPercentage * 100));

    chkAllowDelays.setSelected(settings.traffic.allowDelays);

    setCmbEmergencyProbabilityByClosestValue(settings.traffic.emergencyPerDayProbability);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.files.trafficXmlFile = fleTraffic.getFileName();

    settings.traffic.maxPlanes = nudMaxPlanes.getValue();
    settings.traffic.densityPercentage = nudTrafficDensity.getValue() / 100d;

    settings.traffic.allowDelays = chkAllowDelays.isSelected();

    settings.traffic.emergencyPerDayProbability = cmbEmergencyProbability.getSelectedItem().value;
  }

  private void btnAnalyseTraffic_click(ActionEvent actionEvent) {
    ITrafficModel tfc;
    try {
      tfc = getCurrentTraffic();
    } catch (Exception e) {
      Context.getApp().getAppLog().write(LogItemType.critical, "Failed to load traffic. " + ExceptionUtils.toFullString(e));
      MessageBox.show("Failed to obtain the traffic. Check the app log for more info.", "Failed to load traffic...");
      return;
    }

//    FrmTrafficHistogram frm = new FrmTrafficHistogram();
    FrmTrafficBarGraph frm = new FrmTrafficBarGraph();
    frm.init(tfc, "Movements histogram");
    frm.setVisible(true);
  }

  private void createComponents() {

    btnAnalyseTraffic = SwingFactory.createButton("Analyse traffic movements", this::btnAnalyseTraffic_click);

    fleTraffic = new XmlFileSelectorExtender(SwingFactory.FileDialogType.traffic);

    chkAllowDelays = new javax.swing.JCheckBox("Allow traffic delays");

    nudMaxPlanes = new NumericUpDownExtender(new JSpinner(), 1, 100, 15, 1);
    nudTrafficDensity = new NumericUpDownExtender(new JSpinner(), 0, 100, 100, 1);

    cmbEmergencyProbability = new ComboBoxExtender<>(q -> q.label);
    setCmbEmergencyProbabilityModel();
  }

  private JPanel createGlobalPanel() {
    return LayoutManager.createFormPanel(2, 2,
            new JLabel("Max planes count:"),
            LayoutManager.createFlowPanel(
                    LayoutManager.eVerticalAlign.baseline,
                    DISTANCE,
                    nudMaxPlanes.getControl(),
                    new JLabel("Traffic density (%):"), nudTrafficDensity.getControl(),
                    chkAllowDelays),
            new JLabel("Emergencies probability:"),
            LayoutManager.createFlowPanel(
                    LayoutManager.eVerticalAlign.baseline,
                    DISTANCE,
                    cmbEmergencyProbability.getControl()));
  }

  private void createLayout() {

    JPanel pnlGlobalTrafficSettings = createGlobalPanel();
    JPanel pnlTrafficFile = createTrafficFilePanel();

    pnlGlobalTrafficSettings = LayoutManager.createBorderedPanel(8, pnlGlobalTrafficSettings);

    LayoutManager.setPanelBorderText(pnlGlobalTrafficSettings, "Traffic settings:");
    LayoutManager.setPanelBorderText(pnlTrafficFile, "Traffic source:");

    pnlGlobalTrafficSettings.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    pnlTrafficFile.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    JPanel pnlMain = LayoutManager.createFormPanel(2, 1,
            pnlTrafficFile, pnlGlobalTrafficSettings);

    pnlMain = LayoutManager.createBorderedPanel(DISTANCE, pnlMain);

    this.add(pnlMain);
  }

  private JPanel createTrafficFilePanel() {
    return LayoutManager.createFormPanel(2, 2,
            new JLabel("Load from file"),
            LayoutManager.createFlowPanel(fleTraffic.getTextControl(), fleTraffic.getButtonControl()),
            null, btnAnalyseTraffic);
  }

  private ITrafficModel getCurrentTraffic() {
    ITrafficModel ret;
    TrafficSource trafficSource = SourceFactory.createTrafficXmlSource(fleTraffic.getFileName());
    trafficSource.init();
    ret = trafficSource.getContent();

    return ret;
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    createComponents();
    createLayout();
  }

  private void setCmbEmergencyProbabilityByClosestValue(double emergencyPerDayProbability) {
    double minDiff = Double.MAX_VALUE;
    int bestIndex = 0;
    for (int i = 0; i < cmbEmergencyProbability.getItems().size(); i++) {
      double item = cmbEmergencyProbability.getItem(i).value;
      if (Math.abs(item - emergencyPerDayProbability) < minDiff) {
        minDiff = Math.abs(item - emergencyPerDayProbability);
        bestIndex = i;
      }
    }
    cmbEmergencyProbability.setSelectedIndex(bestIndex);
  }

  private void setCmbEmergencyProbabilityModel() {
    IList<DisplayItem<Double>> lst = new EList<>();

    lst.add(new DisplayItem<>("Off", -1d));
    lst.add(new DisplayItem<>("Once per hour", 24d));
    lst.add(new DisplayItem<>("Once per three hours", 8d));
    lst.add(new DisplayItem<>("Once per six hours", 4d));
    lst.add(new DisplayItem<>("Once per twelve hours", 2d));
    lst.add(new DisplayItem<>("Once per day", 1d));
    lst.add(new DisplayItem<>("Once per three days", 1 / 3d));
    lst.add(new DisplayItem<>("Once per week", 1 / 7d));
    lst.add(new DisplayItem<>("Once per two weeks", 1 / 14d));
    lst.add(new DisplayItem<>("Once per month", 1 / 30d));

    cmbEmergencyProbability.addItems(lst);
  }
}
