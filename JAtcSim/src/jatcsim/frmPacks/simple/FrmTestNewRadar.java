package jatcsim.frmPacks.simple;

import JAtcSim.radarBase.BehaviorSettings;
import JAtcSim.radarBase.DisplaySettings;
import JAtcSim.radarBase.Radar;
import JAtcSim.SwingRadar.SwingCanvas;
import jatcsimlib.speaking.formatting.LongFormatter;
import jatcsimlib.world.Airport;

import javax.swing.*;
import java.awt.*;

public class FrmTestNewRadar extends javax.swing.JFrame {

  private Radar radar = null;

  public FrmTestNewRadar(){
    initComponents();
  }

  public void init(Pack pack){

    Pack parent = pack;

    Airport aip = pack.getSim().getActiveAirport();

    // radar control

    SwingCanvas canvas = new SwingCanvas();
    JComponent component = canvas.getGuiControl();

    BehaviorSettings behSett = new BehaviorSettings(true,
        new LongFormatter(), 10);
    DisplaySettings disSett = new DisplaySettings();

    radar = new Radar(canvas,
        aip.getRadarRange(),
        parent.getSim(),
        parent.getArea(),
        disSett,
        behSett);

    // content pane
    BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    this.getContentPane().add(component, BorderLayout.CENTER);


    this.pack();
    this.setVisible(true);
  }

  private void initComponents() {


  }


}
