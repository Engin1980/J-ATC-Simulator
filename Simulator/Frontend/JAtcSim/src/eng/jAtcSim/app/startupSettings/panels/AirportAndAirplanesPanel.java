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
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.global.logging.ApplicationLog;
import eng.jAtcSim.newLib.area.global.newSources.AreaSource;
import eng.jAtcSim.newLib.world.Area;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AirportAndAirplanesPanel extends JStartupPanel {

  static class AirportInfo{
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
      return this.icao.equals(other.icao);
    }
  }

  private ComboBoxExtender<AirportInfo> cmbAirports;
  private XmlFileSelectorExtender fleFleet;
  private XmlFileSelectorExtender fleTypes;
  private XmlFileSelectorExtender fleArea;
  private JButton btnLoadArea;
  private final EventAnonymous<String> onIcaoChanged = new EventAnonymous<>();

  public AirportAndAirplanesPanel() {
    super();
    initComponents();
    fillAirportsComboBox(null);
  }

  public EventAnonymous<String> getOnIcaoChanged() {
    return onIcaoChanged;
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    cmbAirports.clearItems();
    AirportInfo ai = new AirportInfo(settings.recent.icao, settings.recent.icao + " (area not loaded)");
    cmbAirports.addItem(ai);
    cmbAirports.setSelectedItem(ai);

    fleFleet.setFileName(settings.files.fleetsXmlFile);
    fleTypes.setFileName(settings.files.planesXmlFile);
    fleArea.setFileName(settings.files.areaXmlFile);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.recent.icao = cmbAirports.getSelectedItem().icao;
    settings.files.fleetsXmlFile = fleFleet.getFileName();
    settings.files.planesXmlFile = fleTypes.getFileName();
    settings.files.areaXmlFile = fleArea.getFileName();
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
    JPanel ret = LayoutManager.createFormPanel(2, 3,
        new JLabel("Airplane types:"),
        fleTypes.getTextControl(), fleTypes.getButtonControl(),
        new JLabel("Fleets:"),
        fleFleet.getTextControl(), fleFleet.getButtonControl());
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
    fleTypes = new XmlFileSelectorExtender(SwingFactory.FileDialogType.types);
    fleArea = new XmlFileSelectorExtender(SwingFactory.FileDialogType.area);
    btnLoadArea = SwingFactory.createButton("Load", this::btnLoadArea_click);
    cmbAirports = new ComboBoxExtender<>(q->q.title);
    cmbAirports.getOnSelectionChanged().add(o ->
    {
      if (cmbAirports.getSelectedItem() != null)
        this.getOnIcaoChanged().raise(cmbAirports.getSelectedItem().icao);
    });
  }

  private void btnLoadArea_click(ActionEvent actionEvent) {
    btnLoadArea.setEnabled(false);
    AreaSource area = new AreaSource(fleArea.getFileName(), "");
    try{
      area.init();
    } catch (Exception ex){
      Acc.log().writeLine(ApplicationLog.eType.warning, "Failed to area from '%s'. '%s'", fleFleet.getFileName(),
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
      tmp = area.getAirports().select(q->
          new AirportInfo(q.getIcao(), q.getName() + " [" + q.getIcao() + "]"));
    } else {
      tmp = new EList<>( new AirportInfo[]{new AirportInfo("----", "(Area not loaded)")});
    }

    cmbAirports.clearItems();
    cmbAirports.addItems(tmp);
    if (selectedItem == null || cmbAirports.getItems().isNone(q->q.icao.equals(selectedItem.icao)))
      cmbAirports.setSelectedIndex(0);
    else
      cmbAirports.setSelectedItem(selectedItem);
  }

}
