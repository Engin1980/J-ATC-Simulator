/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.app.extenders.*;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.app.startupSettings.StartupSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class TrafficPanel extends JStartupPanel {

  private javax.swing.JCheckBox chkAllowDelays;
  private XComboBoxExtender<Double> cmbEmergencyProbability;
  private javax.swing.JCheckBox chkCustomExtendedCallsigns;
  private javax.swing.ButtonGroup grpRdb;
  private NumericUpDownExtender nudMaxPlanes;
  private NumericUpDownExtender nudMovements;
  private NumericUpDownExtender nudNonCommercials;
  private javax.swing.JRadioButton rdbUser;
  private javax.swing.JRadioButton rdbXml;
  private NumericUpDownExtender nudA;
  private JScrollBar sldArrivalsDepartures;
  private NumericUpDownExtender nudB;
  private NumericUpDownExtender nudC;
  private NumericUpDownExtender nudD;
  private XComboBoxExtender<String> cmbAirportDefinedTraffic;
  private XComboBoxExtender<String> cmbXmlDefinedTraffic;
  private NumericUpDownExtender nudTrafficDensity;
  private JPanel pnlCustomTraffic;
  private ItemTextFieldExtender txtCompanies;
  private ItemTextFieldExtender txtCountryCodes;
  private int[] specificMovementValues = null;

  public TrafficPanel() {
    initComponents();
    Sources.getOnFleetsChanged().add(this::fleetsChanged);
    Sources.getOnTrafficChanged().add(this::trafficChanged);
  }

  public void setActiveAirport(Airport aip){
    airportChanged(aip);
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    nudMaxPlanes.setValue(settings.traffic.maxPlanes);
    nudTrafficDensity.setValue((int) (settings.traffic.densityPercentage * 100));
    adjustSelectedRdb(settings);

    chkAllowDelays.setSelected(settings.traffic.allowDelays);
    if (settings.traffic.customTraffic.movementsPerHour == null)
      nudMovements.setValue(10);
    else {
      nudMovements.setValue(settings.traffic.customTraffic.movementsPerHour[0]);
      this.specificMovementValues = settings.traffic.customTraffic.movementsPerHour;
    }
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

    //TODO
//    areaChanged();
    trafficChanged();

    cmbAirportDefinedTraffic.setSelectedItem(settings.traffic.trafficAirportDefinedTitle);
    cmbXmlDefinedTraffic.setSelectedItem(settings.traffic.trafficXmlDefinedTitle);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.traffic.maxPlanes = nudMaxPlanes.getValue();
    settings.traffic.trafficAirportDefinedTitle = cmbAirportDefinedTraffic.getSelectedItem();
    settings.traffic.trafficXmlDefinedTitle = cmbXmlDefinedTraffic.getSelectedItem();
    settings.traffic.densityPercentage = nudTrafficDensity.getValue() / 100d;
    if (specificMovementValues == null){
      int tmp []  = new int[24];
      Arrays.fill(tmp, nudMovements.getValue());
      settings.traffic.customTraffic.movementsPerHour = tmp;
    } else
      settings.traffic.customTraffic.movementsPerHour = specificMovementValues;
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

  public void airportChanged(Airport aip) {
    IList<XComboBoxExtender.Item<String>> mp = new EList<>();
    if (aip != null)
        for (Traffic traffic : aip.getTrafficDefinitions()) {
          mp.add(
              new XComboBoxExtender.Item<>(
                  traffic.getTitle(),
                  traffic.getTitle()));
        }
    else
      mp.add(new XComboBoxExtender.Item<>("Airport not selected", ""));
    cmbAirportDefinedTraffic.setModel(mp);
  }

  public void trafficChanged() {
    IList<XComboBoxExtender.Item<String>> mp = new EList<>();
    IReadOnlyList<Traffic> trafficList = Sources.getTraffics();
    if (trafficList != null)
        for (Traffic traffic : trafficList) {
          mp.add(
              new XComboBoxExtender.Item<>(
                  traffic.getTitle(),
                  traffic.getTitle()));
        }
    else
      mp.add(new XComboBoxExtender.Item<>("Traffic set not loaded", ""));
    cmbXmlDefinedTraffic.setModel(mp);
  }

  private void setCmbEmergencyProbabilityByClosestValue(double emergencyPerDayProbability) {
    double minDiff = Double.MAX_VALUE;
    int bestIndex = 0;
    for (int i = 0; i < cmbEmergencyProbability.getCount(); i++) {
      double item = cmbEmergencyProbability.getItem(i);
      if (Math.abs(item - emergencyPerDayProbability) < minDiff) {
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

    JPanel pnlGlobalTrafficSettings = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4,
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
    pnlGlobalTrafficSettings = LayoutManager.createBorderedPanel(8, pnlGlobalTrafficSettings);
    LayoutManager.setPanelBorderText(pnlGlobalTrafficSettings, "Global traffic settings:");

    JPanel pnlTrafficSource = LayoutManager.createFormPanel(2, 2,
        rdbXml,
        LayoutManager.createFlowPanel(fleTraffic.getTextControl(), fleTraffic.getButtonControl()),
        rdbUser, null);
    pnlTrafficSource.setBorder(BorderFactory.createTitledBorder("Used traffic:"));

    this.pnlCustomTraffic = LayoutManager.createFormPanel(7, 2,
        null, chkCustomExtendedCallsigns,
        new JLabel("Movements / hour:"),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE,
            nudMovements.getControl(),
            SwingFactory.createButton("Specify precisely", this::btnSpecifyTraffic_click)),
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

  private void btnSpecifyTraffic_click(ActionEvent actionEvent) {
    GenericTrafficMovementsPerHourPanel pnl = new GenericTrafficMovementsPerHourPanel();

    if (specificMovementValues != null) pnl.setValues(this.specificMovementValues);
    else pnl.setValues(nudMovements.getValue());
    SwingFactory.showDialog(pnl, "Specify traffic per hour...", (JDialog) this.getRootPane().getParent());
    int[] tmp = pnl.getValues();
    if (tmp != null) {
      nudMovements.setValue(tmp[0]);
      specificMovementValues = tmp;
    }
  }
  private XmlFileSelectorExtender fleTraffic;
  private void createComponents() {

    fleTraffic = new XmlFileSelectorExtender(SwingFactory.FileDialogType.traffic);

    grpRdb = new javax.swing.ButtonGroup();

    rdbXml = new javax.swing.JRadioButton("Load from file");

    rdbUser = new javax.swing.JRadioButton("Use custom traffic");
    rdbUser.addChangeListener(q -> updateCustomPanelState());

    chkAllowDelays = new javax.swing.JCheckBox("Allow traffic delays");

    nudMovements = new NumericUpDownExtender(new JSpinner(), 0, 120, 40, 1);
    nudMovements.getOnChanged().add(q -> this.specificMovementValues = null);
    sldArrivalsDepartures = SwingFactory.createHorizontalBar(0, 10, 5);
    nudA = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    nudB = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    nudC = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    nudD = new NumericUpDownExtender(new JSpinner(), 0, 100, 5, 1);
    chkCustomExtendedCallsigns = new javax.swing.JCheckBox("Use extended callsigns");
    nudMaxPlanes = new NumericUpDownExtender(new JSpinner(), 1, 100, 15, 1);
    cmbAirportDefinedTraffic = new XComboBoxExtender<>();
    cmbXmlDefinedTraffic = new XComboBoxExtender<>();
    nudTrafficDensity = new NumericUpDownExtender(new JSpinner(), 0, 100, 100, 1);
    nudNonCommercials = new NumericUpDownExtender(new JSpinner(), 0, 100, 0, 10);
    txtCompanies = new ItemTextFieldExtender();
    txtCountryCodes = new ItemTextFieldExtender();

    cmbEmergencyProbability = new XComboBoxExtender<>();
    setCmbEmergencyProbabilityModel();

    grpRdb.add(rdbXml);
    grpRdb.add(rdbUser);
    rdbUser.setSelected(true);
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
    if (pnlCustomTraffic != null && rdbUser != null)
      ComponentUtils.adjustComponentTree(pnlCustomTraffic, q -> q.setEnabled(rdbUser.isSelected()));
  }

  private void adjustSelectedRdb(StartupSettings settings) {
    rdbXml.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.xml);
    rdbUser.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.user);
  }

  private void adjustRdbSelected(StartupSettings settings) {
    if (rdbXml.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.xml;
    else if (rdbUser.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.user;
    else
      throw new UnsupportedOperationException();
  }
}
