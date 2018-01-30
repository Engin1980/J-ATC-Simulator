/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup;

import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.startup.extenders.TimeExtender;
import eng.jAtcSim.lib.global.TryResult;
import eng.jAtcSim.lib.weathers.MetarDecoder;
import eng.jAtcSim.lib.weathers.MetarDownloader;
import eng.jAtcSim.lib.weathers.MetarDownloaderNoaaGov;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;

/**
 * @author Marek Vajgl
 */
public class FrmWizardAirportTimeAndWeather extends FrmWizardFrame {

  private static List<PresetMetar> presetMetars = new ArrayList();

  static {
    presetMetars.add(new PresetMetar("--- choose ---", null));
    presetMetars.add(new PresetMetar("Normal", "METAR LKPR 121200Z 21004KT 9999 FEW030 16/12 Q1001"));
    presetMetars.add(new PresetMetar("Windy", "METAR LKPR 121200Z 05018G34KT 9999 SCT050 13/10 Q1011"));
    presetMetars.add(new PresetMetar("Stormy", "METAR LKPR 121200Z 18011G10KT 6000 TSRA+ OVC050TCU 27/09 Q0996"));
    presetMetars.add(new PresetMetar("Foggy", "METAR LKPR 121200Z 00000KT 0350 OVC020 19/11 Q1013"));
  }

  /**
   * Creates new form FrmWizardAirportAndTraffic
   */
  public FrmWizardAirportTimeAndWeather() {
    super();
    initComponents();
    initExtenders();

    DefaultComboBoxModel model = new DefaultComboBoxModel();
    for (PresetMetar presetMetar : presetMetars) {
      model.addElement(presetMetar);
    }
    cmbPresetMetars.setModel(model);
  }

  private void cmbPresetMetarsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPresetMetarsActionPerformed
    if (cmbPresetMetars.getSelectedIndex() == 0) return;

    PresetMetar pm = (PresetMetar) cmbPresetMetars.getSelectedItem();
    txtMetar.setText(pm.metar);
    cmbPresetMetars.setSelectedIndex(0);
  }//GEN-LAST:event_cmbPresetMetarsActionPerformed

  private void btnDownloadMetarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloadMetarActionPerformed
    MetarDownloader d = new MetarDownloaderNoaaGov();
    TryResult<String> res = d.tryDownloadMetar(settings.recent.icao);
    if (res.isSuccess)
      txtMetar.setText(res.result);
    else
      MessageBox.show("Failed to download METAR for " + settings.recent.icao + ". Reason: " + res.exceptionOrNull.getMessage(), "Error downloading METAR...");
  }//GEN-LAST:event_btnDownloadMetarActionPerformed

  private javax.swing.JButton btnContinue;
  private javax.swing.JButton btnDownloadMetar;
  private javax.swing.JButton btnSetCurrentTime;
  private javax.swing.JComboBox cmbAirports;
  private javax.swing.JComboBox cmbPresetMetars;
  private javax.swing.JComboBox cmbWeatherUpdate;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JRadioButton rdbWeatherFromUser;
  private javax.swing.JRadioButton rdbWeatherFromWeb;
  private javax.swing.JTextField txtMetar;
  private javax.swing.JTextField txtTime;

  private TimeExtender timeExtender;

  private void initComponents() {
    this.setTitle("");
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();

    pack();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
  }

  private void createLayout() {

    JPanel wp = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, distance,
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top, distance, txtMetar, btnDownloadMetar),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top , distance, jLabel2, cmbPresetMetars),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top, distance, jLabel1, cmbWeatherUpdate)
    );
    wp = LayoutManager.indentPanel(wp, 30);
    wp = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, distance,
        rdbWeatherFromWeb,
        rdbWeatherFromUser,
        wp
    );


    wp = LayoutManager.createFormPanel(3, 2,
        jLabel4, cmbAirports,
        jLabel5, LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top, distance, txtTime,  btnSetCurrentTime),
        jLabel6, wp
    );

    wp = super.wrapWithContinueButton(wp, btnContinue);

    this.getContentPane().add(wp);
  }

  private void createComponents() {
    jLabel4 = new JLabel();
    cmbAirports = new JComboBox();
    jLabel5 = new JLabel();
    txtTime = new JTextField();
    jLabel6 = new JLabel();
    rdbWeatherFromWeb = new JRadioButton();
    rdbWeatherFromUser = new JRadioButton();
    txtMetar = new JTextField();
    btnDownloadMetar = new JButton();
    btnContinue = new JButton();
    jLabel1 = new JLabel();
    cmbWeatherUpdate = new JComboBox();
    btnSetCurrentTime = new JButton();
    jLabel2 = new JLabel();
    cmbPresetMetars = new JComboBox();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    jLabel4.setText("Select airport:");

    cmbAirports.setModel(new DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));

    jLabel5.setText("Simulation start time:");

    txtTime.setHorizontalAlignment(JTextField.CENTER);
    txtTime.setText("8:57");
    txtTime.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        txtTimeKeyReleased(evt);
      }
    });

    jLabel6.setText("Weather:");



    rdbWeatherFromWeb.setText("use real weather continously downloaded from web");

    rdbWeatherFromUser.setSelected(true);
    rdbWeatherFromUser.setText("user set - insert METAR string:");

    ButtonGroup group = new ButtonGroup();
    group.add(rdbWeatherFromUser);
    group.add(rdbWeatherFromWeb);

    txtMetar.setText("METAR ZZZZ 111111Z 20212KTS 9000 OVC012");

    btnDownloadMetar.setText("Download now");
    btnDownloadMetar.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDownloadMetarActionPerformed(evt);
      }
    });

    btnContinue.setText("Continue");
    btnContinue.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnContinueActionPerformed(evt);
      }
    });

    jLabel1.setText("... random weather update:");

    cmbWeatherUpdate.setModel(new DefaultComboBoxModel(new String[]{"None", "Slight", "Moderate", "Significant"}));

    btnSetCurrentTime.setText("Set current");
    btnSetCurrentTime.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnSetCurrentTimeActionPerformed(evt);
      }
    });

    jLabel2.setText("... choose from preset:");

    cmbPresetMetars.setModel(new DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
    cmbPresetMetars.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cmbPresetMetarsActionPerformed(evt);
      }
    });
  }

  @Override
  protected void fillBySettings() {
    fillAirportsComboBox();

    txtTime.setText(settings.recent.time.toString());
    txtMetar.setText(settings.weather.metar);

    cmbWeatherUpdate.setSelectedIndex(settings.weather.userChanges);
    if (settings.weather.useOnline)
      rdbWeatherFromWeb.setSelected(true);
    else
      rdbWeatherFromUser.setSelected(true);
  }

  private void fillAirportsComboBox() {
    Area area = XmlLoadHelper.loadNewArea(settings.files.areaXmlFile);

    int selectedIndex = -1;
    String[] data = new String[area.getAirports().size()];
    for (int i = 0; i < area.getAirports().size(); i++) {
      Airport aip = area.getAirports().get(i);
      data[i] = aip.getIcao() + " - " + aip.getName();

      if (aip.getIcao().equals(settings.recent.icao))
        selectedIndex = i;
    }
    ComboBoxModel<String> model = new DefaultComboBoxModel<>(data);
    cmbAirports.setModel(model);
    if (selectedIndex >= 0)
      cmbAirports.setSelectedIndex(selectedIndex);
  }

  private void initExtenders() {
    this.timeExtender = new TimeExtender(txtTime);
  }

  private boolean checkTimeSanity() {
    if (timeExtender.isValid() == false) {
      MessageBox.show("Selected time value is not valid.", "Error...");
      return false;
    }

    return true;
  }

  private boolean checkMetarSanity() {
    TryResult<Weather> res = MetarDecoder.tryDecode(txtMetar.getText());
    if (res.isSuccess == false) {
      MessageBox.show("Failed to decode METAR to weather. Reason: " + res.exceptionOrNull.getMessage() + ".", "Error...");
      return false;
    }
    return true;
  }

  @Override
  protected boolean isValidated() {
    if (checkTimeSanity() == false) {
      return false;
    }
    if (checkMetarSanity() == false) {
      return false;
    }

    this.settings.recent.time = txtTime.getText();
    String selIcao = (String) cmbAirports.getSelectedItem();
    selIcao = selIcao.substring(0, 4);
    this.settings.recent.icao = selIcao;
    this.settings.weather.useOnline = rdbWeatherFromWeb.isSelected();
    this.settings.weather.userChanges = cmbWeatherUpdate.getSelectedIndex();
    this.settings.weather.metar = txtMetar.getText();

    return true;
  }

  private void txtTimeKeyReleased(java.awt.event.KeyEvent evt) {
  }

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {
    super.closeDialogIfValid();
  }

  private void btnSetCurrentTimeActionPerformed(java.awt.event.ActionEvent evt) {
    Date d = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    String tmp = sdf.format(d);
    txtTime.setText(tmp);
  }
}

class PresetMetar {
  public String key;
  public String metar;

  public PresetMetar(String key, String metar) {
    this.key = key;
    this.metar = metar;
  }

  @Override
  public String toString() {
    return key;
  }


}
