package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.SwingRadar.SwingCanvas;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.UserAtc;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.InitialPosition;
import eng.jAtcSim.radarBase.*;
import eng.jAtcSim.shared.LayoutManager;
import sun.management.MethodInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SwingRadarPanel extends JPanel {
  private Radar radar;
  private CommandJTextWraper wrp;
  private Simulation sim;
  private Area area;
  private RadarStyleSettings styleSettings;
  private RadarDisplaySettings displaySettings;
  private RadarBehaviorSettings behaviorSettings;
  private InitialPosition initialPosition;
  private JButtonExtender extBtn = new JButtonExtender(
      new Color(0, 0, 0),
      new Color(150, 150, 150),
      new Color(0, 0, 0),
      new Color(0, 255, 0)
  );
  private IMap<Integer, RadarViewPort> storedRadarPositions = new EMap<>();
  private IList<ButtonBinding> bndgs = new EList();

  public void init(InitialPosition initialPosition,
                   Simulation sim, Area area,
                   RadarStyleSettings styleSett,
                   RadarDisplaySettings displaySett,
                   RadarBehaviorSettings behSett) {
    this.sim = sim;
    this.area = area;
    this.initialPosition = initialPosition;
    this.styleSettings = styleSett;
    this.displaySettings = displaySett;
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

    this.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        setFocus(null);
      }
    });
  }

  public Radar getRadar() {
    return radar;
  }

  public void addCommandTextToLine(String text) {
    wrp.appendText(text, true);
    wrp.focus();
  }

  public void sendCommand() {
    wrp.send();
  }

  public void eraseCommand() {
    wrp.erase();
  }

  public void setFocus(Character keyChar) {
    if (wrp != null) {
      wrp.focus();
      if (keyChar != null)
        wrp.appendText(keyChar.toString(), false);
    }
  }

  public IMap<Integer, RadarViewPort> getRadarStoredPositions() {
    IMap<Integer, RadarViewPort> ret = new EMap<>(this.storedRadarPositions);
    return ret;
  }

  public void setRadarStoredPositions(IMap<Integer, RadarViewPort> positions) {
    this.storedRadarPositions.set(positions);
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
        this.styleSettings,
        this.displaySettings,
        this.behaviorSettings);

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
    ButtonBinding bb;

    ButtonBinding.init(this.extBtn);

    btn = new JButton("Cntr");
    bb = new ButtonBinding(this.displaySettings, "CountryBorderVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("CTR");
    bb = new ButtonBinding(this.displaySettings, "CtrBorderVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("TMA");
    bb = new ButtonBinding(this.displaySettings, "TmaBorderVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("MRVA");
    bb = new ButtonBinding(this.displaySettings, "MrvaBorderVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("MRVA(lbl)");
    bb = new ButtonBinding(this.displaySettings, "MrvaBorderAltitudeVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("VOR");
    bb = new ButtonBinding(this.displaySettings, "VorVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("NDB");
    bb = new ButtonBinding(this.displaySettings, "NdbVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("FIX");
    bb = new ButtonBinding(this.displaySettings, "FixVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("FIX-R");
    bb = new ButtonBinding(this.displaySettings, "FixRouteVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("FIX-M");
    bb = new ButtonBinding(this.displaySettings, "FixMinorVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("AIP");
    bb = new ButtonBinding(this.displaySettings, "AirportVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("SID");
    bb = new ButtonBinding(this.displaySettings, "SidVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);
    btn = new JButton("STAR");
    bb = new ButtonBinding(this.displaySettings, "StarVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("P(rngs)");
    bb = new ButtonBinding(this.displaySettings, "RingsVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("P(hdg)");
    bb = new ButtonBinding(this.displaySettings, "PlaneHeadingLineVisible", btn);
    this.bndgs.add(bb);
    ret.add(btn);

    btn = new JButton("P(hist)");
    bb = new ButtonBinding(this.displaySettings, "PlaneHistoryVisible", btn);
    this.bndgs.add(bb);
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
    boolean cur = radar.getDisplaySettings().isTmaBorderVisible();
    cur = !cur;
    radar.getDisplaySettings().setTmaBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnMrvaBorder_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isMrvaBorderVisible();
    cur = !cur;
    radar.getDisplaySettings().setMrvaBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnMrvaLblBorder_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isMrvaBorderAltitudeVisible();
    cur = !cur;
    radar.getDisplaySettings().setMrvaBorderAltitudeVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnCountryBorder_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isCountryBorderVisible();
    cur = !cur;
    radar.getDisplaySettings().setCountryBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnCtrBorder_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isCtrBorderVisible();
    cur = !cur;
    radar.getDisplaySettings().setCtrBorderVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnVor_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isVorVisible();
    cur = !cur;
    radar.getDisplaySettings().setVorVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnNdb_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isNdbVisible();
    cur = !cur;
    radar.getDisplaySettings().setNdbVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnAirport_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isAirportVisible();
    cur = !cur;
    radar.getDisplaySettings().setAirportVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnSid_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isSidVisible();
    cur = !cur;
    radar.getDisplaySettings().setSidVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnStar_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isStarVisible();
    cur = !cur;
    radar.getDisplaySettings().setStarVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnFix_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isFixVisible();
    cur = !cur;
    radar.getDisplaySettings().setFixVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnFixRoute_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isFixRouteVisible();
    cur = !cur;
    radar.getDisplaySettings().setFixRouteVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnFixMinor_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isFixMinorVisible();
    cur = !cur;
    radar.getDisplaySettings().setFixMinorVisible(cur);
    extBtn.set((JButton) e.getSource(), cur);
    radar.redraw(true);
  }

  private void btnRings_click(ActionEvent e) {
    boolean cur = radar.getDisplaySettings().isRingsVisible();
    cur = !cur;
    radar.getDisplaySettings().setRingsVisible(cur);
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
      throw new ERuntimeException("Message invocation failed for item: " + msg, t);
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
            if (isCtr) appendText("TL", true);
            break;
          case java.awt.event.KeyEvent.VK_RIGHT:
            if (isCtr) appendText("TR", true);
            break;
          case java.awt.event.KeyEvent.VK_UP:
            if (isCtr) appendText("CM", true);
            break;
          case java.awt.event.KeyEvent.VK_DOWN:
            if (isCtr) appendText("DM", true);
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

  public void appendText(String text, boolean separate) {
    String tmp;
    if (separate)
      tmp = parent.getText() + " " + text + " ";
    else
      tmp = parent.getText() + text;
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

class ButtonBinding {
  private final Object target;
  private final String propertyName;
  private final JButton btn;
  private final EventAnonymousSimple onClicked = new EventAnonymousSimple();
  private static JButtonExtender ext;

  public static void init(JButtonExtender ext) {
    ButtonBinding.ext = ext;
  }

  public ButtonBinding(Object target, String propertyName, JButton btn) {
    this.target = target;
    this.propertyName = propertyName;
    this.btn = btn;

    btn.addActionListener(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        doSwitch();
      }
    });

    boolean state = getStateValue();
    ext.set(this.btn, state);
  }

  private void doSwitch() {
    boolean state = getStateValue();
    state = !state;
    setStateValue(state);
    ext.set(this.btn, state);
  }

  private boolean getStateValue() {
    Class cls = this.target.getClass();
    Method mi;
    try {
      mi = cls.getMethod("is" + propertyName, new Class[0]);
    } catch (NoSuchMethodException e) {
      throw new EApplicationException("Unable to find property is" + propertyName + " over " + target + ".", e);
    }
    boolean val;
    try {
      val = (boolean) mi.invoke(target, (Object[]) null);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new EApplicationException("Unable to read property is" + propertyName + " over " + target + ".", e);
    }
    return val;
  }

  private void setStateValue(boolean val) {
    Class cls = this.target.getClass();
    Method mi;
    try {
      mi = cls.getMethod("set" + propertyName, new Class[]{boolean.class});
    } catch (NoSuchMethodException e) {
      throw new EApplicationException("Unable to find property set" + propertyName + " over " + target + ".", e);
    }
    try {
      mi.invoke(target, new Object[]{val});
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new EApplicationException("Unable to write property set" + propertyName + " over " + target + ".", e);
    }
  }
}