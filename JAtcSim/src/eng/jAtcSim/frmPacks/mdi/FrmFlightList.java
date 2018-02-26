/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.frmPacks.mdi;

import eng.jAtcSim.AppSettings;
import eng.jAtcSim.frmPacks.shared.FlightListPanel;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Marek
 */
public class FrmFlightList extends javax.swing.JFrame {

  FlightListPanel flPanel;

  public FrmFlightList() {
    initComponents();
  }

  public void init(Simulation sim, AppSettings appSettings) {
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

