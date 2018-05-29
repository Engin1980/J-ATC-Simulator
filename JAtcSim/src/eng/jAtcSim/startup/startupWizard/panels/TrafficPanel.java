/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.startupWizard.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.extenders.NumericUpDownExtender;
import eng.jAtcSim.startup.extenders.SwingFactory;
import eng.jAtcSim.startup.extenders.XComboBoxExtender;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;

public class TrafficPanel extends JStartupPanel {

  private javax.swing.JCheckBox chkAllowDelays;
  private javax.swing.JCheckBox chkCustomExtendedCallsigns;
  private javax.swing.ButtonGroup grpRdb;
  private NumericUpDownExtender nudMaxPlanes;
  private NumericUpDownExtender nudMovements;
  private javax.swing.JRadioButton rdbAirportDefined;
  private javax.swing.JRadioButton rdbCustom;
  private javax.swing.JRadioButton rdbXml;
  private NumericUpDownExtender nudA;
  private JScrollBar sldArrivalsDepartures;
  private NumericUpDownExtender nudB;
  private NumericUpDownExtender nudC;
  private NumericUpDownExtender nudD;
  private XmlFileSelectorExtender fleTraffic;
  private XComboBoxExtender<String> cmbAirportDefinedTraffic;
  private NumericUpDownExtender nudTrafficDensity;
  private JPanel pnlCustomTraffic;

  public TrafficPanel() {
    initComponents();
    Sources.getAreaChanged().add(() -> areaChanged());
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    fleTraffic.setFileName(settings.files.trafficXmlFile);
    nudMaxPlanes.setValue(settings.traffic.maxPlanes);
    nudTrafficDensity.setValue((int) (settings.traffic.densityPercentage * 100));
    adjustSelectedRdb(settings);

    chkAllowDelays.setSelected(settings.traffic.allowDelays);
    nudMovements.setValue(settings.traffic.customTraffic.movementsPerHour);
    sldArrivalsDepartures.setValue(settings.traffic.customTraffic.arrivals2departuresRatio);
    nudA.setValue(settings.traffic.customTraffic.weightTypeA);
    nudB.setValue(settings.traffic.customTraffic.weightTypeB);
    nudC.setValue(settings.traffic.customTraffic.weightTypeC);
    nudD.setValue(settings.traffic.customTraffic.weightTypeD);
    chkCustomExtendedCallsigns.setSelected(settings.traffic.customTraffic.useExtendedCallsigns);

    areaChanged();
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.files.trafficXmlFile = fleTraffic.getFileName();
    settings.traffic.maxPlanes = nudMaxPlanes.getValue();
    settings.traffic.trafficAirportDefinedTitle = cmbAirportDefinedTraffic.getSelectedItem();
    settings.traffic.densityPercentage = nudTrafficDensity.getValue() / 100d;
    settings.traffic.customTraffic.movementsPerHour = nudMovements.getValue();
    adjustRdbSelected(settings);

    settings.traffic.allowDelays = chkAllowDelays.isSelected();
    settings.traffic.customTraffic.arrivals2departuresRatio = sldArrivalsDepartures.getValue();
    settings.traffic.customTraffic.weightTypeA = nudA.getValue();
    settings.traffic.customTraffic.weightTypeB = nudB.getValue();
    settings.traffic.customTraffic.weightTypeC = nudC.getValue();
    settings.traffic.customTraffic.weightTypeD = nudD.getValue();
    settings.traffic.customTraffic.useExtendedCallsigns = chkCustomExtendedCallsigns.isSelected();
  }

  public void areaChanged() {
    IList<XComboBoxExtender.Item<String>> mp = new EList<>();
    Area area = Sources.getArea();
    if (area != null)
      for (Airport airport : area.getAirports()) {
        for (Traffic traffic : airport.getTrafficDefinitions()) {
          mp.add(
              new XComboBoxExtender.Item<>(
                  airport.getIcao() + " - " + traffic.getTitle(),
                  airport.getIcao() + ":" + traffic.getTitle()));
        }
      }
    else
      mp.add(new XComboBoxExtender.Item<>("Area not loaded", "----"));
    cmbAirportDefinedTraffic.setModel(mp);
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    createComponents();
    createLayout();
  }

  private void createLayout() {

    JPanel pnlGlobalTrafficSettings = LayoutManager.createFlowPanel(
        LayoutManager.eVerticalAlign.baseline,
        super.DISTANCE,
        new JLabel("Max planes count:"), nudMaxPlanes.getControl(),
        new JLabel("Traffic density (%):"), nudTrafficDensity.getControl(),
        chkAllowDelays);
    pnlGlobalTrafficSettings.setBorder(BorderFactory.createTitledBorder("Global traffic settings:"));

    JPanel pnlTrafficSource = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE);
    pnlTrafficSource.setBorder(BorderFactory.createTitledBorder("Used traffic:"));
    pnlTrafficSource.add(
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, DISTANCE,
            rdbXml, new JLabel(), fleTraffic.getTextControl(), fleTraffic.getButtonControl()));
    pnlTrafficSource.add(
        LayoutManager.createBorderedPanel(0,
            LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, DISTANCE,
                rdbAirportDefined,
                cmbAirportDefinedTraffic.getControl())));
    pnlTrafficSource.add(
        LayoutManager.createBorderedPanel(0, rdbCustom));


    this.pnlCustomTraffic = LayoutManager.createFormPanel(4, 2,
        null, chkCustomExtendedCallsigns,
        new JLabel("Movements / hour:"), nudMovements.getControl(),
        new JLabel("Arrivals <-> Departures"), sldArrivalsDepartures,
        new JLabel("Plane weights A/B/C/D"), LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE,
            nudA.getControl(), nudB.getControl(), nudC.getControl(), nudD.getControl())
    );
    this.pnlCustomTraffic.setBorder(BorderFactory.createTitledBorder("Custom generic traffic settings:"));

    JPanel pnlMain = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE,
        pnlGlobalTrafficSettings, pnlTrafficSource, this.pnlCustomTraffic);

    pnlMain = LayoutManager.createBorderedPanel(DISTANCE, pnlMain);

    this.add(pnlMain);
  }

  private void createComponents() {
    grpRdb = new javax.swing.ButtonGroup();

    rdbXml = new javax.swing.JRadioButton("Use XML defined traffic");

    rdbAirportDefined = new javax.swing.JRadioButton("Defined by active airport");

    rdbCustom = new javax.swing.JRadioButton("Use custom traffic");
    rdbCustom.addChangeListener(q -> updateCustomPanelState());

    fleTraffic = new XmlFileSelectorExtender();

    chkAllowDelays = new javax.swing.JCheckBox("Allow traffic delays");

    nudMovements = new NumericUpDownExtender(new JSpinner(), 0, 120, 40, 1);
    sldArrivalsDepartures = SwingFactory.createHorizontalBar(0, 10, 5);
    nudA = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    nudB = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    nudC = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    nudD = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    chkCustomExtendedCallsigns = new javax.swing.JCheckBox("Use extended callsigns");
    nudMaxPlanes = new NumericUpDownExtender(new JSpinner(), 1, 100, 15, 1);
    cmbAirportDefinedTraffic = new XComboBoxExtender<>();
    nudTrafficDensity = new NumericUpDownExtender(new JSpinner(), 0, 100, 100, 1);

    grpRdb.add(rdbXml);
    grpRdb.add(rdbCustom);
    grpRdb.add(rdbAirportDefined);
    rdbCustom.setSelected(true);
  }

  private void updateCustomPanelState() {
    if (pnlCustomTraffic != null && rdbCustom != null)
      ComponentUtils.adjustComponentTree(pnlCustomTraffic, q -> q.setEnabled(rdbCustom.isSelected()));
  }

//
//  protected boolean doWizardValidation() {
//
//    if (rdbXml.isSelected()) {
//      if (xmlFile.isValid() == false) {
//        MessageBox.show("Xml file name is not valid.", "Error...");
//        return false;
//      }
//    }
//
//    return true;
//  }

  private void adjustSelectedRdb(StartupSettings settings) {
    rdbAirportDefined.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.airportDefined);
    rdbXml.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.xml);
    rdbCustom.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.custom);
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
}
