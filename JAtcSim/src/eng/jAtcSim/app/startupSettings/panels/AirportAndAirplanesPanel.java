package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.app.extenders.SwingFactory;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;
import eng.jAtcSim.lib.weathers.presets.PresetWeatherList;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.XComboBoxExtender;

import javax.swing.*;
import java.awt.*;

public class AirportAndAirplanesPanel extends JStartupPanel {

  private XComboBoxExtender<String> cmbAirports;
  private EventAnonymous<Airport> onAirportChanged = new EventAnonymous<Airport>();
  private XmlFileSelectorExtender fleFleet;
  private XmlFileSelectorExtender fleTypes;
  private XmlFileSelectorExtender fleArea;
  private JButton btnLoadArea;

  public AirportAndAirplanesPanel() {
    super();
    initComponents();
    Sources.getOnAreaChanged().add(this::fillAirportsComboBox);
    cmbAirports.getSelectedItemChanged().add(this::cmbAirports_changed);
  }

  public EventAnonymous getOnAirportChanged() {
    return onAirportChanged;
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    fillAirportsComboBox();
    cmbAirports.setSelectedItem(settings.recent.icao);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.recent.icao = cmbAirports.getSelectedItem();
  }

  private void cmbAirports_changed(Object e) {
    String s = cmbAirports.getSelectedItem();
    //weatherPanel.setRelativeIcao(s);
    if (Sources.getArea() == null)
      onAirportChanged.raise(null);
    else {
      Airport aip = Sources.getArea().getAirports().tryGetFirst(q -> q.getIcao().equals(s));
      onAirportChanged.raise(aip);
    }
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


    JPanel pnlMain = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 4,
        pnlArea, pnlPlanes);

    this.add(pnlMain);
  }

  private JPanel createPlanesPanel() {
    JPanel ret = LayoutManager.createFormPanel(3, 3,
        new JLabel("Airplane types:"),
        fleTypes.getTextControl(), fleTypes.getButtonControl(),
        new JLabel("Fleets:"),
        fleFleet.getTextControl(), fleFleet.getButtonControl(),
        null, null, new JButton("Validate"));
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
    btnLoadArea = new JButton("Load");

    cmbAirports = new XComboBoxExtender<>();
  }

  private void fillAirportsComboBox() {
    EList<XComboBoxExtender.Item<String>> aips = new EList<>();
    Area area = Sources.getArea();
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
    cmbAirports.setSelectedIndex(0);
  }

}
