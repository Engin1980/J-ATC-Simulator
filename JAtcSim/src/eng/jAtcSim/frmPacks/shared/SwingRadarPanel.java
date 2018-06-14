package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.SwingRadar.SwingCanvas;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.UserAtc;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.InitialPosition;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.radarBase.DisplaySettings;
import eng.jAtcSim.radarBase.Radar;
import eng.jAtcSim.radarBase.RadarViewPort;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SwingRadarPanel extends JPanel {
  private Radar radar;
  private CommandJTextWraper wrp;
  private Simulation sim;
  private Area area;
  private DisplaySettings displaySettings;
  private BehaviorSettings behaviorSettings;
  private InitialPosition initialPosition;
  private JButtonExtender extBtn = new JButtonExtender(
      new Color(0, 0, 0),
      new Color(150, 150, 150),
      new Color(0, 0, 0),
      new Color(0, 255, 0)
  );
  private IMap<Integer, RadarViewPort> storedRadarPositions = new EMap<>();

  public void init(InitialPosition initialPosition,
                   Simulation sim, Area area,
                   DisplaySettings dispSett, BehaviorSettings behSett) {
    this.sim = sim;
    this.area = area;
    this.initialPosition = initialPosition;
    this.displaySettings = dispSett;
    this.behaviorSettings = behSett;

    this.setLayout(new BorderLayout());

    JPanel pnlTop = buildTopPanel();
    JPanel pnlContent = buildRadarPanel();

    this.add(pnlContent, BorderLayout.CENTER);
    this.add(pnlTop, BorderLayout.PAGE_START);

    if (behaviorSettings.isPaintMessages()) {
      JPanel pnlBottom = buildTextPanel();
      this.add(pnlBottom, BorderLayout.PAGE_END);
    }
  }

  public Radar getRadar() {
    return radar;
  }

  public void addCommandTextToLine(String text) {
    wrp.appendText(text);
    wrp.focus();
  }

  public void sendCommand() {
    wrp.send();
  }

  public void eraseCommand() {
    wrp.erase();
  }

  private JPanel buildTextPanel() {

    // textove pole
    JTextField txtInput = new JTextField();
    Font font = new Font("Courier New", Font.PLAIN, txtInput.getFont().getSize());
    txtInput.setFont(font);
    JPanel ret = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle,
        3,
        txtInput);

    wrp = new CommandJTextWraper(txtInput);
    wrp.getSendEvent().add(() -> this.wrp_send());
    wrp.getRecallRadarPosition().add(pos -> this.recallRadarPosition(pos));
    wrp.getStoreRadarPosition().add(pos -> this.storeRadarPosition(pos));
    wrp.focus();

    return ret;
  }

  private void storeRadarPosition(int index) {
    RadarViewPort rp = radar.getViewPort();
    storedRadarPositions.set(index, rp);
  }

  public IMap<Integer, RadarViewPort> getRadarStoredPositions(){
    IMap<Integer, RadarViewPort> ret = new EMap<>(this.storedRadarPositions);
    return ret;
  }

  public void setRadarStoredPositions(IMap<Integer, RadarViewPort> positions){
    this.storedRadarPositions.set(positions);
  }

  private void recallRadarPosition(int index) {
    RadarViewPort rp = storedRadarPositions.tryGet(index);
    if (rp != null) {
      radar.setViewPort(rp);
    }
  }

  private void wrp_send() {
    String msg = wrp.getText();
    boolean accepted = sendMessage(msg);
    if (accepted) {
      wrp.erase();
    }
    wrp.focus();
  }

  private JPanel buildRadarPanel() {
    JPanel ret = new JPanel();
    ret.setLayout(new BorderLayout());
    SwingCanvas canvas = new SwingCanvas();
    this.radar = new Radar(
        canvas,
        this.initialPosition,
        this.sim, this.area,
        this.displaySettings, this.behaviorSettings);

    this.radar.start(1, 3);

    ret.add(canvas.getGuiControl());
    // TODO this redirects text events to jtextfield. However not working now.
//    canvas.getGuiControl().addKeyListener(new MyKeyListener(this.txtInput));
    return ret;
  }

  private JPanel buildTopPanel() {
    JPanel ret = new JPanel();
    LayoutManager.fillFlowPanel(ret, LayoutManager.eVerticalAlign.middle, 4);

    JButton btn;
    JTextField txt;

    btn = new JButton("Cntr");
    btn.addActionListener(e -> btnCountryBorder_click(e));
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("CTR");
    btn.addActionListener(e -> btnCtrBorder_click(e));
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("TMA");
    btn.addActionListener(e -> btnTmaBorder_click(e));
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("MRVA");
    btn.addActionListener(e -> btnMrvaBorder_click(e));
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("MRVA(lbl)");
    btn.addActionListener(e -> btnMrvaLblBorder_click(e));
    extBtn.set(btn, true);
    ret.add(btn);

    btn = new JButton("VOR");
    btn.addActionListener(e -> btnVor_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("NDB");
    btn.addActionListener(e -> btnNdb_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("FIX");
    btn.addActionListener(e -> btnFix_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("FIX-R");
    btn.addActionListener(e -> btnFixRoute_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("FIX-M");
    btn.addActionListener(e -> btnFixMinor_click(e));
    extBtn.set(btn, false);
    ret.add(btn);
    btn = new JButton("AIP");
    btn.addActionListener(e -> btnAirport_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("SID");
    btn.addActionListener(e -> btnSid_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("STAR");
    btn.addActionListener(e -> btnStar_click(e));
    extBtn.set(btn, true);
    ret.add(btn);
    btn = new JButton("(rngs)");
    btn.addActionListener(e -> btnRings_click(e));
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

  private void btnTmaBorder_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isTmaBorderVisible();
    cur = !cur;
    radar.getLocalSettings().setTmaBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnMrvaBorder_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isMrvaBorderVisible();
    cur = !cur;
    radar.getLocalSettings().setMrvaBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnMrvaLblBorder_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isMrvaBorderAltitudeVisible();
    cur = !cur;
    radar.getLocalSettings().setMrvaBorderAltitudeVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnCountryBorder_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isCountryBorderVisible();
    cur = !cur;
    radar.getLocalSettings().setCountryBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnCtrBorder_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isCtrBorderVisible();
    cur = !cur;
    radar.getLocalSettings().setCtrBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnVor_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isVorVisible();
    cur = !cur;
    radar.getLocalSettings().setVorVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnNdb_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isNdbVisible();
    cur = !cur;
    radar.getLocalSettings().setNdbVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnAirport_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isAirportVisible();
    cur = !cur;
    radar.getLocalSettings().setAirportVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnSid_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isSidVisible();
    cur = !cur;
    radar.getLocalSettings().setSidVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnStar_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isStarVisible();
    cur = !cur;
    radar.getLocalSettings().setStarVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnFix_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isFixVisible();
    cur = !cur;
    radar.getLocalSettings().setFixVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnFixRoute_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isFixRouteVisible();
    cur = !cur;
    radar.getLocalSettings().setFixRouteVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnFixMinor_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isFixMinorVisible();
    cur = !cur;
    radar.getLocalSettings().setFixMinorVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnRings_click(ActionEvent e) {
    boolean cur = radar.getLocalSettings().isRingsVisible();
    cur = !cur;
    radar.getLocalSettings().setRingsVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private boolean sendMessage(String msg) {
    msg = normalizeMsg(msg);
    boolean ret;
    UserAtc app = sim.getAppAtc();
    try {
      if (msg.startsWith("+")) {
        // msg for ctr
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

  private String normalizeMsg(String txt) {
    txt = txt.trim();
    StringBuilder sb = new StringBuilder(txt);
    int doubleSpaceIndex = sb.toString().indexOf("  ");
    while (doubleSpaceIndex >= 0) {
      sb.replace(doubleSpaceIndex, doubleSpaceIndex + 2, " ");
      doubleSpaceIndex = sb.toString().indexOf("  ");
    }
    return sb.toString();
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

class CommandJTextWraper {

  private final JTextField parent;
  private boolean isCtr = false;
  private EventAnonymousSimple sendEvent = new EventAnonymousSimple();
  private EventAnonymous<Integer> storeRadarPosition = new EventAnonymous<>();
  private EventAnonymous<Integer> recallRadarPosition = new EventAnonymous<>();

  public CommandJTextWraper(JTextField parentJTextField) {
    parent = parentJTextField;

    parent.addKeyListener(new KeyListener() {

      @Override
      public void keyTyped(KeyEvent e) {

      }

      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_CONTROL:
            isCtr = true;
            break;
          case java.awt.event.KeyEvent.VK_ESCAPE:
            erase();
            break;
          case java.awt.event.KeyEvent.VK_LEFT:
            if (isCtr) appendText("TL");
            break;
          case java.awt.event.KeyEvent.VK_RIGHT:
            if (isCtr) appendText("TR");
            break;
          case java.awt.event.KeyEvent.VK_UP:
            if (isCtr) appendText("CM");
            break;
          case java.awt.event.KeyEvent.VK_DOWN:
            if (isCtr) appendText("DM");
            break;
          case java.awt.event.KeyEvent.VK_ENTER:
            send();
            break;
          case KeyEvent.VK_F2:
            if (isCtr)
              storeRadarPosition.raise(2);
            else
              recallRadarPosition.raise(2);
            break;
          case KeyEvent.VK_F3:
            if (isCtr)
              storeRadarPosition.raise(3);
            else
              recallRadarPosition.raise(3);
            break;
          case KeyEvent.VK_F4:
            if (isCtr)
              storeRadarPosition.raise(4);
            else
              recallRadarPosition.raise(4);
            break;
          case KeyEvent.VK_F5:
            if (isCtr)
              storeRadarPosition.raise(5);
            else
              recallRadarPosition.raise(5);
            break;
          case KeyEvent.VK_F6:
            if (isCtr)
              storeRadarPosition.raise(6);
            else
              recallRadarPosition.raise(6);
            break;
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_CONTROL:
            isCtr = false;
            break;
        }
      }


    });
  }

  public void focus() {
    parent.requestFocus();
  }

  public void appendText(String text) {
    String tmp = parent.getText() + " " + text + " ";
    parent.setText(tmp);
  }

  public String getText() {
    return parent.getText().trim().toUpperCase();
  }

  public void send() {
    sendEvent.raise();
  }

  public void erase() {
    parent.setText("");
  }

  public EventAnonymousSimple getSendEvent() {
    return sendEvent;
  }

  public EventAnonymous<Integer> getStoreRadarPosition() {
    return storeRadarPosition;
  }

  public EventAnonymous<Integer> getRecallRadarPosition() {
    return recallRadarPosition;
  }
}

class JButtonExtender {
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

  public void set(JButton btn, boolean state) {
    if (state) {
      btn.setBackground(backOn);
      btn.setForeground(foreOn);
    } else {
      btn.setBackground(backOff);
      btn.setForeground(foreOff);
    }
  }
}

class RadarPosition {

}