package eng.jAtcSim.startup.startupSettings.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.startup.startupSettings.StartupSettings;
import eng.jAtcSim.startup.extenders.XComboBoxExtender;

import javax.swing.*;

public class AirportAndWeatherPanel extends JStartupPanel {

  private XComboBoxExtender<String> cmbAirports;
  private JRadioButton rdbWeatherFromUser;
  private JRadioButton rdbWeatherFromWeb;
  private WeatherPanel weatherPanel;

  public AirportAndWeatherPanel() {
    super();
    initComponents();
    Sources.getAreaChanged().add(this::fillAirportsComboBox);
    cmbAirports.getSelectedItemChanged().add(this::cmbAirports_changed);
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    fillAirportsComboBox();

    cmbAirports.setSelectedItem(settings.recent.icao);

    if (settings.weather.useOnline)
      rdbWeatherFromWeb.setSelected(true);
    else
      rdbWeatherFromUser.setSelected(true);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.recent.icao = cmbAirports.getSelectedItem();
    settings.weather.useOnline = rdbWeatherFromWeb.isSelected();
  }

  private void cmbAirports_changed(Object e) {
    Object o = cmbAirports.getSelectedItem();
    weatherPanel.setRelativeIcao((String) o);
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
        rdbWeatherFromUser,
        wp
    );

    wp = LayoutManager.createFormPanel(2, 2,
        new JLabel("Select airport:"),
        cmbAirports.getControl(),
        new JLabel("Weather"),
        wp
    );

    this.add(wp);
  }

  private void createComponents() {

    rdbWeatherFromWeb = new JRadioButton();
    rdbWeatherFromWeb.addActionListener((e) -> {
      ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(false));
    });
    rdbWeatherFromWeb.setText("use real weather continuously downloaded from web");

    rdbWeatherFromUser = new JRadioButton();
    rdbWeatherFromUser.addActionListener((e) -> {
      ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(true));
    });
    rdbWeatherFromUser.setSelected(true);
    rdbWeatherFromUser.setText("user set - insert METAR string:");

    ButtonGroup group = new ButtonGroup();
    group.add(rdbWeatherFromUser);
    group.add(rdbWeatherFromWeb);

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
