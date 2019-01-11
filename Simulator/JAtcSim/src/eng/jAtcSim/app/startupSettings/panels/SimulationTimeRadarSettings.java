package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.swing.extenders.DisplayItem;
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
  private ComboBoxExtender<DisplayItem<String>> cmbRadarClass;

  public SimulationTimeRadarSettings() {
    initComponents();
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    this.nudSecondLength.setValue(settings.simulation.secondLengthInMs);
    this.tmeTime.setTime(settings.recent.time);
    this.cmbRadarClass.setSelectedItem(new DisplayItem<>("-", settings.radar.packClass));
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.simulation.secondLengthInMs = this.nudSecondLength.getValue();
    settings.recent.time = this.tmeTime.getTime();
    settings.radar.packClass = this.cmbRadarClass.getSelectedItem().value;
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();
  }

  private void createLayout() {

    JButton btnNow = new JButton("Set current");
    btnNow.addActionListener(q -> {
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
    LayoutManager.setFixedWidth(tmeTime.getControl(), 100);

    IList<DisplayItem<String>> rdrTypes = new EList<>();
    rdrTypes.add(new DisplayItem<>("SDI", "eng.jAtcSim.frmPacks.sdi.Pack"));
    rdrTypes.add(new DisplayItem<>("MDI", "eng.jAtcSim.frmPacks.mdi.Pack"));
    cmbRadarClass = new ComboBoxExtender<>(q -> q.label, rdrTypes);
  }
}

