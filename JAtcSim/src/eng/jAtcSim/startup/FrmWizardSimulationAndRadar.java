package eng.jAtcSim.startup;

import eng.jAtcSim.startup.extenders.NumericUpDownExtender;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marek Vajgl
 */
public class FrmWizardSimulationAndRadar extends FrmWizardFrame {

  private static List<String> packList = new ArrayList();
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnContinue;
  private javax.swing.JLabel lblSimulationSpeed;
  private javax.swing.JSpinner nudSecondLengthInMs;
  // End of variables declaration//GEN-END:variables
  private NumericUpDownExtender nudeSecondLength;

  static {
    packList.add("Pack");
  }

  /**
   * Creates new form FrmWizardRadar
   */
  public FrmWizardSimulationAndRadar() {
    initComponents();
    initExtenders();
  }

  private void initComponents() {
    this.setTitle("");
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();

    pack();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
  }

  private void createLayout() {

    JPanel pnl =
        LayoutManager.createFormPanel(1, 2, lblSimulationSpeed, nudSecondLengthInMs);

    pnl = super.wrapWithContinueButton(pnl, btnContinue);
    pnl = LayoutManager.createBorderedPanel(10, pnl);

    this.setContentPane(pnl);
  }

  private void createComponents() {
    lblSimulationSpeed = new javax.swing.JLabel();
    nudSecondLengthInMs = new javax.swing.JSpinner();
    btnContinue = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    lblSimulationSpeed.setText("Simulation speed:");

    btnContinue.setText("Continue");
    btnContinue.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnContinueActionPerformed(evt);
      }
    });
  }

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinueActionPerformed
    super.closeDialogIfValid();
  }//GEN-LAST:event_btnContinueActionPerformed

  @Override
  protected void fillBySettings() {
    this.nudeSecondLength.setValue(settings.simulation.secondLengthInMs);
  }

  @Override
  protected boolean isValidated() {

    settings.simulation.secondLengthInMs = this.nudeSecondLength.getValue();

    return true;
  }

  private void initExtenders() {
    nudeSecondLength = new NumericUpDownExtender(nudSecondLengthInMs, 200, 3000, 700, 100);
  }
}
