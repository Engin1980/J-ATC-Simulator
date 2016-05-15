/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek Vajgl
 */
public class FrmWizardSimulationAndRadar extends FrmWizardFrame {

  private static List<String> packList = new ArrayList();
  
  static{
    packList.add("jatcsim.frmPacks.simple.Pack");
  }
  
  /**
   * Creates new form FrmWizardRadar
   */
  public FrmWizardSimulationAndRadar() {
    initComponents();
    initExtenders();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new javax.swing.JLabel();
    nudSecondLengthInMs = new javax.swing.JSpinner();
    btnContinue = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    jLabel1.setText("Simulation speed:");

    btnContinue.setText("Continue");
    btnContinue.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnContinueActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel1)
        .addGap(14, 14, 14)
        .addComponent(nudSecondLengthInMs, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(31, Short.MAX_VALUE))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(btnContinue)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(nudSecondLengthInMs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
        .addComponent(btnContinue)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinueActionPerformed
    super.closeDialogIfValid();
  }//GEN-LAST:event_btnContinueActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnContinue;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JSpinner nudSecondLengthInMs;
  // End of variables declaration//GEN-END:variables
private NumericUpDownExtender nudeSecondLength;
  
  @Override
  protected void fillBySettings() {
    this.nudeSecondLength.setValue(settings.getSimulationSecondLengthInMs());
  }

  @Override
  protected boolean isValidated() {
    
    settings.setSimulationSecondLengthInMs(this.nudeSecondLength.getValue());
    
    return true;
  }

  private void initExtenders() {
    nudeSecondLength = new NumericUpDownExtender(nudSecondLengthInMs, 200, 3000, 700, 100);
  }
}

