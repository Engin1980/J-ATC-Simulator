/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.frmPacks.mdi;

import eng.jAtcSim.settings.AppSettings;
import eng.jAtcSim.frmPacks.shared.FlightListPanel;
import eng.jAtcSim.newLib.gameSim.ISimulation;

import java.awt.*;

/**
 * @author Marek
 */
public class FrmFlightList extends javax.swing.JFrame {

  FlightListPanel flPanel;

  public FrmFlightList() {
    initComponents();
  }

  public void init(ISimulation sim, AppSettings appSettings) {
    flPanel = new FlightListPanel();
    flPanel.init(sim, appSettings);

    this.add(flPanel);
  }

  private void initComponents() {

    this.setLayout(new BorderLayout());
    this.setPreferredSize(new Dimension(200, 800));

    pack();
  }
}

