/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.startupWizard;

import eng.eSystem.utilites.CollectionUtils;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.MessageBox;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;

/**
 * @author Marek Vajgl
 */
public class PnlWizardTraffic extends JWizardPanel {

  XmlFileSelectorExtender xmlFile;
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
  private javax.swing.JRadioButton rdbAirportDefined;
  private javax.swing.JRadioButton rdbCustom;
  private javax.swing.JRadioButton rdbXml;
  private javax.swing.JSlider sldA;
  private javax.swing.JSlider sldArrivalsDepartures;
  private javax.swing.JSlider sldB;
  private javax.swing.JSlider sldC;
  private javax.swing.JSlider sldD;
  private javax.swing.JTextField txtTrafficXmlFile;
  private javax.swing.JComboBox<String> cmbAirportDefined;
  private javax.swing.JSlider sldTrafficDensity;
  private javax.swing.JLabel lblTrafficDensity;
  public PnlWizardTraffic() {
    initComponents();
    initExtenders();


  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();
  }

  private void createLayout() {

    JPanel pnlGlobalTrafficSettings = LayoutManager.createFormPanel(2, 2,
        lblMaxPlanesCount, nudMaxPlanes,
        lblTrafficDensity, sldTrafficDensity);
    pnlGlobalTrafficSettings.setBorder(BorderFactory.createTitledBorder("Global traffic settings:"));


    JPanel pnlTrafficSource = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE);
    pnlTrafficSource.setBorder(BorderFactory.createTitledBorder("Used traffic:"));
    pnlTrafficSource.add(
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, DISTANCE,
            rdbXml, lblTraffixXmlFile, txtTrafficXmlFile, btnTrafficXmlFileBrowse));
    pnlTrafficSource.add(
        LayoutManager.createBorderedPanel(0,
            LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, DISTANCE,
                rdbAirportDefined,
                cmbAirportDefined)));
    pnlTrafficSource.add(
        LayoutManager.createBorderedPanel(0, rdbCustom));

    JPanel pnlCustomGenericTrafficSettings = LayoutManager.createFormPanel(7, 3,
        null, chkCustomExtendedCallsigns, null,
        lblMovementsPerHour, nudMovements, null,
        lblArrivals, sldArrivalsDepartures, lblDepartures,
        null, sldA, lblAPlaneWeight,
        null, sldB, lblBPlaneWeight,
        null, sldC, lblCPlaneWeight,
        null, sldD, lblDPlaneWeight
    );
    pnlCustomGenericTrafficSettings.setBorder(BorderFactory.createTitledBorder("Custom generic traffic settings (if used):"));


    JPanel pnlMain = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE,
        pnlGlobalTrafficSettings, pnlTrafficSource, pnlCustomGenericTrafficSettings);


    pnlMain = LayoutManager.createBorderedPanel(DISTANCE, pnlMain);

    this.add(pnlMain);
  }

  private void createComponents() {
    grpRdb = new javax.swing.ButtonGroup();
    rdbXml = new javax.swing.JRadioButton();
    pnlXml = new javax.swing.JPanel();
    rdbAirportDefined = new javax.swing.JRadioButton();
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
    cmbAirportDefined = new JComboBox<>();
    lblTrafficDensity = new JLabel();
    sldTrafficDensity = new JSlider();

    lblTrafficDensity.setText("Traffic density %:");
    sldTrafficDensity.setMinimum(0);
    sldTrafficDensity.setMajorTickSpacing(10);
    sldTrafficDensity.setMaximum(100);
    sldTrafficDensity.setValue(100);
    sldTrafficDensity.setPaintTicks(true);
    sldTrafficDensity.setSnapToTicks(false);
    sldTrafficDensity.setEnabled(false);

    rdbCustom.setText("Use custom traffic");

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

    lblTraffixXmlFile.setText("Traffic XML file:");

    txtTrafficXmlFile.setText("---");

    btnTrafficXmlFileBrowse.setText("(browse)");

    chkAllowDelays.setText("Allow traffic delays");

    grpRdb.add(rdbXml);
    grpRdb.add(rdbCustom);
    grpRdb.add(rdbAirportDefined);

    rdbAirportDefined.setText("Defined by active airport");
  }

  private void nudMovementsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nudMovementsStateChanged
    int val = (int) nudMovements.getValue();
    if (val < 1) {
      nudMovements.setValue(1);
    }
  }

  private void nudMaxPlanesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nudMaxPlanesStateChanged
    // TODO add your handling code here:
  }

  private void fillAirportDefinedTraffic(StartupSettings settings) {
    Area area = XmlLoadHelper.loadNewArea(settings.files.areaXmlFile);

    int selectedIndex = -1;
    String[] data = new String[0];
    ComboBoxModel<String> model;
    Airport airport = CollectionUtils.tryGetFirst(area.getAirports(), o -> o.getIcao().equals(settings.recent.icao));
    if (airport != null) {
      data = new String[airport.getTrafficDefinitions().size()];
      for (int i = 0; i < data.length; i++) {
        data[i] = airport.getTrafficDefinitions().get(i).getTitle();
        if (data[i].equals(settings.traffic.trafficAirportDefinedTitle))
          selectedIndex = i;
      }
    }
    model = new DefaultComboBoxModel<>(data);
    cmbAirportDefined.setModel(model);
    if (selectedIndex >= 0)
      cmbAirportDefined.setSelectedIndex(selectedIndex);
  }

  @Override
  protected boolean doWizardValidation() {

    if (rdbXml.isSelected()) {
      if (xmlFile.isValid() == false) {
        MessageBox.show("Xml file name is not valid.", "Error...");
        return false;
      }
    }

    return true;
  }

  @Override
  protected void fillBySettings(StartupSettings settings) {

    txtTrafficXmlFile.setText(settings.files.trafficXmlFile);
    nudMaxPlanes.setValue(settings.traffic.maxPlanes);
    sldTrafficDensity.setValue((int) (settings.traffic.densityPercentage * 100));
    adjustSelectedRdb(settings);
    fillAirportDefinedTraffic(settings);

    chkAllowDelays.setSelected(settings.traffic.customTraffic.delayAllowed);
    nudMovements.setValue(settings.traffic.customTraffic.movementsPerHour);
    sldArrivalsDepartures.setValue(settings.traffic.customTraffic.arrivals2departuresRatio);
    sldA.setValue(settings.traffic.customTraffic.weightTypeA);
    sldB.setValue(settings.traffic.customTraffic.weightTypeB);
    sldC.setValue(settings.traffic.customTraffic.weightTypeC);
    sldD.setValue(settings.traffic.customTraffic.weightTypeD);
    chkCustomExtendedCallsigns.setSelected(settings.traffic.customTraffic.useExtendedCallsigns);
  }

  private void adjustSelectedRdb(StartupSettings settings) {
    rdbAirportDefined.setSelected(settings.traffic.type ==  StartupSettings.Traffic.eTrafficType.airportDefined);
    rdbXml.setSelected(settings.traffic.type ==  StartupSettings.Traffic.eTrafficType.xml);
    rdbCustom.setSelected(settings.traffic.type ==  StartupSettings.Traffic.eTrafficType.custom);
  }

  @Override
  void fillSettingsBy(StartupSettings settings) {
    settings.files.trafficXmlFile = txtTrafficXmlFile.getText();
    settings.traffic.maxPlanes = (int) nudMaxPlanes.getValue();
    settings.traffic.trafficAirportDefinedTitle = (String) cmbAirportDefined.getSelectedItem();
    settings.traffic.densityPercentage = sldTrafficDensity.getValue() / 100d;
    adjustRdbSelected(settings);

    settings.traffic.customTraffic.delayAllowed = chkAllowDelays.isSelected();
    settings.traffic.customTraffic.movementsPerHour = (int) nudMovements.getValue();
    settings.traffic.customTraffic.arrivals2departuresRatio = sldArrivalsDepartures.getValue();
    settings.traffic.customTraffic.weightTypeA = sldA.getValue();
    settings.traffic.customTraffic.weightTypeB = sldB.getValue();
    settings.traffic.customTraffic.weightTypeC = sldC.getValue();
    settings.traffic.customTraffic.weightTypeD = sldD.getValue();
    settings.traffic.customTraffic.useExtendedCallsigns = chkCustomExtendedCallsigns.isSelected();
  }

  private void adjustRdbSelected(StartupSettings settings) {
    if (rdbXml.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.xml;
    else if (rdbAirportDefined.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.airportDefined;
    else if (rdbCustom.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.custom;
    else
      throw new UnsupportedOperationException();
  }

  private void initExtenders() {
    xmlFile = new XmlFileSelectorExtender(txtTrafficXmlFile, btnTrafficXmlFileBrowse);
  }
}
