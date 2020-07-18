/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.swing.extenders.DisplayItem;
import eng.eSystem.swing.extenders.RegexPatternTextFieldExtender;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.app.extenders.NumericUpDownExtender;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.frmPacks.shared.FrmTrafficBarGraph;
import eng.jAtcSim.newLib.gameSim.game.sources.FleetsSource;
import eng.jAtcSim.newLib.gameSim.game.sources.TrafficSource;
import eng.jAtcSim.newLib.gameSim.game.sources.TrafficXmlSource;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.traffic.models.SimpleGenericTrafficModel;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafficPanel extends JStartupPanel {

  private static final Pattern ALL_MOVEMENT_REGEX = Pattern.compile("^(\\d+)(?::(?:0?\\.(\\d+))?(?:\\/0?\\.(\\d+))?)?((;(\\d+)(?::(?:0?\\.(\\d+))?(?:\\/0?\\.(\\d+))?)?){23})?$");
  private final static String ONE_COMPANY_REGEX = "([a-zA-Z]{3})(?:;(\\d+))?";
  private final static String ONE_COUNTRY_CODE_REGEX = "([a-zA-Z]{2})(?:;(\\d+))?";
  private static final Pattern ONE_MOVEMENT_REGEX = Pattern.compile("^(\\d+)(?::(?:0?\\.(\\d+))?(?:\\/0?\\.(\\d+))?)?$");

  private static ITrafficModel generateCustomTrafficModel(StartupSettings.Traffic trf) {

    SimpleGenericTrafficModel ret = SimpleGenericTrafficModel.create(
        trf.customTraffic.getGeneralAviationProbability(),
        trf.customTraffic.getDepartureProbability(),
        trf.customTraffic.getMovementsPerHour(),
        trf.customTraffic.getCompanies(),
        trf.customTraffic.getCountryCodes());

    return ret;

//    return ret;
  }

  private JButton btnAnalyseTraffic;
  private javax.swing.JCheckBox chkAllowDelays;
  private ComboBoxExtender<DisplayItem<Double>> cmbEmergencyProbability;
  private XmlFileSelectorExtender fleTraffic;
  private javax.swing.ButtonGroup grpRdb;
  private NumericUpDownExtender nudMaxPlanes;
  private NumericUpDownExtender nudNonCommercials;
  private NumericUpDownExtender nudTrafficDensity;
  private JPanel pnlCustomTraffic;
  private javax.swing.JRadioButton rdbUser;
  private javax.swing.JRadioButton rdbXml;
  private JScrollBar sldArrivalsDepartures;
  private int[] specificMovementValues = null;
  private RegexPatternTextFieldExtender txtCompanies;
  private RegexPatternTextFieldExtender txtCountryCodes;
  private RegexPatternTextFieldExtender txtMovements;

  public TrafficPanel() {
    initComponents();
  }

  @Override
  public void fillBySettings(StartupSettings settings) {

    fleTraffic.setFileName(settings.files.trafficXmlFile);

    nudMaxPlanes.setValue(settings.traffic.maxPlanes);
    nudTrafficDensity.setValue((int) (settings.traffic.densityPercentage * 100));
    adjustSelectedRdb(settings);

    chkAllowDelays.setSelected(settings.traffic.allowDelays);
    txtMovements.setText(encodeMovements(settings.traffic.customTraffic.getMovementsPerHour()));
    sldArrivalsDepartures.setValue((int) (settings.traffic.customTraffic.getDepartureProbability() * 10));
    nudNonCommercials.setValue((int) (settings.traffic.customTraffic.getGeneralAviationProbability() * 100));
    txtCompanies.setText(encodeMap(settings.traffic.customTraffic.getCompanies()));
    txtCountryCodes.setText(encodeMap(settings.traffic.customTraffic.getCountryCodes()));

    setCmbEmergencyProbabilityByClosestValue(settings.traffic.emergencyPerDayProbability);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.files.trafficXmlFile = fleTraffic.getFileName();

    settings.traffic.maxPlanes = nudMaxPlanes.getValue();
    settings.traffic.densityPercentage = nudTrafficDensity.getValue() / 100d;
    settings.traffic.customTraffic.setMovementsPerHour(decodeMovements(txtMovements.getText()));
    adjustRdbSelected(settings);

    settings.traffic.allowDelays = chkAllowDelays.isSelected();
    settings.traffic.customTraffic.setGeneralAviationProbability(nudNonCommercials.getValue() / 100d);
    settings.traffic.customTraffic.setDepartureProbability(sldArrivalsDepartures.getValue() / 10d);
    settings.traffic.customTraffic.setCompanies(decodeMapFromText(txtCompanies.getText(), ONE_COMPANY_REGEX));
    settings.traffic.customTraffic.setCountryCodes(decodeMapFromText(txtCountryCodes.getText(), ONE_COUNTRY_CODE_REGEX));

    settings.traffic.emergencyPerDayProbability = cmbEmergencyProbability.getSelectedItem().value;
  }

  private void adjustRdbSelected(StartupSettings settings) {
    if (rdbXml.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.xml;
    else if (rdbUser.isSelected())
      settings.traffic.type = StartupSettings.Traffic.eTrafficType.user;
    else
      throw new UnsupportedOperationException();
  }

  private void adjustSelectedRdb(StartupSettings settings) {
    rdbXml.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.xml);
    rdbUser.setSelected(settings.traffic.type == StartupSettings.Traffic.eTrafficType.user);
  }

  private void btnAnalyseTraffic_click(ActionEvent actionEvent) {
    ITrafficModel tfc;
    try {
      tfc = getCurrentTraffic();
    } catch (Exception e) {
      SharedAcc.getAppLog().write(ApplicationLog.eType.critical, "Failed to load traffic. " + ExceptionUtils.toFullString(e));
      MessageBox.show("Failed to obtain the traffic. Check the app log for more info.", "Failed to load traffic...");
      return;
    }

//    FrmTrafficHistogram frm = new FrmTrafficHistogram();
    FrmTrafficBarGraph frm = new FrmTrafficBarGraph();
    frm.init(tfc, "Movements histogram");
    frm.setVisible(true);
  }

  private void createComponents() {

    btnAnalyseTraffic = SwingFactory.createButton("Analyse traffic movements", this::btnAnalyseTraffic_click);

    fleTraffic = new XmlFileSelectorExtender(SwingFactory.FileDialogType.traffic);

    grpRdb = new javax.swing.ButtonGroup();

    rdbXml = new javax.swing.JRadioButton("Load from file");

    rdbUser = new javax.swing.JRadioButton("Use custom traffic");
    rdbUser.addChangeListener(q -> updateCustomPanelState());

    chkAllowDelays = new javax.swing.JCheckBox("Allow traffic delays");

    sldArrivalsDepartures = SwingFactory.createHorizontalBar(0, 10, 5);
    nudMaxPlanes = new NumericUpDownExtender(new JSpinner(), 1, 100, 15, 1);
    nudTrafficDensity = new NumericUpDownExtender(new JSpinner(), 0, 100, 100, 1);
    nudNonCommercials = new NumericUpDownExtender(new JSpinner(), 0, 100, 0, 10);
    txtCompanies = new RegexPatternTextFieldExtender("^[A-Z]{3}(:\\d+)?(;[A-Z]{3}(:\\d+)?)*$");
    txtCountryCodes = new RegexPatternTextFieldExtender("^[A-Z]{2}(:\\d+)?(;[A-Z]{2}(:\\d+)?)*$");
    txtMovements = new RegexPatternTextFieldExtender(ALL_MOVEMENT_REGEX);

    cmbEmergencyProbability = new ComboBoxExtender<>(q -> q.label);
    setCmbEmergencyProbabilityModel();

    grpRdb.add(rdbXml);
    grpRdb.add(rdbUser);
    rdbUser.setSelected(true);
  }

  private JPanel createCustomTrafficPanel() {
    JPanel ret2 = LayoutManager.createFormPanel(5, 2,
        new JLabel("Movements / hour:"), txtMovements.getControl(),
        new JLabel("Overall departure probability:"), sldArrivalsDepartures,
        new JLabel("Overall general aviation probability:"), nudNonCommercials.getControl(),
        new JLabel("Companies:"), txtCompanies.getControl(),
        new JLabel("Country codes:"), txtCountryCodes.getControl());
    return ret2;

//    JPanel ret = LayoutManager.createFormPanel(7, 2,
//        null, chkCustomExtendedCallsigns,
//        new JLabel("Movements / hour:"),
//        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE,
//            nudMovements.getControl(),
//            SwingFactory.createButton("Specify precisely", this::btnSpecifyTraffic_click)),
//        new JLabel("Non-commercial flights (%):"), nudNonCommercials.getControl(),
//        new JLabel("Arrivals <-> Departures"), sldArrivalsDepartures,
//        new JLabel("Companies (ICAO;ICAO;...):"), txtCompanies.getControl(),
//        new JLabel("Country codes (as above):"), txtCountryCodes.getControl(),
//        new JLabel("Plane weights A/B/C/D"), LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, DISTANCE,
//            nudA.getControl(), nudB.getControl(), nudC.getControl(), nudD.getControl())
//    );
//    return ret;
  }

  private JPanel createGlobalPanel() {
    return LayoutManager.createFormPanel(2, 2,
        new JLabel("Max planes count:"),
        LayoutManager.createFlowPanel(
            LayoutManager.eVerticalAlign.baseline,
            super.DISTANCE,
            nudMaxPlanes.getControl(),
            new JLabel("Traffic density (%):"), nudTrafficDensity.getControl(),
            chkAllowDelays),
        new JLabel("Emergencies probability:"),
        LayoutManager.createFlowPanel(
            LayoutManager.eVerticalAlign.baseline,
            super.DISTANCE,
            cmbEmergencyProbability.getControl()));
  }

  private void createLayout() {

    JPanel pnlGlobalTrafficSettings = createGlobalPanel();
    JPanel pnlTrafficSource = createTrafficSourcePanel();
    this.pnlCustomTraffic = createCustomTrafficPanel();

    pnlGlobalTrafficSettings = LayoutManager.createBorderedPanel(8, pnlGlobalTrafficSettings);

    LayoutManager.setPanelBorderText(pnlGlobalTrafficSettings, "Global traffic settings:");
    LayoutManager.setPanelBorderText(pnlTrafficSource, "Used traffic:");
    LayoutManager.setPanelBorderText(this.pnlCustomTraffic, "Custom generic traffic settings:");

    pnlGlobalTrafficSettings.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    pnlTrafficSource.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    pnlCustomTraffic.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    JPanel pnlMain = LayoutManager.createFormPanel(3, 1,
        pnlGlobalTrafficSettings, pnlTrafficSource, this.pnlCustomTraffic);

    pnlMain = LayoutManager.createBorderedPanel(DISTANCE, pnlMain);

    this.add(pnlMain);
  }

  private JPanel createTrafficSourcePanel() {
    return LayoutManager.createFormPanel(3, 2,
        rdbXml,
        LayoutManager.createFlowPanel(fleTraffic.getTextControl(), fleTraffic.getButtonControl()),
        rdbUser, null,
        null, btnAnalyseTraffic);
  }

//  private void btnSpecifyTraffic_click(ActionEvent actionEvent) {
//    GenericTrafficMovementsPerHourPanel pnl = new GenericTrafficMovementsPerHourPanel();
//
//    if (specificMovementValues != null) pnl.setValues(this.specificMovementValues);
//    else pnl.setValues(nudMovements.getValue());
//    SwingFactory.showDialog(pnl, "Specify traffic per hour...", (JDialog) this.getRootPane().getParent());
//    int[] tmp = pnl.getValues();
//    if (tmp != null) {
//      nudMovements.setValue(tmp[0]);
//      specificMovementValues = tmp;
//    }
//  }

  private IMap<String, Integer> decodeMapFromText(String text, String regex) {
    IMap<String, Integer> ret = new EMap<>();
    Pattern p = Pattern.compile(regex);
    String[] pts = text.split(";");
    for (String pt : pts) {
      Matcher m = p.matcher(pt);
      if (m.find()) {
        String a = m.group(1);
        Integer w = m.groupCount() == 2 ? null : Integer.parseInt(m.group(2));
        ret.set(a, w);
      }
    }
    return ret;
  }

  private SimpleGenericTrafficModel.MovementsForHour decodeMovement(String txt) {
    Matcher m = ONE_MOVEMENT_REGEX.matcher(txt);
    EAssert.isTrue(m.find());
    int cnt = Integer.parseInt(m.group(1));
    Double d = m.group(2) == null ? null
        : Double.parseDouble("0." + m.group(2));
    Double g = m.group(3) == null ? null
        : Double.parseDouble("0." + m.group(3));
    SimpleGenericTrafficModel.MovementsForHour ret = new SimpleGenericTrafficModel.MovementsForHour(cnt, g, d);
    return ret;
  }

  private SimpleGenericTrafficModel.MovementsForHour[] decodeMovements(String txt) {
    String[] pts = txt.split(";");
    SimpleGenericTrafficModel.MovementsForHour[] ret = new SimpleGenericTrafficModel.MovementsForHour[24];
    if (pts.length == 1) {
      SimpleGenericTrafficModel.MovementsForHour mvm = decodeMovement(pts[0]);
      for (int i = 0; i < ret.length; i++) {
        ret[i] = mvm;
      }
    } else if (pts.length == 24) {
      for (int i = 0; i < pts.length; i++) {
        SimpleGenericTrafficModel.MovementsForHour mvm = decodeMovement(pts[i]);
        ret[i] = mvm;
      }
    } else {
      throw new EApplicationException("Movement-input-text does not have 1 or 24 blocks.");
    }
    return ret;
  }

  private String encodeMap(IMap<String, Integer> map) {
    IList<String> pts = map.getEntries().select(q -> {
      StringBuilder sb = new StringBuilder();
      sb.append(q.getKey());
      if (q.getValue() != null && q.getValue() != 1)
        sb.append(":").append(q.getValue().toString());
      return sb.toString();
    }).toList();
    String ret = StringUtils.join(";", pts);
    return ret;
  }

  private String encodeMovement(SimpleGenericTrafficModel.MovementsForHour movementPerHour) {
    StringBuilder sb = new StringBuilder();
    sb.append(movementPerHour.count);
    if (movementPerHour.generalAviationProbability != null || movementPerHour.departureProbability != null) {
      sb.append(":");
      if (movementPerHour.departureProbability != null)
        sb.append(movementPerHour.departureProbability.toString().substring(1));
      if (movementPerHour.generalAviationProbability != null)
        sb.append("/").append(movementPerHour.generalAviationProbability.toString().substring(1));
    }
    return sb.toString();
  }

  private String encodeMovements(SimpleGenericTrafficModel.MovementsForHour[] movementsPerHour) {
    String ret;
    int c = movementsPerHour[0].count;
    Double d = movementsPerHour[0].departureProbability;
    Double g = movementsPerHour[0].generalAviationProbability;
    IList<SimpleGenericTrafficModel.MovementsForHour> lst = new EList<>(movementsPerHour);
    if (lst.isAll(
        q -> q.count == c && q.departureProbability == d && q.generalAviationProbability == g
    )) {
      ret = encodeMovement(movementsPerHour[0]);
    } else {
      IList<String> tmp = lst.select(q -> encodeMovement(q));
      ret = StringUtils.join(";", tmp);
    }
    return ret;
  }

  private void fleetsChanged(FleetsSource.Fleets fleets) {
    //TODO Implement this: DO not remember what was this good for.
    throw new ToDoException("DO not remember what was this good for.");
    //txtCompanies.setModel(fleets.getIcaos());
  }

  private ITrafficModel getCurrentTraffic() {
    ITrafficModel ret;
    if (rdbXml.isSelected()) {
      TrafficSource trafficSource = new TrafficXmlSource(fleTraffic.getFileName());
      trafficSource.init();
      ret = trafficSource.getContent();
    } else {
      StartupSettings ss = new StartupSettings();
      fillSettingsBy(ss);
      ret = generateCustomTrafficModel(ss.traffic);
    }

    return ret;
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    createComponents();
    createLayout();
  }

  private void setCmbEmergencyProbabilityByClosestValue(double emergencyPerDayProbability) {
    double minDiff = Double.MAX_VALUE;
    int bestIndex = 0;
    for (int i = 0; i < cmbEmergencyProbability.getItems().size(); i++) {
      double item = cmbEmergencyProbability.getItem(i).value;
      if (Math.abs(item - emergencyPerDayProbability) < minDiff) {
        minDiff = Math.abs(item - emergencyPerDayProbability);
        bestIndex = i;
      }
    }
    cmbEmergencyProbability.setSelectedIndex(bestIndex);
  }

  private void setCmbEmergencyProbabilityModel() {
    IList<DisplayItem<Double>> lst = new EList<>();

    lst.add(new DisplayItem<>("Off", -1d));
    lst.add(new DisplayItem<>("Once per hour", 24d));
    lst.add(new DisplayItem<>("Once per three hours", 8d));
    lst.add(new DisplayItem<>("Once per six hours", 4d));
    lst.add(new DisplayItem<>("Once per twelve hours", 2d));
    lst.add(new DisplayItem<>("Once per day", 1d));
    lst.add(new DisplayItem<>("Once per three days", 1 / 3d));
    lst.add(new DisplayItem<>("Once per week", 1 / 7d));
    lst.add(new DisplayItem<>("Once per two weeks", 1 / 14d));
    lst.add(new DisplayItem<>("Once per month", 1 / 30d));

    cmbEmergencyProbability.addItems(lst);
  }

  private void updateCustomPanelState() {
    if (pnlCustomTraffic != null && rdbUser != null)
      ComponentUtils.adjustComponentTree(pnlCustomTraffic, q -> q.setEnabled(rdbUser.isSelected()));
  }
}
