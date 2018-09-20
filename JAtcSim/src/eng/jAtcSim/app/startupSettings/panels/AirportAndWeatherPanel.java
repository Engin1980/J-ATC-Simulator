package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.lib.weathers.presets.PresetWeatherList;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.XComboBoxExtender;

import javax.swing.*;

public class AirportAndWeatherPanel extends JStartupPanel {

  private XComboBoxExtender<String> cmbAirports;
  private JRadioButton rdbWeatherFromUser;
  private JRadioButton rdbWeatherFromWeb;
  private JRadioButton rdbWeatherFromFile;
  private XComboBoxExtender<PresetWeatherList> cmbWeatherSets;
  private WeatherPanel weatherPanel;
  private EventAnonymous<Airport> onAirportChanged = new EventAnonymous<Airport>();

  public AirportAndWeatherPanel() {
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

    switch (settings.weather.type){
      case constant:
        rdbWeatherFromUser.setSelected(true);
        break;
      case online:
        rdbWeatherFromWeb.setSelected(true);
        break;
      case preset:
        rdbWeatherFromFile.setSelected(true);
        break;
      default:
        throw new EEnumValueUnsupportedException(settings.weather.type);
    }
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.recent.icao = cmbAirports.getSelectedItem();
    if (rdbWeatherFromFile.isSelected())
      settings.weather.type = StartupSettings.Weather.WeatherSourceType.preset;
    else if (rdbWeatherFromWeb.isSelected())
      settings.weather.type = StartupSettings.Weather.WeatherSourceType.online;
    else
      settings.weather.type = StartupSettings.Weather.WeatherSourceType.constant;
  }

  private void cmbAirports_changed(Object e) {
    String s = cmbAirports.getSelectedItem();
    weatherPanel.setRelativeIcao(s);
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

    this.weatherPanel = new WeatherPanel();

    JPanel wp = LayoutManager.indentPanel(this.weatherPanel, 30);
    wp = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE,
        rdbWeatherFromWeb,
        LayoutManager.createFlowPanel(
            LayoutManager.eVerticalAlign.baseline, 4,
            rdbWeatherFromFile, cmbWeatherSets.getControl()),
        rdbWeatherFromUser,
        wp
    );

    wp = LayoutManager.createFormPanel(2, 2,
        new JLabel("Select airport:"),
        cmbAirports.getControl(),
        new JLabel("Weather to use:"),
        wp
    );

    this.add(wp);
  }

  private void createComponents() {

    rdbWeatherFromWeb = new JRadioButton();
    rdbWeatherFromWeb.addActionListener((e) -> {
      ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(false));
    });
    rdbWeatherFromWeb.setText("online weather from web");

    cmbWeatherSets = new XComboBoxExtender<>();
    rdbWeatherFromFile = new JRadioButton();
    rdbWeatherFromWeb.addActionListener((e) -> {
      ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(false));
    });
    rdbWeatherFromFile.setText("preset weather from file");

    rdbWeatherFromUser = new JRadioButton();
    rdbWeatherFromUser.addActionListener((e) -> {
      ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(true));
    });
    rdbWeatherFromUser.setSelected(true);
    rdbWeatherFromUser.setText("custom weather:");

    ButtonGroup group = new ButtonGroup();
    group.add(rdbWeatherFromUser);
    group.add(rdbWeatherFromWeb);
    group.add(rdbWeatherFromFile);

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
