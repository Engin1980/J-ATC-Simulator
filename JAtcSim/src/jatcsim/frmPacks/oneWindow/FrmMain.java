/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.frmPacks.oneWindow;

import jatcsimdraw.canvases.EJComponent;
import jatcsimdraw.canvases.EJComponentCanvas;
import jatcsimdraw.radar.BasicRadar;
import jatcsimdraw.settings.Settings;
import jatcsimdraw.shared.es.WithCoordinateEvent;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.Airplanes;
import jatcsimlib.airplanes.Squawk;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.commands.CommandList;
import jatcsimlib.events.EventListener;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Marek
 */
public class FrmMain extends javax.swing.JFrame {

  /**
   * Creates new form FrmMain
   */
  public FrmMain() {
    initComponents();
  }

  /**
   * This method is called from within the constructor to
   * initialize the form. WARNING: Do NOT modify this code.
   * The content of this method is always regenerated by the
   * Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jTxtInput = new javax.swing.JTextField();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        formFocusGained(evt);
      }
    });

    jTxtInput.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        jTxtInputKeyPressed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jTxtInput, javax.swing.GroupLayout.DEFAULT_SIZE, 1032, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGap(0, 607, Short.MAX_VALUE)
        .addComponent(jTxtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jTxtInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtInputKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
      String msg = jTxtInput.getText();
      boolean accepted = sendMessage(msg);
      if (accepted) {
        jTxtInput.setText("");
      }
    }
  }//GEN-LAST:event_jTxtInputKeyPressed

  private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    // TODO add your handling code here:
   this.jTxtInput.requestFocus();
  }//GEN-LAST:event_formFocusGained

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField jTxtInput;
  // End of variables declaration//GEN-END:variables

  private Simulation sim;

  void init(final Simulation sim, final Area area, Settings displaySettings) {

    this.sim = sim;
    Airport aip = sim.getActiveAirport();

    EJComponentCanvas canvas = new EJComponentCanvas();
    BasicRadar r = new BasicRadar(canvas, aip.getRadarRange(), sim, area, displaySettings);
    final EJComponent comp = canvas.getEJComponent();

    final FrmMain f = this;
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.addComponentListener(new ComponentListener() {

      @Override
      public void componentResized(ComponentEvent e) {
        int newHeight = jTxtInput.getY(); // f.getHeight() - 60 ; //jTxtInput.getHeight() - 80;
        comp.setSize(
            f.getWidth(),
            newHeight);
      }

      @Override
      public void componentMoved(ComponentEvent e) {
      }

      @Override
      public void componentShown(ComponentEvent e) {
      }

      @Override
      public void componentHidden(ComponentEvent e) {
      }
    });
    f.add(comp);
    f.setVisible(true);

    int delay = 500; //milliseconds
    ActionListener taskPerformer = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        sim.elapseSecond();
        comp.repaint();
      }
    };

    // mouse coord on title
    r.onMouseMove().addListener(new EventListener<BasicRadar, WithCoordinateEvent>() {

      @Override
      public void raise(BasicRadar parent, WithCoordinateEvent e) {
        f.setTitle(e.coordinate.toString());
      }
    });

    new Timer(delay, taskPerformer).start();

  }

  private boolean sendMessage(String msg) {
    boolean ret;
    UserAtc app = sim.getAppAtc();
    if (msg.startsWith("+")) {
      // msg for CTR
      msg = msg.substring(1);
      Squawk s = Squawk.tryCreate(msg);
      if (s == null) {
        app.sendError("Invalid transponder format: " + msg);
        return true;
      }
      Airplane plane = Airplanes.tryGet(sim.getPlanes(), s);
      if (plane == null) {
        app.sendError("No such plane with sqwk = " + msg);
      } else {
        app.sendCommands(Atc.eType.ctr, plane);
      }
      ret = true;
    } else if (msg.startsWith("-")) {
      // msg for TWR
      msg = msg.substring(1);
      Squawk s = Squawk.tryCreate(msg);
      if (s == null){
        app.sendError("Invalid transponder format: " + msg);
        return true;
      }
      Airplane plane = Airplanes.tryGet(sim.getPlanes(), s);
      if (plane == null) {
        app.sendError("No such plane with sqwk = " + msg);
      } else {
        app.sendCommands(Atc.eType.twr, plane);
      }
      ret = true;
    } else if (msg.startsWith("=")) {
      // system
      msg = msg.substring(1);
      app.sendSystem(msg);
      ret = true;
    } else {
      String[] spl = splitToCallsignAndMessages(msg);
      Airplane p = Airplanes.tryGetByCallsingOrNumber(sim.getPlanes(), spl[0]);
      if (p == null) {
        app.sendError("No such plane (or multiple planes) for callsign " + spl[0] + ".");
        ret = false;
      } else {
        CommandList cmdList = null;
        try {
          cmdList = CommandFormat.parseMulti(spl[1]);
          ret = true;
        } catch (Exception ex) {
          app.sendError("Command error: " + ex.getMessage());
          ret = false;
        }
        if (ret) {
          sim.getMessenger().addMessage(app, p, cmdList);
        }
      }
    }
    return ret;
  }

  private String[] splitToCallsignAndMessages(String msg) {
    String[] ret = new String[2];
    int i = msg.indexOf(" ");
    if (i == msg.length() || i < 0) {
      ret[0] = msg;
      ret[1] = "";
    } else {
      ret[0] = msg.substring(0, i);
      ret[1] = msg.substring(i + 1);
    }
    return ret;
  }
}
