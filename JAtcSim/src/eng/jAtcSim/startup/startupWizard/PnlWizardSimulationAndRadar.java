package eng.jAtcSim.startup.startupWizard;

import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.extenders.NumericUpDownExtender;
import eng.jAtcSim.startup.startupWizard.FrmWizardFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marek Vajgl
 */
public class PnlWizardSimulationAndRadar extends JWizardPanel {

  private static List<String> packList = new ArrayList();
  private javax.swing.JLabel lblSimulationSpeed;
  private javax.swing.JSpinner nudSecondLengthInMs;
  private NumericUpDownExtender nudeSecondLength;

  static {
    packList.add("Pack");
  }

  /**
   * Creates new form FrmWizardRadar
   */
  public PnlWizardSimulationAndRadar() {
    initComponents();
    initExtenders();
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();
  }

  private void createLayout() {

    JPanel pnl =
        LayoutManager.createFormPanel(1, 2, lblSimulationSpeed, nudSecondLengthInMs);

    pnl = LayoutManager.createBorderedPanel(10, pnl);

    this.add(pnl);
  }

  private void createComponents() {
    lblSimulationSpeed = new javax.swing.JLabel();
    nudSecondLengthInMs = new javax.swing.JSpinner();
    lblSimulationSpeed.setText("Simulation speed:");
  }

  @Override
  protected void fillBySettings(StartupSettings settings) {

    this.nudeSecondLength.setValue(settings.simulation.secondLengthInMs);
  }

  @Override
  protected boolean doWizardValidation() {
    return true;
  }

  @Override
  void fillSettingsBy(StartupSettings settings) {
    settings.simulation.secondLengthInMs = this.nudeSecondLength.getValue();
  }

  private void initExtenders() {
    nudeSecondLength = new NumericUpDownExtender(nudSecondLengthInMs, 200, 3000, 700, 100);
  }
}

