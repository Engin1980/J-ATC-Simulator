/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.frmPacks.simple;

import eng.jAtcSim.SwingRadar.SwingCanvas;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.UserAtc;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.radarBase.Radar;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Marek
 */
public class FrmMain extends javax.swing.JFrame {

  private javax.swing.JTextField jTxtInput;
  private JPanel pnlContent;
  private Pack parent;
  private CommandJTextWraper wrp;
  private int refreshRate;
  private int refreshRateCounter = 0;
  private Radar radar;
  private JPanel pnlTop;

  public FrmMain() {
    initComponents();
  }

  private void initComponents() {

    // top panel
    pnlTop = new JPanel();
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
    Dimension prefferedSize = new Dimension(1032, 607);
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

  void init(Pack pack) {

    this.parent = pack;
    this.refreshRate = parent.getDisplaySettings().refreshRate;
    this.refreshRateCounter = 0;

    // behavior settings for this radar
    BehaviorSettings behSett = new BehaviorSettings(true, new LongFormatter(), 10);

    // generování hlavního radaru
    SwingCanvas canvas = new SwingCanvas();
    this.radar = new Radar(
        canvas,
        this.parent.getSim().getActiveAirport().getRadarRange(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getDisplaySettings(), behSett
    );

    this.pnlContent.add(canvas.getGuiControl());
    canvas.getGuiControl().addKeyListener(new MyKeyListener(this.jTxtInput));

    // zabalení chování textového pole s příkazy
    wrp = new CommandJTextWraper(jTxtInput);

    this.jTxtInput.requestFocus();

    // create radar panel
    //
    createRadarPanel();
  }

  private void createRadarPanel( ) {
    JLabel lbl = new JLabel();
    lbl.setText("Demo text");

    JTextField txt = new JTextField();
    txt.setText("???");

    JButton btn = new JButton();
    btn.setText("Set");

    setDarkStyle(pnlTop, lbl, txt, btn);
    this.setBackground(Color.black);
    this.setForeground(Color.yellow);



    pnlTop.add(
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.top, 25, lbl, txt, btn)
    );

    java.awt.Container ct = lbl;
    while (ct != null){
      setDarkStyle(ct);
      ct = ct.getParent();
    }
  }

  private void setDarkStyle(java.awt.Container ... components) {
    for (java.awt.Container component : components) {
      component.setBackground(Color.black);
      component.setForeground(Color.yellow);
    }
  }

  private boolean sendMessage(String msg) {
    boolean ret;
    UserAtc app = this.parent.getSim().getAppAtc();
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
      } else if (msg.startsWith("!")) {
        // application
        processApplicationMessage(msg);
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

  private void processApplicationMessage(String msg) {
    switch (msg) {
      case "!view":
        FrmView f = new FrmView();
        f.init(this.parent);
        break;
    }
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
      radar.redraw();
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
      }

      private void addText(String text) {
        parent.setText(parent.getText() + text);
      }

      @Override
      public void keyTyped(KeyEvent e) {

      }

      @Override
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