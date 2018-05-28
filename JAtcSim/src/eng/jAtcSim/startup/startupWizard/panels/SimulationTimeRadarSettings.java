package eng.jAtcSim.startup.startupWizard.panels;

import eng.eSystem.xmlSerialization.Settings;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.extenders.NumericUpDownExtender;
import eng.jAtcSim.startup.extenders.TimeExtender;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Marek Vajgl
 */
public class SimulationTimeRadarSettings extends JStartupPanel {

  private NumericUpDownExtender nudSecondLength;
  private TimeExtender tmeTime;

  public SimulationTimeRadarSettings() {
    initComponents();
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    this.nudSecondLength.setValue(settings.simulation.secondLengthInMs);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.simulation.secondLengthInMs = this.nudSecondLength.getValue();
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();
  }

  private void createLayout() {

    JPanel pnl =
        LayoutManager.createFormPanel(2, 2,
            new JLabel("Simulation speed:"), this.nudSecondLength.getControl(),
            new JLabel("Startup time:"), tmeTime.getControl()
        );

    pnl = LayoutManager.createBorderedPanel(10, pnl);

    this.add(pnl);
  }

  private void createComponents() {
    nudSecondLength = new NumericUpDownExtender(new JSpinner(), 100, 3000, 1000, 200);
    java.time.LocalTime tm = java.time.LocalTime.now();
    tmeTime = new TimeExtender(tm);
  }
}

