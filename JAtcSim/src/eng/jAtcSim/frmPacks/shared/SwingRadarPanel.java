package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.SwingRadar.SwingCanvas;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.UserAtc;
import eng.jAtcSim.lib.coordinates.RadarRange;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.radarBase.DisplaySettings;
import eng.jAtcSim.radarBase.Radar;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import javax.tools.JavaCompiler;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SwingRadarPanel extends JPanel {
  private Radar radar;
  private javax.swing.JTextField txtInput;
  private CommandJTextWraper wrp;
  private Simulation sim;
  private Area area;
  private DisplaySettings displaySettings;
  private BehaviorSettings behaviorSettings;
  private RadarRange defaultRadarRange;
private JButtonExtender extBtn = new JButtonExtender(
    new Color(0,0,0),
    new Color(0,255,0),
    new Color(0,0,0),
    new Color(150,150,150)
);

  public void init(RadarRange radarRange,
                   Simulation sim, Area area,
                   DisplaySettings dispSett, BehaviorSettings behSett) {
    this.sim = sim;
    this.area = area;
    this.defaultRadarRange = radarRange;
    this.displaySettings = dispSett;
    this.behaviorSettings = behSett;

    this.setLayout(new BorderLayout());

    JPanel pnlTop = buildTopPanel();
    JPanel pnlContent = buildRadarPanel();
    JPanel pnlBottom = buildTextPanel();

    this.add(pnlTop, BorderLayout.PAGE_START);
    this.add(pnlBottom, BorderLayout.PAGE_END);
    this.add(pnlContent, BorderLayout.CENTER);

    // zabalení chování textového pole s příkazy
    wrp = new CommandJTextWraper(txtInput);

    this.txtInput.requestFocus();
  }

  private JPanel buildTextPanel() {

    // textove pole
    txtInput = new JTextField();
    Font font = new Font("Courier New", Font.PLAIN, txtInput.getFont().getSize());
    txtInput.setFont(font);
    txtInput.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        jTxtInputKeyPressed(evt);
      }
    });
    JPanel ret = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle,
        3,
        txtInput);

    return ret;
  }

  private JPanel buildRadarPanel() {
    JPanel ret = new JPanel();
    ret.setLayout(new BorderLayout());
    SwingCanvas canvas = new SwingCanvas();
    this.radar = new Radar(
        canvas,
        this.defaultRadarRange,
        this.sim, this.area,
        this.displaySettings, this.behaviorSettings);

    ret.add(canvas.getGuiControl());
    canvas.getGuiControl().addKeyListener(new MyKeyListener(this.txtInput));
    return ret;
  }

  private JPanel buildTopPanel() {
    JPanel ret = new JPanel();
    LayoutManager.fillFlowPanel(ret, LayoutManager.eVerticalAlign.middle  , 4);

    JButton btn;
    JTextField txt;

    btn = new JButton("C-bdr");
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("T-bdr");
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("VOR");
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("NDB");
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("I-FIX");
    extBtn.set(btn, false);
    ret.add(btn);
    btn = new JButton("R-FIX");
    extBtn.set(btn, false);
    ret.add(btn);
    btn = new JButton("FIX");
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("AIP");
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("SID");
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("STAR");
    extBtn.set(btn, true);
    ret.add(btn);

    txt = new JTextField("0");
    ret.add(txt);
    txt = new JTextField("380");
    ret.add(txt);
    btn = new JButton("(set)");
    extBtn.set(btn, false);
    ret.add(btn);

    return ret;
  }

  public Radar getRadar() {
    return radar;
  }

  private void jTxtInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtInputKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
      String msg = txtInput.getText();
      boolean accepted = sendMessage(msg);
      if (accepted) {
        txtInput.setText("");
      }
    }
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

class JButtonExtender{
  public final Color backOff;
  public final Color backOn;
  public final Color foreOff;
  public final Color foreOn;

  public JButtonExtender(Color backOff, Color foreOff, Color backOn, Color foreOn) {
    this.backOff = backOff;
    this.backOn = backOn;
    this.foreOff = foreOff;
    this.foreOn = foreOn;
  }

  public void set(JButton btn, boolean state){
    if (state) {
      btn.setBackground(backOn);
      btn.setForeground(foreOn);
    } else {
      btn.setBackground(backOff);
      btn.setForeground(foreOff);
    }
  }
}