package eng.jAtcSim.startup.startupWizard.panels;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.extenders.ComboBoxExtender;
import eng.jAtcSim.startup.extenders.NumericUpDownExtender;
import eng.jAtcSim.startup.extenders.XComboBoxExtender;

import javax.swing.*;
import java.awt.color.ColorSpace;

public class StaticWeatherPanel extends JPanel {

  private static final int SPACE = 4;

  private NumericUpDownExtender txtWindHeading = new NumericUpDownExtender(new JSpinner(), 0, 360, 40, 1);
  private NumericUpDownExtender txtWindSpeed = new NumericUpDownExtender(new JSpinner(), 0, 100, 4, 1);
  private NumericUpDownExtender txtVisibility = new NumericUpDownExtender(new JSpinner(), 0, 9999, 9999, 100);
  private ComboBoxExtender cmbClouds = new ComboBoxExtender(new JComboBox(),
      new EList(new String[]{"CLR", "FEW", "BKN", "SCT", "OVC"})
      );
  private XComboBoxExtender<Weather> cmbPreset = new XComboBoxExtender<>(new JComboBox(), getPredefinedWeathers());

  private static IMap<String, Weather> getPredefinedWeathers() {
    EMap<String, Weather> ret =new EMap<>();

    String k;
    Weather w;

    k = "Clear";
    w = new Weather(34, 4, 9999, 12_000, .1);
    ret.set(k,w);

    k = "Foggy";
    w = new Weather(61, 2, 100, 100, 1);
    ret.set(k,w);

    k = "Windy";
    w = new Weather(281, 31, 7000, 8_000, .7);
    ret.set(k,w);

    k = "Rainy";
    w = new Weather(174, 17, 1_500, 1000, .8);
    ret.set(k,w);

    return ret;
  }

  private NumericUpDownExtender txtBaseAltitude = new NumericUpDownExtender(new JSpinner(), 0, 20000, 8000, 1000);

  public StaticWeatherPanel() {

    JPanel pnlA = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Wind direction (°):"),
        txtWindHeading.getControl(),
        new JLabel("Wind speed (kts):"),
        txtWindSpeed.getControl());

    JPanel pnlB = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Visibility (meters):"),
        txtVisibility.getControl());

    JPanel pnlC = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Cloud intensity:"),
        cmbClouds.getControl(),
        new JLabel("Base cloud altitude (ft):"),
        txtBaseAltitude.getControl()
        );

    JPanel pnlD = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, SPACE,
        new JLabel("Choose preset:"),
        cmbPreset.getControl(),
        new JButton("Download and use current")
        );

    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.left, SPACE, pnlA, pnlB, pnlC, pnlD);
  }


}
