package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.app.extenders.XComboBoxExtender;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.NumericUpDownExtender;
import eng.jAtcSim.app.extenders.TimeExtender;

import javax.swing.*;
import java.time.LocalTime;

/**
 * @author Marek Vajgl
 */
public class SimulationTimeRadarSettings extends JStartupPanel {

  private NumericUpDownExtender nudSecondLength;
  private TimeExtender tmeTime;
  private XComboBoxExtender<String> cmbRadarClass;

  public SimulationTimeRadarSettings() {
    initComponents();
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    this.nudSecondLength.setValue(settings.simulation.secondLengthInMs);
    this.tmeTime.setTime(settings.recent.time);
    this.cmbRadarClass.setSelectedItem(settings.radar.packClass);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.simulation.secondLengthInMs = this.nudSecondLength.getValue();
    settings.recent.time = this.tmeTime.getTime();
    settings.radar.packClass = this.cmbRadarClass.getSelectedItem();
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();
  }

  private void createLayout() {

    JButton btnNow = new JButton("Set current");
    btnNow.addActionListener(q->{
      tmeTime.setTime(LocalTime.now());
    });

    JPanel pnl =
        LayoutManager.createFormPanel(3, 2,
            new JLabel("Simulation speed:"), this.nudSecondLength.getControl(),
            new JLabel("Startup time:"), LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE, tmeTime.getControl(), btnNow),
            new JLabel("Radar screen type:"), cmbRadarClass.getControl()
        );

    pnl = LayoutManager.createBorderedPanel(10, pnl);

    this.add(pnl);
  }

  private void createComponents() {
    nudSecondLength = new NumericUpDownExtender(new JSpinner(), 100, 3000, 1000, 200);
    java.time.LocalTime tm = java.time.LocalTime.now();
    tmeTime = new TimeExtender(tm);

    IMap<String, String> rdrTypes = new EMap<>();
    rdrTypes.set("SDI", "eng.jAtcSim.frmPacks.sdi.Pack");
    rdrTypes.set("MDI", "eng.jAtcSim.frmPacks.mdi.Pack");
    cmbRadarClass = new XComboBoxExtender<>(rdrTypes);
  }
}

