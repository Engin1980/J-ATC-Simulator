package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.decoders.MetarDecoder;
import eng.jAtcSim.newLib.weather.downloaders.MetarDownloader;
import eng.jAtcSim.newLib.weather.downloaders.MetarDownloaderNoaaGov;
import eng.jAtcSim.shared.BackgroundWorker;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.NumericUpDownExtender;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CustomWeatherPanel extends JStartupPanel {

  private static final String DOWNLOAD_BUTTON_TEXT = "Download and use current";
  private static final String DOWNLOADING_BUTTON_TEXT = "...downloading, please wait";

  private static class PredefinedWeather{
    public String title;
    public Weather weather;

    public PredefinedWeather(String title, Weather weather) {
      this.title = title;
      this.weather = weather;
    }
  }

  private NumericUpDownExtender txtWindHeading = new NumericUpDownExtender(new JSpinner(), 0, 360, 40, 1);
  private NumericUpDownExtender txtWindSpeed = new NumericUpDownExtender(new JSpinner(), 0, 100, 4, 1);
  private NumericUpDownExtender txtVisibility = new NumericUpDownExtender(new JSpinner(), 0, 9999, 9999, 100);
  private NumericUpDownExtender txtHitProbability = new NumericUpDownExtender(new JSpinner(), 0, 100, 0, 1);
  private ComboBoxExtender<String> cmbClouds = new ComboBoxExtender();
  private ComboBoxExtender<StartupSettings.Weather.eSnowState> cmbSnowState = new ComboBoxExtender<>();
  private ComboBoxExtender<PredefinedWeather> cmbPreset = new ComboBoxExtender<>(q->q.title);
  private NumericUpDownExtender txtBaseAltitude = new NumericUpDownExtender(new JSpinner(), 0, 20000, 8000, 1000);
  private String icao;
  private JButton btnDownload;

  private static IList<PredefinedWeather> getPredefinedWeathers() {
    IList<PredefinedWeather> ret = new EList<>();
PredefinedWeather pw;

    String k;
    Weather w;

    k = "Clear";
    w = new Weather(34, 4, 4, 9999, 12_000, .1, Weather.eSnowState.none);
    pw = new PredefinedWeather(k,w);
    ret.add(pw);

    k = "Foggy";
    w = new Weather(61, 2, 2, 100, 100, 1, Weather.eSnowState.none);
    pw = new PredefinedWeather(k,w);
    ret.add(pw);

    k = "Windy";
    w = new Weather(281, 31, 46, 7000, 8_000, .7, Weather.eSnowState.none);
    pw = new PredefinedWeather(k,w);
    ret.add(pw);

    k = "Rainy";
    w = new Weather(174, 17, 21, 1_500, 1000, .8, Weather.eSnowState.none);
    pw = new PredefinedWeather(k,w);
    ret.add(pw);

    k = "Snow showers";
    w = new Weather(174, 11, 11, 3_500, 1000, .8, Weather.eSnowState.normal);
    pw = new PredefinedWeather(k,w);
    ret.add(pw);

    k = "Heavy snow";
    w = new Weather(174, 22, 31, 700, 1000, 1, Weather.eSnowState.intensive);
    pw = new PredefinedWeather(k,w);
    ret.add(pw);

    return ret;
  }

  public CustomWeatherPanel() {
    this.cmbClouds.addItems(new String[]{"CLR", "FEW", "SCT", "BKN", "OVC"});
    this.cmbSnowState.addItems( new StartupSettings.Weather.eSnowState[]{
        StartupSettings.Weather.eSnowState.none, StartupSettings.Weather.eSnowState.normal, StartupSettings.Weather.eSnowState.intensive});
this.cmbPreset.addItems(getPredefinedWeathers());

    this.btnDownload = new JButton(DOWNLOAD_BUTTON_TEXT);
    btnDownload.addActionListener(q -> btnDownload_click(q));
    cmbClouds.getControl().addActionListener(q -> cmbCloudsChanged());
    cmbPreset.getControl().addActionListener(this::cmbPreset_selectedItemChanged);


    JPanel pnlA = LayoutManager.createFormPanel(7, 2,
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
        new JLabel("Snow state:"),
        cmbSnowState.getControl(),
        new JLabel("Choose preset:"),
        LayoutManager.createFlowPanel(
            cmbPreset.getControl(),
            btnDownload));

    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.left, 4, pnlA);
  }

  public void setRelativeIcao(String icao) {
    this.icao = icao;
    btnDownload.setEnabled(StringUtils.isNullOrWhitespace(icao) == false);
    if (btnDownload.isEnabled())
      btnDownload.setText("Download for '" + icao + "'");
    else
      btnDownload.setText("(select airport first)");
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    StartupSettings.Weather w = settings.weather;

    txtBaseAltitude.setValue(w.cloudBaseAltitudeFt);
    txtVisibility.setValue(w.visibilityInM);
    txtHitProbability.setValue((int) (w.cloudBaseProbability * 100));
    txtWindHeading.setValue(w.windDirection);
    txtWindSpeed.setValue(w.windSpeed);
    cmbSnowState.setSelectedItem(w.snowState);
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
    w.snowState = cmbSnowState.getSelectedIndex() == 0
        ? StartupSettings.Weather.eSnowState.none
        : cmbSnowState.getSelectedIndex() == 1
        ? StartupSettings.Weather.eSnowState.normal
        : StartupSettings.Weather.eSnowState.intensive;
  }

  private void cmbPreset_selectedItemChanged(ActionEvent actionEvent) {
    Weather w = cmbPreset.getSelectedItem().weather;
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
    cmbSnowState.setSelectedLabel(w.getSnowState().toString());
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
