/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.ExceptionUtil;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.startup.extenders.*;
import eng.jAtcSim.startup.startupSettings.StartupSettings;

import javax.swing.*;

public class TrafficPanel extends JStartupPanel {

  private javax.swing.JCheckBox chkAllowDelays;
  private XComboBoxExtender<Double> cmbEmergencyProbability;
  private javax.swing.JCheckBox chkCustomExtendedCallsigns;
  private javax.swing.ButtonGroup grpRdb;
  private NumericUpDownExtender nudMaxPlanes;
  private NumericUpDownExtender nudMovements;
  private NumericUpDownExtender nudNonCommercials;
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
  private ItemTextFieldExtender txtCompanies;
  private ItemTextFieldExtender txtCountryCodes;

  public TrafficPanel() {
    initComponents();
    Sources.getAreaChanged().add(() -> areaChanged());
    Sources.getFleetsChanged().add(() -> fleetsChanged());
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
    nudNonCommercials.setValue((int) (settings.traffic.customTraffic.nonCommercialFlightProbability * 100));
    nudA.setValue(settings.traffic.customTraffic.weightTypeA);
    nudB.setValue(settings.traffic.customTraffic.weightTypeB);
    nudC.setValue(settings.traffic.customTraffic.weightTypeC);
    nudD.setValue(settings.traffic.customTraffic.weightTypeD);
    chkCustomExtendedCallsigns.setSelected(settings.traffic.customTraffic.useExtendedCallsigns);
    txtCompanies.setItems(settings.traffic.customTraffic.getCompanies());
    txtCountryCodes.setItems(settings.traffic.customTraffic.getCountryCodes());

    setCmbEmergencyProbabilityByClosestValue(settings.traffic.emergencyPerDayProbability);

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
    settings.traffic.customTraffic.nonCommercialFlightProbability = nudNonCommercials.getValue() / 100d;
    settings.traffic.customTraffic.arrivals2departuresRatio = sldArrivalsDepartures.getValue();
    settings.traffic.customTraffic.weightTypeA = nudA.getValue();
    settings.traffic.customTraffic.weightTypeB = nudB.getValue();
    settings.traffic.customTraffic.weightTypeC = nudC.getValue();
    settings.traffic.customTraffic.weightTypeD = nudD.getValue();
    settings.traffic.customTraffic.useExtendedCallsigns = chkCustomExtendedCallsigns.isSelected();
    settings.traffic.customTraffic.setCompanies(txtCompanies.getItems());
    settings.traffic.customTraffic.setCountryCodes(txtCountryCodes.getItems());

    settings.traffic.emergencyPerDayProbability = cmbEmergencyProbability.getSelectedItem();
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

  private void setCmbEmergencyProbabilityByClosestValue(double emergencyPerDayProbability) {
    double minDiff = Double.MAX_VALUE;
    int bestIndex = 0;
    for (int i = 0; i < cmbEmergencyProbability.getCount(); i++) {
      double item = cmbEmergencyProbability.getItem(i);
      if (Math.abs(item - emergencyPerDayProbability) < minDiff){
        minDiff = Math.abs(item - emergencyPerDayProbability);
        bestIndex = i;
      }
    }
    cmbEmergencyProbability.setSelectedIndex(bestIndex);
  }

  private void fleetsChanged() {
    txtCompanies.setModel(Sources.getFleets().getIcaos());
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    createComponents();
    createLayout();
  }

  private void createLayout() {

    JPanel pnlGlobalTrafficSettings = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left , 4,
        LayoutManager.createFlowPanel(
          LayoutManager.eVerticalAlign.baseline,
          super.DISTANCE,
          new JLabel("Max planes count:"), nudMaxPlanes.getControl(),
          new JLabel("Traffic density (%):"), nudTrafficDensity.getControl(),
          chkAllowDelays),
        LayoutManager.createFlowPanel(
            LayoutManager.eVerticalAlign.baseline,
            super.DISTANCE,
            new JLabel("Emergencies probabilty:"),
            cmbEmergencyProbability.getControl()));

//    JPanel pnlGlobalTrafficSettings = LayoutManager.createFlowPanel(
//        LayoutManager.eVerticalAlign.baseline,
//        super.DISTANCE,
//        new JLabel("Max planes count:"), nudMaxPlanes.getControl(),
//        new JLabel("Traffic density (%):"), nudTrafficDensity.getControl(),
//        chkAllowDelays);
    pnlGlobalTrafficSettings.setBorder(BorderFactory.createTitledBorder("Global traffic settings:"));

    JButton btnCheckTraffic = new JButton("Check");
    btnCheckTraffic.addActionListener(q -> btnCheckTraffic_click());

    JPanel pnlTrafficSource = LayoutManager.createFormPanel(3, 2,
        rdbXml,
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE,
            fleTraffic.getTextControl(), fleTraffic.getButtonControl(), btnCheckTraffic),
        rdbAirportDefined,
        cmbAirportDefinedTraffic.getControl(),
        rdbCustom, null);
    pnlTrafficSource.setBorder(BorderFactory.createTitledBorder("Used traffic:"));

//    JPanel pnlTrafficSource = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE);
//    pnlTrafficSource.setBorder(BorderFactory.createTitledBorder("Used traffic:"));
//    pnlTrafficSource.add(
//        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, DISTANCE,
//            rdbXml, new JLabel(), fleTraffic.getTextControl(), fleTraffic.getButtonControl(), btnCheckTraffic));
//    pnlTrafficSource.add(
//        LayoutManager.createBorderedPanel(0,
//            LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, DISTANCE,
//                rdbAirportDefined,
//                cmbAirportDefinedTraffic.getControl())));
//    pnlTrafficSource.add(
//        LayoutManager.createBorderedPanel(0, rdbCustom));


    this.pnlCustomTraffic = LayoutManager.createFormPanel(7, 2,
        null, chkCustomExtendedCallsigns,
        new JLabel("Movements / hour:"), nudMovements.getControl(),
        new JLabel("Non-commercial flights (%):"), nudNonCommercials.getControl(),
        new JLabel("Arrivals <-> Departures"), sldArrivalsDepartures,
        new JLabel("Companies (ICAO;ICAO;...):"), txtCompanies.getControl(),
        new JLabel("Country codes (as above):"), txtCountryCodes.getControl(),
        new JLabel("Plane weights A/B/C/D"), LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE,
            nudA.getControl(), nudB.getControl(), nudC.getControl(), nudD.getControl())
    );
    this.pnlCustomTraffic.setBorder(BorderFactory.createTitledBorder("Custom generic traffic settings:"));

    JPanel pnlMain = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE,
        pnlGlobalTrafficSettings, pnlTrafficSource, this.pnlCustomTraffic);

    pnlMain = LayoutManager.createBorderedPanel(DISTANCE, pnlMain);

    this.add(pnlMain);
  }

  private void btnCheckTraffic_click() {
    try {
      String file = fleTraffic.getFileName();
      XmlLoadHelper.loadTraffic(file);
      MessageBox.show("Traffic file seems ok.", "Traffic file check");
    } catch (Exception ex) {
      MessageBox.show("Error occurred when loading traffic file. \n\n" + ExceptionUtil.toFullString(ex, "\n"), "Traffic file check");
    }
  }

  private void createComponents() {
    grpRdb = new javax.swing.ButtonGroup();

    rdbXml = new javax.swing.JRadioButton("Use XML defined traffic");

    rdbAirportDefined = new javax.swing.JRadioButton("Defined by active airport");

    rdbCustom = new javax.swing.JRadioButton("Use custom traffic");
    rdbCustom.addChangeListener(q -> updateCustomPanelState());

    fleTraffic = new XmlFileSelectorExtender(SwingFactory.FileDialogType.traffic);

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
    nudNonCommercials = new NumericUpDownExtender(new JSpinner(), 0, 100, 0, 10);
    txtCompanies = new ItemTextFieldExtender();
    txtCountryCodes = new ItemTextFieldExtender();

    cmbEmergencyProbability = new XComboBoxExtender<>();
    setCmbEmergencyProbabilityModel();

    grpRdb.add(rdbXml);
    grpRdb.add(rdbCustom);
    grpRdb.add(rdbAirportDefined);
    rdbCustom.setSelected(true);
  }

  private void setCmbEmergencyProbabilityModel() {
IList<XComboBoxExtender.Item<Double>> lst = new EList<>();

    lst.add(new XComboBoxExtender.Item<>("Off", -1d));
    lst.add(new XComboBoxExtender.Item<>("Once per hour", 24d));
    lst.add(new XComboBoxExtender.Item<>("Once per three hours", 8d));
    lst.add(new XComboBoxExtender.Item<>("Once per six hours", 4d));
    lst.add(new XComboBoxExtender.Item<>("Once per twelve hours", 2d));
    lst.add(new XComboBoxExtender.Item<>("Once per day", 1d));
    lst.add(new XComboBoxExtender.Item<>("Once per three days", 1 / 3d));
    lst.add(new XComboBoxExtender.Item<>("Once per week", 1 / 7d));
    lst.add(new XComboBoxExtender.Item<>("Once per two weeks", 1 / 14d));
    lst.add(new XComboBoxExtender.Item<>("Once per month", 1 / 30d));

    cmbEmergencyProbability.setModel(lst);
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
