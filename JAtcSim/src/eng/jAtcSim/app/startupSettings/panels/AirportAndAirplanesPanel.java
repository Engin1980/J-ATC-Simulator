package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.global.newSources.AreaSource;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.XComboBoxExtender;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AirportAndAirplanesPanel extends JStartupPanel {

  private XComboBoxExtender<String> cmbAirports;
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
    cmbAirports.setSelectedItem(settings.recent.icao);

    fleFleet.setFileName(settings.files.fleetsXmlFile);
    fleTypes.setFileName(settings.files.planesXmlFile);
    fleArea.setFileName(settings.files.areaXmlFile);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.recent.icao = cmbAirports.getSelectedItem();
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
    cmbAirports = new XComboBoxExtender<>();
    cmbAirports.getOnSelectedItemChanged().add(o ->
        this.getOnIcaoChanged().raise(cmbAirports.getSelectedItem()));
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
    String selectedItem = cmbAirports.getSelectedItem();
    EList<XComboBoxExtender.Item<String>> aips = new EList<>();
    if (area != null) {
      for (Airport airport : area.getAirports()) {
        aips.add(
            new XComboBoxExtender.Item<>(
                airport.getName() + " [" + airport.getIcao() + "]",
                airport.getIcao()));
      }
    } else {
      aips.add(
          new XComboBoxExtender.Item<>(
              "Area not loaded", "----"));
    }

    cmbAirports.setModel(aips);
    if (selectedItem == null)
      cmbAirports.setSelectedIndex(0);
    else
      cmbAirports.setSelectedItem(selectedItem);
  }

}
