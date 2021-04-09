package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.game.sources.AreaSource;
import eng.jAtcSim.newLib.gameSim.game.sources.SourceFactory;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

import static eng.eSystem.utilites.FunctionShortcuts.coalesce;

public class AirportAndAirplanesPanel extends JStartupPanel {

  static class AirportInfo {
    public String icao;
    public String title;

    public AirportInfo(String icao, String title) {
      this.icao = icao;
      this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof AirportInfo == false) return false;
      AirportInfo other = (AirportInfo) obj;
      return Objects.equals(this.icao, other.icao);
    }
  }

  private ComboBoxExtender<AirportInfo> cmbAirports;
  private XmlFileSelectorExtender fleFleet;
  private XmlFileSelectorExtender fleGaFleet;
  private XmlFileSelectorExtender fleTypes;
  private XmlFileSelectorExtender fleArea;
  private JButton btnLoadArea;
  private final EventAnonymous<String> onIcaoChanged = new EventAnonymous<>();

  public AirportAndAirplanesPanel() {
    super();
    initComponents();
    fillAirportsComboBox(null);
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    cmbAirports.clearItems();
    AirportInfo ai = new AirportInfo(settings.recent.icao, coalesce(settings.recent.icao, "") + " (area not loaded)");
    cmbAirports.addItem(ai);
    cmbAirports.setSelectedItem(ai);

    fleFleet.setFileName(settings.files.companiesFleetsXmlFile);
    fleGaFleet.setFileName(settings.files.generalAviationFleetsXmlFile);
    fleTypes.setFileName(settings.files.planesXmlFile);
    fleArea.setFileName(settings.files.areaXmlFile);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.recent.icao = cmbAirports.getSelectedItem().icao;
    settings.files.companiesFleetsXmlFile = fleFleet.getFileName();
    settings.files.generalAviationFleetsXmlFile = fleGaFleet.getFileName();
    settings.files.planesXmlFile = fleTypes.getFileName();
    settings.files.areaXmlFile = fleArea.getFileName();
  }

  public EventAnonymous<String> getOnIcaoChanged() {
    return onIcaoChanged;
  }

  private void initComponents() {
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();
  }

  private void createLayout() {

    JPanel pnlArea = createAreaPanel();
    JPanel pnlPlanes = createPlanesPanel();

    LayoutManager.setPanelBorderText(pnlArea, "Area:");
    LayoutManager.setPanelBorderText(pnlPlanes, "Plane types & fleets:");

    pnlArea.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);
    pnlPlanes.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    JPanel pnlMain = LayoutManager.createFormPanel(2, 1,
            pnlArea, pnlPlanes);

    this.add(pnlMain);
  }

  private JPanel createPlanesPanel() {
    JPanel ret = LayoutManager.createFormPanel(3, 3,
            new JLabel("Airplane types:"),
            fleTypes.getTextControl(), fleTypes.getButtonControl(),
            new JLabel("Airlines fleets:"),
            fleFleet.getTextControl(), fleFleet.getButtonControl(),
            new JLabel("General aviation fleets:"),
            fleGaFleet.getTextControl(), fleGaFleet.getButtonControl());
    return ret;
  }

  private JPanel createAreaPanel() {
    JPanel ret = LayoutManager.createFormPanel(2, 2,
            new JLabel("Select area:"),
            LayoutManager.createFlowPanel(
                    fleArea.getTextControl(),
                    fleArea.getButtonControl(),
                    btnLoadArea
            ),
            new JLabel("Select airport:"),
            cmbAirports.getControl()
    );

    return ret;
  }

  private void createComponents() {

    fleFleet = new XmlFileSelectorExtender(SwingFactory.FileDialogType.fleets);
    fleGaFleet = new XmlFileSelectorExtender(SwingFactory.FileDialogType.fleets);
    fleTypes = new XmlFileSelectorExtender(SwingFactory.FileDialogType.types);
    fleArea = new XmlFileSelectorExtender(SwingFactory.FileDialogType.area);
    btnLoadArea = SwingFactory.createButton("Load", this::btnLoadArea_click);
    cmbAirports = new ComboBoxExtender<>(q -> q.title);
    cmbAirports.getOnSelectionChanged().add(o ->
    {
      if (cmbAirports.getSelectedItem() != null)
        this.getOnIcaoChanged().raise(cmbAirports.getSelectedItem().icao);
    });
  }

  private void btnLoadArea_click(ActionEvent actionEvent) {
    btnLoadArea.setEnabled(false);
    AreaSource area = SourceFactory.createAreaSource(fleArea.getFileName(), "");
    try {
      area.init();
    } catch (Exception ex) {
      Context.getApp().getAppLog().write(LogItemType.warning, "Failed to area from '%s'. '%s'", fleFleet.getFileName(),
              ExceptionUtils.toFullString(ex));
      MessageBox.show("Failed to load area from file " + fleFleet.getFileName() + ". " + ex.getMessage(), "Error...");
      btnLoadArea.setEnabled(true);
      return;
    }
    fillAirportsComboBox(area.getContent());

    btnLoadArea.setEnabled(true);
  }


  private void fillAirportsComboBox(Area area) {
    AirportInfo selectedItem = cmbAirports.getSelectedItem();
    IList<AirportInfo> tmp;

    if (area != null) {
      tmp = area.getAirports().select(q ->
              new AirportInfo(q.getIcao(), q.getName() + " [" + q.getIcao() + "]"));
    } else {
      tmp = EList.of(new AirportInfo("----", "(Area not loaded)"));
    }

    cmbAirports.clearItems();
    cmbAirports.addItems(tmp);
    if (selectedItem == null || cmbAirports.getItems().isNone(q -> q.icao.equals(selectedItem.icao)))
      cmbAirports.setSelectedIndex(0);
    else
      cmbAirports.setSelectedItem(selectedItem);
  }

}
