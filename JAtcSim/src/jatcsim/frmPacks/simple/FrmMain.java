/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.frmPacks.simple;

import jatcsimdraw.global.events.WithCoordinateEvent;
import jatcsimdraw.mainRadar.BasicRadar;
import jatcsimdraw.mainRadar.canvases.EJComponent;
import jatcsimdraw.mainRadar.canvases.EJComponentCanvas;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.events.EventListener;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Marek
 */
public class FrmMain extends javax.swing.JFrame {

  private javax.swing.JTextField jTxtInput;
  private JPanel pnlContent;
  private Simulation sim;
  private CommandJTextWraper wrp;
  private EJComponent radarComponent;
  private int refreshRate;
  private int refreshRateCounter = 0;

  public FrmMain() {
    initComponents();
  }

  private void initComponents() {

    // top panel
    JPanel pnlTop = new JPanel();
    pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
    //JButton btn = new JButton("test");
    //btn.addActionListener(this::btn_click);
    //pnlTop.add(btn);

    // bottom panel
    JPanel pnlBottom = new JPanel();
    pnlBottom.setLayout(new BorderLayout());
    jTxtInput = new javax.swing.JTextField();
    Font font = new Font("Courier New", Font.PLAIN, jTxtInput.getFont().getSize());
    jTxtInput.setFont(font);
    jTxtInput.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        jTxtInputKeyPressed(evt);
      }
    });
    pnlBottom.add(jTxtInput);

    // content (radar) panel
    pnlContent = new JPanel();
    pnlContent.setLayout(new BorderLayout());
    pnlContent.setBackground(Color.white);
    Dimension prefferedSize = new Dimension(1032,607);
    pnlContent.setPreferredSize(prefferedSize);


    // content pane
    BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    this.getContentPane().add(pnlTop, BorderLayout.PAGE_START);
    this.getContentPane().add(pnlBottom, BorderLayout.PAGE_END);
    this.getContentPane().add(pnlContent, BorderLayout.CENTER);


    this.pack();

    addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        formFocusGained(evt);
      }
    });
  }

  private void btn_click(ActionEvent actionEvent) {
    System.out.println(this.radarComponent.getSize());
    this.radarComponent.repaint();
  }

  private void jTxtInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtInputKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
      String msg = jTxtInput.getText();
      boolean accepted = sendMessage(msg);
      if (accepted) {
        jTxtInput.setText("");
      }
    }
  }

  private void formFocusGained(java.awt.event.FocusEvent evt) {
    this.jTxtInput.requestFocus();
  }

  void init(final Simulation sim, final Area area, Settings displaySettings) {

    this.sim = sim;
    this.refreshRate = displaySettings.getRefreshRate();
    this.refreshRateCounter = 0;

    Airport aip = sim.getActiveAirport();

    // generování hlavního radaru
    EJComponentCanvas canvas = new EJComponentCanvas();
    BasicRadar r = new BasicRadar(canvas, aip.getRadarRange(), sim, area, displaySettings);
    final EJComponent comp = canvas.getEJComponent();
    this.radarComponent = canvas.getEJComponent();

    // předávání kláves do textového pole z radaru
    this.radarComponent.addKeyListener(new MyKeyListener(this.jTxtInput));
    // zabalení chování textového pole s příkazy
    wrp = new CommandJTextWraper(jTxtInput);

    // otevření hlavního formuláře
    this.pnlContent.add(this.radarComponent);
    this.jTxtInput.requestFocus();
    this.setVisible(true);

    // mouse coord on title
    /*
    final FrmMain f = this;
    r.onMouseMove().addListener(new EventListener<BasicRadar, WithCoordinateEvent>() {

      @Override
      public void raise(BasicRadar parent, WithCoordinateEvent e) {
        f.setTitle(e.coordinate.toString());
      }
    });
    */
  }

  private boolean sendMessage(String msg) {
    boolean ret;
    UserAtc app = sim.getAppAtc();
    try {
      if (msg.startsWith("+")) {
        // msg for CTR
        msg = msg.substring(1);
        app.sendToAtc(Atc.eType.ctr, msg);
        ret = true;

      } else if (msg.startsWith("-")) {
        // msg for TWR
        msg = msg.substring(1);
        app.sendToAtc(Atc.eType.twr, msg);
        ret = true;

      } else if (msg.startsWith("?")) {
        // system
        msg = msg.substring(1);
        app.sendSystem(msg);
        ret = true;

      } else {
        // plane fromAtc
        String[] spl = splitToCallsignAndMessages(msg);
        app.sendToPlane(spl[0], spl[1]);
        ret = true;
      }
    } catch (Throwable t) {
      throw new ERuntimeException("Message invocation failed for speech: " + msg, t);
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

  void elapseSecond() {
    this.refreshRateCounter++;
    if (this.refreshRateCounter >= this.refreshRate) {
      this.refreshRateCounter = 0;
      radarComponent.repaint();
    }
  }
}

class MyKeyListener implements KeyListener {

  private final JTextField cmdTextField;

  public MyKeyListener(JTextField cmdTextField) {
    this.cmdTextField = cmdTextField;
  }

  @Override
  public void keyTyped(KeyEvent e) {
    this.cmdTextField.dispatchEvent(new KeyEvent(cmdTextField,
        KeyEvent.KEY_TYPED, System.currentTimeMillis(),
        e.getModifiers(), KeyEvent.VK_UNDEFINED, e.getKeyChar()));
    this.cmdTextField.requestFocus();
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }

}

class CommandJTextWraper {

  private final JTextField parent;
  private String lastMessage = "";

  public CommandJTextWraper(JTextField parentJTextField) {
    parent = parentJTextField;

    parent.addKeyListener(new KeyListener() {

      private void setText(String text) {
        parent.setText(text);
      }      @Override
      public void keyTyped(KeyEvent e) {

      }

      private void addText(String text) {
        parent.setText(parent.getText() + text);
      }      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
          case java.awt.event.KeyEvent.VK_ESCAPE:
            setText("");
            break;
          case java.awt.event.KeyEvent.VK_LEFT:
            addText(" TL ");
            break;
          case java.awt.event.KeyEvent.VK_RIGHT:
            addText(" TR ");
            break;
          case java.awt.event.KeyEvent.VK_UP:
            addText(" CM ");
            break;
          case java.awt.event.KeyEvent.VK_DOWN:
            addText(" DM ");
            break;
          case java.awt.event.KeyEvent.VK_ENTER:
            lastMessage = parent.getText();
            break;
          case java.awt.event.KeyEvent.VK_PAGE_UP:
            setText(lastMessage);
            break;
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
      }





    });
  }

}