/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup;

import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;

/**
 * @author Marek Vajgl
 */
public class FrmWizardTraffic extends FrmWizardFrame {

  public FrmWizardTraffic() {
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

    JPanel pnlS = LayoutManager.createFormPanel(6, 3,
        lblArrivals, sldArrivalsDepartures, lblDepartures,
        lblVFR, sldVfrIfr, lblIFR,
        null, sldA, lblAPlaneWeight,
        null, sldB, lblBPlaneWeight,
        null, sldC, lblCPlaneWeight,
        null, sldD, lblDPlaneWeight
    );
    JPanel pnlU = LayoutManager.createBoxPanel(
        LayoutManager.eHorizontalAlign.left,
        distance,
        LayoutManager.createFormPanel(2, 2, lblMovementsPerHour, nudMovements, lblMaxPlanesCount, nudMaxPlanes),
        chkCustomExtendedCallsigns
    );
    JPanel pnlC = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top, distance, pnlU, pnlS);
    JPanel pnlM = LayoutManager.createBoxPanel(
        LayoutManager.eHorizontalAlign.left,
        distance,
        rdbXml,
        LayoutManager.indentPanel(
            LayoutManager.createBoxPanel(
                LayoutManager.eHorizontalAlign.left,
                distance,
                LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top, distance, lblTraffixXmlFile, txtTrafficXmlFile, btnTrafficXmlFileBrowse),
                chkAllowDelays
            ), distance),
        rdbCustom,
        LayoutManager.indentPanel(pnlC, distance)
    );

    pnlM = super.wrapWithContinueButton(pnlM, btnContinue);

    pnlM = LayoutManager.createBorderedPanel(distance, pnlM);

    this.getContentPane().add(pnlM);
  }

  private void createComponents() {
    grpRdb = new javax.swing.ButtonGroup();
    rdbXml = new javax.swing.JRadioButton();
    pnlXml = new javax.swing.JPanel();
    lblTraffixXmlFile = new javax.swing.JLabel();
    txtTrafficXmlFile = new javax.swing.JTextField();
    btnTrafficXmlFileBrowse = new javax.swing.JButton();
    chkAllowDelays = new javax.swing.JCheckBox();
    rdbCustom = new javax.swing.JRadioButton();
    pnlCustom = new javax.swing.JPanel();
    lblMovementsPerHour = new javax.swing.JLabel();
    nudMovements = new javax.swing.JSpinner();
    sldArrivalsDepartures = new javax.swing.JSlider();
    lblArrivals = new javax.swing.JLabel();
    lblDepartures = new javax.swing.JLabel();
    sldVfrIfr = new javax.swing.JSlider();
    lblVFR = new javax.swing.JLabel();
    lblIFR = new javax.swing.JLabel();
    sldA = new javax.swing.JSlider();
    sldB = new javax.swing.JSlider();
    sldC = new javax.swing.JSlider();
    sldD = new javax.swing.JSlider();
    lblAPlaneWeight = new javax.swing.JLabel();
    lblBPlaneWeight = new javax.swing.JLabel();
    lblCPlaneWeight = new javax.swing.JLabel();
    lblDPlaneWeight = new javax.swing.JLabel();
    chkCustomExtendedCallsigns = new javax.swing.JCheckBox();
    nudMaxPlanes = new javax.swing.JSpinner();
    lblMaxPlanesCount = new javax.swing.JLabel();
    btnContinue = new javax.swing.JButton();
    btnContinue.setText("Continue");
    btnContinue.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnContinueActionPerformed(evt);
      }
    });

    rdbCustom.setText("Use custom traffic");
    rdbCustom.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rdbCustomActionPerformed(evt);
      }
    });

    lblMovementsPerHour.setText("Movements / hour:");

    nudMovements.setValue(10);
    nudMovements.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        nudMovementsStateChanged(evt);
      }
    });

    sldArrivalsDepartures.setMajorTickSpacing(1);
    sldArrivalsDepartures.setMaximum(10);
    sldArrivalsDepartures.setPaintTicks(true);
    sldArrivalsDepartures.setSnapToTicks(true);
    sldArrivalsDepartures.setValue(5);

    lblArrivals.setText("Arrivals");

    lblDepartures.setText("Departures");

    sldVfrIfr.setMajorTickSpacing(1);
    sldVfrIfr.setMaximum(10);
    sldVfrIfr.setPaintTicks(true);
    sldVfrIfr.setSnapToTicks(true);
    sldVfrIfr.setEnabled(false);

    lblVFR.setText("VFR");

    lblIFR.setText("IFR");

    sldA.setMajorTickSpacing(1);
    sldA.setMaximum(10);
    sldA.setPaintTicks(true);
    sldA.setSnapToTicks(true);
    sldA.setValue(5);

    sldB.setMajorTickSpacing(1);
    sldB.setMaximum(10);
    sldB.setPaintTicks(true);
    sldB.setSnapToTicks(true);
    sldB.setValue(5);

    sldC.setMajorTickSpacing(1);
    sldC.setMaximum(10);
    sldC.setPaintTicks(true);
    sldC.setSnapToTicks(true);
    sldC.setValue(5);

    sldD.setMajorTickSpacing(1);
    sldD.setMaximum(10);
    sldD.setPaintTicks(true);
    sldD.setSnapToTicks(true);
    sldD.setValue(5);

    lblAPlaneWeight.setText("A plane type occurence probability weight");

    lblBPlaneWeight.setText("B plane type occurence probability weight");

    lblCPlaneWeight.setText("C plane type occurence probability weight");

    lblDPlaneWeight.setText("D plane type occurence probability weight");

    chkCustomExtendedCallsigns.setText("Use extended callsigns");

    nudMaxPlanes.setValue(10);
    nudMaxPlanes.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        nudMaxPlanesStateChanged(evt);
      }
    });

    lblMaxPlanesCount.setText("Max planes count:");

    rdbXml.setText("Use XML defined traffic");
    rdbXml.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rdbXmlActionPerformed(evt);
      }
    });

    lblTraffixXmlFile.setText("Traffic XML file:");

    txtTrafficXmlFile.setText("---");

    btnTrafficXmlFileBrowse.setText("(browse)");

    chkAllowDelays.setText("Allow traffic delays");

    grpRdb.add(rdbXml);
    grpRdb.add(rdbCustom);
  }

  private void rdbXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbXmlActionPerformed
    updatePanelAccess();
  }//GEN-LAST:event_rdbXmlActionPerformed

  private void rdbCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbCustomActionPerformed
    updatePanelAccess();
  }//GEN-LAST:event_rdbCustomActionPerformed

  private void nudMovementsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nudMovementsStateChanged
    int val = (int) nudMovements.getValue();
    if (val < 1) {
      nudMovements.setValue(1);
    }
  }//GEN-LAST:event_nudMovementsStateChanged

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinueActionPerformed
    super.closeDialogIfValid();
  }//GEN-LAST:event_btnContinueActionPerformed

  private void nudMaxPlanesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nudMaxPlanesStateChanged
    // TODO add your handling code here:
  }//GEN-LAST:event_nudMaxPlanesStateChanged

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnContinue;
  private javax.swing.JButton btnTrafficXmlFileBrowse;
  private javax.swing.JCheckBox chkAllowDelays;
  private javax.swing.JCheckBox chkCustomExtendedCallsigns;
  private javax.swing.ButtonGroup grpRdb;
  private javax.swing.JLabel lblTraffixXmlFile;
  private javax.swing.JLabel lblDPlaneWeight;
  private javax.swing.JLabel lblMaxPlanesCount;
  private javax.swing.JLabel lblMovementsPerHour;
  private javax.swing.JLabel lblVFR;
  private javax.swing.JLabel lblArrivals;
  private javax.swing.JLabel lblDepartures;
  private javax.swing.JLabel lblIFR;
  private javax.swing.JLabel lblAPlaneWeight;
  private javax.swing.JLabel lblBPlaneWeight;
  private javax.swing.JLabel lblCPlaneWeight;
  private javax.swing.JSpinner nudMaxPlanes;
  private javax.swing.JSpinner nudMovements;
  private javax.swing.JPanel pnlCustom;
  private javax.swing.JPanel pnlXml;
  private javax.swing.JRadioButton rdbCustom;
  private javax.swing.JRadioButton rdbXml;
  private javax.swing.JSlider sldA;
  private javax.swing.JSlider sldArrivalsDepartures;
  private javax.swing.JSlider sldB;
  private javax.swing.JSlider sldC;
  private javax.swing.JSlider sldD;
  private javax.swing.JSlider sldVfrIfr;
  private javax.swing.JTextField txtTrafficXmlFile;
  // End of variables declaration//GEN-END:variables
  XmlFileSelectorExtender xmlFile;

  @Override
  protected void fillBySettings() {

    rdbCustom.setSelected(settings.traffic.useXml == false);
    rdbXml.setSelected(settings.traffic.useXml);
    chkAllowDelays.setSelected(settings.traffic.delayAllowed);
    txtTrafficXmlFile.setText(settings.files.trafficXmlFile);
    nudMovements.setValue(settings.traffic.movementsPerHour);
    sldArrivalsDepartures.setValue(settings.traffic.arrivals2departuresRatio);
    sldVfrIfr.setValue(settings.traffic.vfr2ifrRatio);
    sldA.setValue(settings.traffic.weightTypeA);
    sldB.setValue(settings.traffic.weightTypeB);
    sldC.setValue(settings.traffic.weightTypeC);
    sldD.setValue(settings.traffic.weightTypeD);
    chkCustomExtendedCallsigns.setSelected(settings.traffic.useExtendedCallsigns);
    nudMaxPlanes.setValue(settings.traffic.maxPlanes);

    updatePanelAccess();
  }

  @Override
  protected boolean isValidated() {

    if (rdbXml.isSelected()) {
      if (xmlFile.isValid() == false) {
        MessageBox.show("Xml file name is not valid.", "Error...");
        return false;
      }
    }

    settings.traffic.useXml=rdbXml.isSelected();
    settings.traffic.delayAllowed=chkAllowDelays.isSelected();
    settings.files.trafficXmlFile=txtTrafficXmlFile.getText();
    settings.traffic.movementsPerHour=(int) nudMovements.getValue();
    settings.traffic.arrivals2departuresRatio=sldArrivalsDepartures.getValue();
    settings.traffic.vfr2ifrRatio=sldVfrIfr.getValue();
    settings.traffic.weightTypeA=sldA.getValue();
    settings.traffic.weightTypeB=sldB.getValue();
    settings.traffic.weightTypeC=sldC.getValue();
    settings.traffic.weightTypeD=sldD.getValue();
    settings.traffic.useExtendedCallsigns=chkCustomExtendedCallsigns.isSelected();
    settings.traffic.maxPlanes=(int) nudMaxPlanes.getValue();

    return true;
  }

  private void initExtenders() {
    xmlFile = new XmlFileSelectorExtender(txtTrafficXmlFile, btnTrafficXmlFileBrowse);
  }

  private void updatePanelAccess() {
    pnlCustom.setEnabled(rdbCustom.isSelected());
    pnlXml.setEnabled(rdbXml.isSelected());
  }
}
