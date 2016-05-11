/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import eng.eIni.IniFile;
import eng.eSystem.Exceptions;
import eng.eSystem.dateTime.DateTime;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import jatcsimxml.serialization.Serializer;
import java.awt.Component;
import java.awt.Font;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Marek
 */
public class FrmStartup extends javax.swing.JFrame {

  public FrmStartup() {
    initComponents();
    eComponentInit();
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new javax.swing.JLabel();
    txtAreaXml = new javax.swing.JTextField();
    btnAreaXml = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    txtTypesXml = new javax.swing.JTextField();
    btnTypesXml = new javax.swing.JButton();
    jLabel7 = new javax.swing.JLabel();
    btnRadars = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    txtError = new javax.swing.JTextArea();

    setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

    jLabel1.setText("Area XML file:");

    txtAreaXml.setText("--");

    btnAreaXml.setText("(browse)");

    jLabel2.setText("Plane types XML file:");

    txtTypesXml.setText("--");

    btnTypesXml.setText("(browse)");

    jLabel7.setText("Radars:");

    btnRadars.setText("(select radars)");

    txtError.setColumns(20);
    txtError.setForeground(new java.awt.Color(255, 0, 0));
    txtError.setLineWrap(true);
    txtError.setRows(5);
    jScrollPane1.setViewportView(txtError);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addGap(18, 18, 18)
            .addComponent(txtAreaXml)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnAreaXml))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addGap(18, 18, 18)
            .addComponent(txtTypesXml, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnTypesXml))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel7)
            .addGap(81, 81, 81)
            .addComponent(btnRadars)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(txtAreaXml, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnAreaXml))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(txtTypesXml, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnTypesXml))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
        .addGap(117, 117, 117)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel7)
          .addComponent(btnRadars))
        .addGap(46, 46, 46))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private static java.awt.Color bad = new java.awt.Color(255, 200, 200);
  private static java.awt.Color good = java.awt.Color.WHITE;

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnAreaXml;
  private javax.swing.JButton btnRadars;
  private javax.swing.JButton btnTypesXml;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTextField txtAreaXml;
  private javax.swing.JTextArea txtError;
  private javax.swing.JTextField txtTypesXml;
  // End of variables declaration//GEN-END:variables

  XmlFileSelectorExtender fsAreaFile;
  XmlFileSelectorExtender fsTypesFile;

  private void eComponentInit() {

    setFontAll(this.getComponents());
  }

  private static final Font f = new Font("Verdana", 0, 12);

  private void setFontAll(Component[] components) {

    for (Component c : components) {
      c.setFont(f);
      if (c instanceof java.awt.Container) {
        setFontAll(((java.awt.Container) c).getComponents());
      }
    }
  }

  public boolean isDataValid() {
    return false;
  }

  public void eInit() {
    String iniFileName =  jatcsim.JAtcSim.resFolder.toString() + "\\settings\\config.ini";

    IniFile inf = IniFile.tryLoad(iniFileName);

    fsAreaFile.setFile(
      inf.getValue("Xml", "areaXmlFile"));
    fsTypesFile.setFile(
      inf.getValue("Xml", "planesXmlFile"));
    
  }

  class FsTypesFileChangedHandler extends XmlFileSelectorFileChangedHandler {

    @Override
    public void fileChanged(String newFileName) {
      AirplaneTypes types = new AirplaneTypes();
      Serializer ser = new Serializer();
      try {
        ser.fillList(
          newFileName,
          types);
        txtTypesXml.setForeground(java.awt.Color.BLACK);
      } catch (Exception ex) {
        txtError.setText("Error loading types! " + Exceptions.toString(ex));
        txtTypesXml.setForeground(java.awt.Color.RED);
      }
    }
  }
}
