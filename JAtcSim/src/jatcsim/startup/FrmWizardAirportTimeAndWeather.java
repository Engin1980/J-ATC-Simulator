/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import jatcsimxml.serialization.Serializer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Marek Vajgl
 */
public class FrmWizardAirportTimeAndWeather extends FrmWizardFrame {

  /**
   * Creates new form FrmWizardAirportAndTraffic
   */
  public FrmWizardAirportTimeAndWeather() {
    initComponents();
    initExtenders();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel4 = new javax.swing.JLabel();
    cmbAirports = new javax.swing.JComboBox();
    jLabel5 = new javax.swing.JLabel();
    txtTime = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    rdbWeatherFromWeb = new javax.swing.JRadioButton();
    rdbWeatherFromUser = new javax.swing.JRadioButton();
    txtMetar = new javax.swing.JTextField();
    btnDownloadMetar = new javax.swing.JButton();
    btnContinue = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();
    cmbWeatherUpdate = new javax.swing.JComboBox();
    btnSetCurrentTime = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    jLabel4.setText("Select airport:");

    cmbAirports.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    jLabel5.setText("Simulation start time:");

    txtTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
    txtTime.setText("8:57");
    txtTime.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        txtTimeKeyReleased(evt);
      }
      public void keyTyped(java.awt.event.KeyEvent evt) {
        txtTimeKeyTyped(evt);
      }
    });

    jLabel6.setText("Weather:");

    rdbWeatherFromWeb.setText("use real weather continously downloaded from web");
    rdbWeatherFromWeb.setEnabled(false);

    rdbWeatherFromUser.setSelected(true);
    rdbWeatherFromUser.setText("user set - insert METAR string:");

    txtMetar.setText("METAR ZZZZ 111111Z 20212KTS 9000 OVC012");

    btnDownloadMetar.setText("Download now");
    btnDownloadMetar.setEnabled(false);

    btnContinue.setText("Continue");
    btnContinue.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnContinueActionPerformed(evt);
      }
    });

    jLabel1.setText("... random weather update:");

    cmbWeatherUpdate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Slight", "Moderate", "Significant" }));

    btnSetCurrentTime.setText("Set current");
    btnSetCurrentTime.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnSetCurrentTimeActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel5)
          .addComponent(jLabel4)
          .addComponent(jLabel6))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(cmbAirports, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSetCurrentTime))
              .addComponent(rdbWeatherFromUser)
              .addComponent(rdbWeatherFromWeb))
            .addGap(0, 251, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbWeatherUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
              .addGroup(layout.createSequentialGroup()
                .addComponent(txtMetar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDownloadMetar)))))
        .addGap(10, 10, 10))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(btnContinue)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(cmbAirports, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnSetCurrentTime))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(rdbWeatherFromWeb))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(rdbWeatherFromUser)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMetar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnDownloadMetar))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(cmbWeatherUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnContinue)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void txtTimeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTimeKeyReleased

  }//GEN-LAST:event_txtTimeKeyReleased

  private void txtTimeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTimeKeyTyped

  }//GEN-LAST:event_txtTimeKeyTyped

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinueActionPerformed

    if (checkTimeSanity() == false) {
      return;
    }
    if (checkMetarSanity() == false) {
      return;
    }
    
    this.settings.setRecentTime(txtTime.getText());
    String selIcao = (String) cmbAirports.getSelectedItem();
    selIcao = selIcao.substring(0,5);
    this.settings.setRecentIcao(selIcao);
    this.settings.setWeatherOnline(rdbWeatherFromWeb.isSelected());
    this.settings.setWeatherUserChanges(cmbWeatherUpdate.getSelectedIndex());
    this.settings.setWeatherUserMetar(txtMetar.getText());

    super.dialogResult  = DialogResult.Ok;
    this.setVisible(false);
  }//GEN-LAST:event_btnContinueActionPerformed

  private void btnSetCurrentTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetCurrentTimeActionPerformed
    Date d = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    String tmp = sdf.format(d);
    txtTime.setText(tmp);
  }//GEN-LAST:event_btnSetCurrentTimeActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnContinue;
  private javax.swing.JButton btnDownloadMetar;
  private javax.swing.JButton btnSetCurrentTime;
  private javax.swing.JComboBox cmbAirports;
  private javax.swing.JComboBox cmbWeatherUpdate;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JRadioButton rdbWeatherFromUser;
  private javax.swing.JRadioButton rdbWeatherFromWeb;
  private javax.swing.JTextField txtMetar;
  private javax.swing.JTextField txtTime;
  // End of variables declaration//GEN-END:variables

  private TimeExtender timeExtender;
  
  @Override
  protected void fillBySettings() {
    fillAirportsComboBox();
    
    txtTime.setText(settings.getRecentTime());
    txtMetar.setText(settings.getWeatherUserMetar());
    
    cmbWeatherUpdate.setSelectedIndex(settings.getWeatherUserChanges());
    if (settings.isWeatherOnline())
      rdbWeatherFromWeb.setSelected(true);
    else
      rdbWeatherFromUser.setSelected(true);
  }

  private void fillAirportsComboBox() {
    Area area = Area.create();
    Serializer ser = new Serializer();
    ser.fillObject(settings.getAreaXmlFile(), area);

    int selectedIndex = -1;
    String[] data = new String[area.getAirports().size()];
    for (int i = 0; i < area.getAirports().size(); i++) {
      Airport aip = area.getAirports().get(i);
      data[i] = aip.getIcao() + " - " + aip.getName();
      
      if (aip.getIcao().equals(settings.getRecentIcao()))
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
    if (timeExtender.isValid() == false){
      MessageBox.show("Selected time value is not valid.", "Error...");
      return false;
    }
    
    return true;
  }

  private boolean checkMetarSanity() {
    //TODO finish test for metar validity
    return true;
  }
}
