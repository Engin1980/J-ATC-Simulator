package eng.jAtcSim.startup.startupSettings.panels;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloader;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.startup.startupSettings.StartupSettings;
import eng.jAtcSim.startup.extenders.NumericUpDownExtender;
import eng.jAtcSim.startup.extenders.XComboBoxExtender;

import javax.swing.*;

public class WeatherPanel extends JStartupPanel {

  private static final int SPACE = 4;

  private NumericUpDownExtender txtWindHeading = new NumericUpDownExtender(new JSpinner(), 0, 360, 40, 1);
  private NumericUpDownExtender txtWindSpeed = new NumericUpDownExtender(new JSpinner(), 0, 100, 4, 1);
  private NumericUpDownExtender txtVisibility = new NumericUpDownExtender(new JSpinner(), 0, 9999, 9999, 100);
  private NumericUpDownExtender txtHitProbability = new NumericUpDownExtender(new JSpinner(), 0, 100, 0, 1);
  private XComboBoxExtender<String> cmbClouds = new XComboBoxExtender(
      new String[]{"CLR", "FEW", "SCT", "BKN",  "OVC"}
  );
  private XComboBoxExtender<Weather> cmbPreset = new XComboBoxExtender<>(getPredefinedWeathers());
  private NumericUpDownExtender txtBaseAltitude = new NumericUpDownExtender(new JSpinner(), 0, 20000, 8000, 1000);
  private String icao;

  private static IList<XComboBoxExtender.Item<Weather>> getPredefinedWeathers() {
    IList<XComboBoxExtender.Item<Weather>> ret = new EList<>();

    String k;
    Weather w;
    XComboBoxExtender.Item<Weather> item;

    k = "Clear";
    w = new Weather(34, 4, 9999, 12_000, .1);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    k = "Foggy";
    w = new Weather(61, 2, 100, 100, 1);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    k = "Windy";
    w = new Weather(281, 31, 7000, 8_000, .7);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    k = "Rainy";
    w = new Weather(174, 17, 1_500, 1000, .8);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    return ret;
  }

  public WeatherPanel() {

    JPanel pnlA = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Wind direction (°):"),
        txtWindHeading.getControl(),
        new JLabel("Wind speed (kts):"),
        txtWindSpeed.getControl());

    JPanel pnlB = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Visibility (meters):"),
        txtVisibility.getControl());

    JPanel pnlC = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Cloud intensity (%):"),
        cmbClouds.getControl(),
        txtHitProbability.getControl(),
        new JLabel("Base cloud altitude (ft):"),
        txtBaseAltitude.getControl()
    );

    JButton btnDownload = new JButton("Download and use current");
    btnDownload.addActionListener(q -> btnDownload_click(q));

    JPanel pnlD = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Choose preset:"),
        cmbPreset.getControl(),
        btnDownload
    );

    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.left, SPACE, pnlA, pnlB, pnlC, pnlD);

    cmbClouds.getControl().addActionListener(q->cmbCloudsChanged());
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    StartupSettings.Weather w = settings.weather;

    txtBaseAltitude.setValue(w.cloudBaseAltitudeFt);
    txtVisibility.setValue(w.visibilityInM);
    txtHitProbability.setValue((int) (w.cloudBaseProbability * 100));
    txtWindHeading.setValue(w.windDirection);
    txtWindSpeed.setValue(w.windSpeed);
    selectHitProbabilityComboBoxByValue(w.cloudBaseProbability);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    StartupSettings.Weather w = settings.weather;

    w.cloudBaseAltitudeFt = txtBaseAltitude.getValue();
    w.visibilityInM = txtVisibility.getValue();
    w.cloudBaseProbability  = txtHitProbability.getValue() / 100d;
    w.windDirection = txtWindHeading.getValue();
    w.windSpeed = txtWindSpeed.getValue();
  }

  private void cmbCloudsChanged() {
    String item = cmbClouds.getSelectedItem();
    switch (item){
      case "CLR":
        txtHitProbability.setValue(0);
        break;
      case "FEW":
        txtHitProbability.setValue(1000/80);
        break;
      case "SCT":
        txtHitProbability.setValue(4000/80);
        break;
      case "BKN":
        txtHitProbability.setValue(6000/80);
        break;
      case "OVC":
        txtHitProbability.setValue(100);
        break;
      default:
        throw new EEnumValueUnsupportedException(item);
    }
  }

  public void setRelativeIcao(String icao) {
    this.icao = icao;
  }

  private void btnDownload_click(java.awt.event.ActionEvent evt) {
    MetarDownloader down = new MetarDownloaderNoaaGov();
    String s;
    Weather w;

    try {
      s = down.downloadMetar(icao);
      w = MetarDecoder.decode(s);
      setWeather(w);
    } catch (Exception ex) {
      EStringBuilder sb = new EStringBuilder();
      sb.appendFormatLine("Failed to download METAR for airport with code: %s. Reason:", icao);
      sb.appendLine(ExceptionUtil.toFullString(ex, "\n"));
      MessageBox.show(sb.toString(), "Error...");
    }
  }

  private void setWeather(Weather w) {
    txtBaseAltitude.setValue(w.getCloudBaseInFt());
    txtVisibility.setValue(w.getVisibilityInMeters());
    txtWindHeading.setValue(w.getWindHeading());
    txtWindSpeed.setValue(w.getWindSpeetInKts());
    txtHitProbability.setValue((int) (w.getCloudBaseHitProbability() * 100));
    selectHitProbabilityComboBoxByValue(w.getCloudBaseHitProbability());
  }

  private void selectHitProbabilityComboBoxByValue(double value){
    value = value * .8;
    if (value == 0)
      cmbClouds.setSelectedItem("CLR");
    else if (value < .2)
      cmbClouds.setSelectedItem("FEW");
    else if (value < .4)
      cmbClouds.setSelectedItem("SCT");
    else if (value < .8)
      cmbClouds.setSelectedItem("BKN");
    else
      cmbClouds.setSelectedItem("OVC");
  }
}
