package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.app.extenders.SwingFactory;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;

public class WeatherPanel extends JStartupPanel {
  private CustomWeatherPanel weatherPanel;
  private JRadioButton rdbWeatherFromUser;
  private JRadioButton rdbWeatherFromWeb;
  private JRadioButton rdbWeatherFromFile;
  private XmlFileSelectorExtender fleWeather;

  public WeatherPanel() {
    initComponents();
    createLayout();
  }

  private void createLayout() {
    JPanel pnlWeather = createWeatherPanel();
    JPanel pnlUserWeather = createUserWeatherPanel();
    LayoutManager.setPanelBorderText(pnlWeather, "Weather source:");
    LayoutManager.setPanelBorderText(pnlUserWeather, "Custom weather settings:");
    JPanel tmp = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 4,
        pnlWeather, pnlUserWeather);

    this.add(tmp);
  }

  private void initComponents() {
    fleWeather = new XmlFileSelectorExtender(SwingFactory.FileDialogType.weather);
    this.weatherPanel = new CustomWeatherPanel();

    rdbWeatherFromWeb = new JRadioButton();
    rdbWeatherFromWeb.addActionListener(
        e -> ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(false)));
    rdbWeatherFromWeb.setText("online weather from web");

    rdbWeatherFromFile = new JRadioButton();
    rdbWeatherFromWeb.addActionListener(
        e -> ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(false)));
    rdbWeatherFromFile.setText("from file");

    rdbWeatherFromUser = new JRadioButton();
    rdbWeatherFromUser.addActionListener(
        e -> ComponentUtils.adjustComponentTree(this.weatherPanel, q -> q.setEnabled(true)));
    rdbWeatherFromUser.setSelected(true);
    rdbWeatherFromUser.setText("custom weather:");

    ButtonGroup group = new ButtonGroup();
    group.add(rdbWeatherFromUser);
    group.add(rdbWeatherFromWeb);
    group.add(rdbWeatherFromFile);
  }

  private JPanel createWeatherPanel() {
    JPanel ret = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, DISTANCE,
        rdbWeatherFromWeb,
        LayoutManager.createFlowPanel(
            LayoutManager.eVerticalAlign.baseline, 4,
            rdbWeatherFromFile,
            LayoutManager.createFlowPanel(fleWeather.getTextControl(), fleWeather.getButtonControl())),
        rdbWeatherFromUser
    );
    return ret;
  }

  private JPanel createUserWeatherPanel() {
    this.weatherPanel = new CustomWeatherPanel();
    return this.weatherPanel;
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    switch (settings.weather.type) {
      case user:
        rdbWeatherFromUser.setSelected(true);
        break;
      case online:
        rdbWeatherFromWeb.setSelected(true);
        break;
      case xml:
        rdbWeatherFromFile.setSelected(true);
        break;
      default:
        throw new EEnumValueUnsupportedException(settings.weather.type);
    }
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    if (rdbWeatherFromFile.isSelected())
      settings.weather.type = StartupSettings.Weather.WeatherSourceType.xml;
    else if (rdbWeatherFromWeb.isSelected())
      settings.weather.type = StartupSettings.Weather.WeatherSourceType.online;
    else
      settings.weather.type = StartupSettings.Weather.WeatherSourceType.user;
  }
}
