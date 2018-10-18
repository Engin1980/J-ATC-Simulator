package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.decoders.MetarDecoder;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloader;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;
import eng.jAtcSim.shared.BackgroundWorker;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.NumericUpDownExtender;
import eng.jAtcSim.app.extenders.XComboBoxExtender;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CustomWeatherPanel extends JStartupPanel {

  private static final int SPACE = 4;
  private static final String DOWNLOAD_BUTTON_TEXT = "Download and use current";
  private static final String DOWNLOADING_BUTTON_TEXT = "...downloading, please wait";

  private NumericUpDownExtender txtWindHeading = new NumericUpDownExtender(new JSpinner(), 0, 360, 40, 1);
  private NumericUpDownExtender txtWindSpeed = new NumericUpDownExtender(new JSpinner(), 0, 100, 4, 1);
  private NumericUpDownExtender txtVisibility = new NumericUpDownExtender(new JSpinner(), 0, 9999, 9999, 100);
  private NumericUpDownExtender txtHitProbability = new NumericUpDownExtender(new JSpinner(), 0, 100, 0, 1);
  private XComboBoxExtender<String> cmbClouds = new XComboBoxExtender(
      new String[]{"CLR", "FEW", "SCT", "BKN", "OVC"}
  );
  private XComboBoxExtender<Weather> cmbPreset = new XComboBoxExtender<>(getPredefinedWeathers());
  private NumericUpDownExtender txtBaseAltitude = new NumericUpDownExtender(new JSpinner(), 0, 20000, 8000, 1000);
  private String icao;
  private JButton btnDownload;

  public void setRelativeIcao(String icao) {
    this.icao = icao;
    btnDownload.setEnabled(StringUtils.isNullOrWhitespace(icao) == false);
    if (btnDownload.isEnabled())
      btnDownload.setText("Download for '" + icao+ "'");
    else
      btnDownload.setText("(select airport first)");
  }

  private static IList<XComboBoxExtender.Item<Weather>> getPredefinedWeathers() {
    IList<XComboBoxExtender.Item<Weather>> ret = new EList<>();

    String k;
    Weather w;
    XComboBoxExtender.Item<Weather> item;

    k = "Clear";
    w = new Weather(34, 4, 4, 9999, 12_000, .1);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    k = "Foggy";
    w = new Weather(61, 2, 2, 100, 100, 1);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    k = "Windy";
    w = new Weather(281, 31, 46, 7000, 8_000, .7);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    k = "Rainy";
    w = new Weather(174, 17, 21, 1_500, 1000, .8);
    item = new XComboBoxExtender.Item(k, w);
    ret.add(item);

    return ret;
  }

  public CustomWeatherPanel() {

    this.btnDownload = new JButton(DOWNLOAD_BUTTON_TEXT);
    btnDownload.addActionListener(q -> btnDownload_click(q));
    cmbClouds.getControl().addActionListener(q -> cmbCloudsChanged());
    cmbPreset.getControl().addActionListener(this::cmbPreset_selectedItemChanged);


    JPanel pnlA = LayoutManager.createFormPanel(6, 2,
        new JLabel("Wind direction (°):"),
        txtWindHeading.getControl(),
        new JLabel("Wind speed (kts):"),
        txtWindSpeed.getControl(),
        new JLabel("Visibility (meters):"),
        txtVisibility.getControl(),
        new JLabel("Cloud intensity (%):"),
        LayoutManager.createFlowPanel(
            cmbClouds.getControl(),
            txtHitProbability.getControl()),
        new JLabel("Base cloud altitude (ft):"),
        txtBaseAltitude.getControl(),
        new JLabel("Choose preset:"),
        LayoutManager.createFlowPanel(
            cmbPreset.getControl(),
            btnDownload));

    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.left, 4, pnlA);
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
    w.cloudBaseProbability = txtHitProbability.getValue() / 100d;
    w.windDirection = txtWindHeading.getValue();
    w.windSpeed = txtWindSpeed.getValue();
  }

  private void cmbPreset_selectedItemChanged(ActionEvent actionEvent) {
    Weather w = cmbPreset.getSelectedItem();
    this.setWeather(w);
  }

  private void cmbCloudsChanged() {
    String item = cmbClouds.getSelectedItem();
    switch (item) {
      case "CLR":
        txtHitProbability.setValue(0);
        break;
      case "FEW":
        txtHitProbability.setValue(1500 / 80);
        break;
      case "SCT":
        txtHitProbability.setValue(4000 / 80);
        break;
      case "BKN":
        txtHitProbability.setValue(6000 / 80);
        break;
      case "OVC":
        txtHitProbability.setValue(100);
        break;
      default:
        throw new EEnumValueUnsupportedException(item);
    }
  }

  private void btnDownload_click(java.awt.event.ActionEvent evt) {
    btnDownload.setText(DOWNLOADING_BUTTON_TEXT);
    btnDownload.setEnabled(false);

    BackgroundWorker<Tuple<String, Weather>> bw = new BackgroundWorker<>(
        this::metarDownloadStart,
        this::metarDownloadFinished);
    bw.start();
  }

  private Tuple<String, Weather> metarDownloadStart() {
    MetarDownloader down = new MetarDownloaderNoaaGov();
    String s = down.downloadMetar(icao);
    Weather w = MetarDecoder.decode(s);
    Tuple<String, Weather> ret = new Tuple<>(s, w);
    return ret;
  }

  private void metarDownloadFinished(Tuple<String, Weather> result, Exception ex) {
    if (result != null) {
      setWeather(result.getB());
    } else {
      EStringBuilder sb = new EStringBuilder();
      sb.appendFormatLine("Failed to download METAR for airport with code: %s. Reason:", icao);
      sb.appendLine(ExceptionUtils.toFullString(ex, "\n"));
      MessageBox.show(sb.toString(), "Error...");
    }
    btnDownload.setText(DOWNLOAD_BUTTON_TEXT);
    btnDownload.setEnabled(true);
  }

  private void setWeather(Weather w) {
    txtBaseAltitude.setValue(w.getCloudBaseInFt());
    txtVisibility.setValue(w.getVisibilityInMeters());
    txtWindHeading.setValue(w.getWindHeading());
    txtWindSpeed.setValue(w.getWindSpeetInKts());
    txtHitProbability.setValue((int) (w.getCloudBaseHitProbability() * 100));
    selectHitProbabilityComboBoxByValue(w.getCloudBaseHitProbability());
  }

  private void selectHitProbabilityComboBoxByValue(double value) {
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
