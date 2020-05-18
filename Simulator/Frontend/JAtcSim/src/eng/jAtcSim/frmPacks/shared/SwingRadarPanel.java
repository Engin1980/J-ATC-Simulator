package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.SwingRadar.SwingCanvas;
import eng.jAtcSim.abstractRadar.settngs.RadarBehaviorSettings;
import eng.jAtcSim.abstractRadar.settngs.RadarDisplaySettings;
import eng.jAtcSim.abstractRadar.settngs.RadarStyleSettings;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.Simulation;
import eng.jAtcSim.newLib.area.atcs.Atc;
import eng.jAtcSim.newLib.area.atcs.UserAtc;
import eng.jAtcSim.newLib.world.*;
import eng.jAtcSim.newLib.world.approachesOld.Approach;
import eng.jAtcSim.abstractRadar.*;
import eng.eSystem.swing.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

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
  private AdjustSelectionPanelWrapper<DARoute> wrpRoutes;
  private AdjustSelectionPanelWrapper<Approach> wrpApproaches;

  class RoutesAdjustSelectionPanelWrapperListener implements AdjustSelectionPanelWrapper.ActionSelectionPanelWraperListener<DARoute>{

    @Override
    public Tuple<Iterable<DARoute>, Selector<DARoute, String>> doInit() {
      Tuple<Iterable<DARoute>, Selector<DARoute, String>> ret = new Tuple<>();

      IMap<DARoute, IList<ActiveRunwayThreshold>> tmp = new EMap<>();
      for (ActiveRunwayThreshold threshold : Acc.airport().getAllThresholds()) {
        for (DARoute route : threshold.getRoutes()) {
          IList<ActiveRunwayThreshold> lst = tmp.tryGet(route);
          if (lst == null) {
            lst = new EList<>();
            tmp.set(route, lst);
          }
          lst.add(threshold);
        }
      }
      IList<DARoute> orderedRoutes = tmp.getKeys().toList();
      orderedRoutes.sort(new RouteComparator(tmp));

      ret.setA(orderedRoutes);
      ret.setB(q->{
        EStringBuilder sb = new EStringBuilder();
        sb.append(q.getName());
        sb.append(" (");
        switch (q.getType()) {
          case sid:
            sb.append("SID:");
            break;
          case star:
            sb.append("STAR:");
            break;
          case transition:
            sb.append("TRANS:");
            break;
          default:
            throw new EEnumValueUnsupportedException(q.getType());
        }
        sb.appendItems(tmp.get(q), r -> r.getName(), ", ");
        sb.append(")");
        return sb.toString();
      });

      return ret;
    }

    @Override
    public Iterable<DARoute> doRequest() {
      return radar.getDrawnRoutes();
    }

    @Override
    public void doResponse(Iterable<DARoute> routes) {
      radar.setDrawnRoutes(routes);
      radar.redraw(true);
    }
  }

  class ApproachesAdjustSelectionPanelWrapperListener implements AdjustSelectionPanelWrapper.ActionSelectionPanelWraperListener<Approach>{
    @Override
    public Tuple<Iterable<Approach>, Selector<Approach, String>> doInit() {
      Tuple<Iterable<Approach>, Selector<Approach, String>> ret = new Tuple<>();
      IList<Approach> tmp = new EList();

      for (ActiveRunwayThreshold runwayThreshold : Acc.atcTwr().getRunwayConfigurationInUse().getArrivals().select(q -> q.getThreshold())) {
        tmp.add(runwayThreshold.getApproaches());
      }

      ret.setA(tmp);
      ret.setB(q->q.getParent().getName()+ " " + q.getTypeString());

      return ret;
    }

    @Override
    public Iterable<Approach> doRequest() {
      return radar.getDrawnApproaches();
    }

    @Override
    public void doResponse(Iterable<Approach> routes) {
      radar.setDrawnApproaches(routes);
      radar.redraw(true);
    }
  }

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
    wrp.getPauseUnpauseSimulation().add(() -> this.sim.pauseUnpauseSim());
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

  private Tuple<JPanel, JButton[]> buildButtonBlock(
      String btnLabel, Object targetObject, String propertyName){

    JButton btn = new JButton(btnLabel);
    ButtonBinding bb = new ButtonBinding(targetObject, propertyName, btn);
    this.bndgs.add(bb);

    JPanel ret = LayoutManager.createGridPanel(1, 1, 0, btn);
    return new Tuple<>(ret, new JButton[]{btn});
  }

  private Tuple<JPanel, JButton[]> buildButtonBlock(
      String btnLabel, Object targetObject, String propertyName,
      String btnLabelB, Object targetObjectB, String propertyNameB){

    JButton btn = new JButton(btnLabel);
    ButtonBinding bb = new ButtonBinding(targetObject, propertyName, btn);
    this.bndgs.add(bb);

    JButton btnB = new JButton(btnLabelB);
    ButtonBinding bbB = new ButtonBinding(targetObjectB, propertyNameB, btnB);
    this.bndgs.add(bbB);

    JPanel ret = LayoutManager.createGridPanel(2, 1, 0, btn, btnB);
    return new Tuple<>(ret, new JButton[]{btn, btnB});
  }

  private JPanel buildTopPanel() {
    JPanel ret = new JPanel();
    ret.setBackground(Color.BLACK);
    LayoutManager.fillFlowPanel(ret, LayoutManager.eVerticalAlign.middle, 4);

    ButtonBinding.init(this.extBtn);


    Tuple<JPanel, JButton[]> pnlx;
    pnlx = buildButtonBlock(
        "Cntr",
        this.displaySettings, "CountryBorderVisible");
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "CTR", this.displaySettings, "CtrBorderVisible",
        "TMA", this.displaySettings, "TmaBorderVisible"
        );
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "MRVA", this.displaySettings, "MrvaBorderVisible",
        "MRVA(lbl)", this.displaySettings, "MrvaBorderAltitudeVisible"
    );
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "PROH", this.displaySettings, "RestrictedBorderVisible",
        "PROH(lbl)", this.displaySettings, "RestrictedBorderAltitudeVisible"
    );
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "VOR", this.displaySettings, "VorVisible",
        "NDB", this.displaySettings, "NdbVisible"
    );
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "FIX",
        this.displaySettings, "FixVisible");
    ret.add(pnlx.getA());
    pnlx = buildButtonBlock(
        "FIX-R", this.displaySettings, "FixRouteVisible",
        "FIX-M", this.displaySettings, "FixMinorVisible"
    );
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "AIP",
        this.displaySettings, "AirportVisible");
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "SID", this.displaySettings, "SidVisible",
        "STAR", this.displaySettings, "StarVisible"
    );
    wrpRoutes =
        new AdjustSelectionPanelWrapper(new RoutesAdjustSelectionPanelWrapperListener(), pnlx.getB());
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "Apps",
        this.displaySettings, "ApproachesVisible");
    wrpApproaches =
        new AdjustSelectionPanelWrapper(new ApproachesAdjustSelectionPanelWrapperListener(), pnlx.getB());
    ret.add(pnlx.getA());

    pnlx = buildButtonBlock(
        "P(rngs)",
        this.displaySettings, "RingsVisible");
    ret.add(pnlx.getA());
    pnlx = buildButtonBlock(
        "P(hdg)", this.displaySettings, "PlaneHeadingLineVisible",
        "P(hist)", this.displaySettings, "PlaneHistoryVisible"
    );
    ret.add(pnlx.getA());

    {
      JTextField txt;
      JTextField txtA = new JTextField("0");
      JTextField txtB = new JTextField("380");
      JPanel pnl = LayoutManager.createGridPanel(2, 1, 0, txtA, txtB);
      ret.add(pnl);
      JButton btn = new JButton("(set)");
      extBtn.set(btn, false);
      ret.add(btn);
    }

    return ret;
  }

  private boolean sendMessage(String msg) {
    msg = normalizeMsg(msg);
    boolean ret;
    UserAtc app = sim.getAppAtc();
    try {
      if (msg.startsWith("+")) {
        // msg for ctr
        msg = msg.substring(1);
        app.sendPlaneSwitchMessageToAtc(Atc.eType.ctr, msg);
        ret = true;

      } else if (msg.startsWith("-")) {
        // msg for TWR
        msg = msg.substring(1);
        app.sendPlaneSwitchMessageToAtc(Atc.eType.twr, msg);
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
        app.sendToPlane(msg);
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
}

class CommandJTextWraper {

  private final JTextField parent;
  private boolean isCtr = false;
  private EventAnonymousSimple sendEvent = new EventAnonymousSimple();
  private EventAnonymous<Integer> storeRadarPosition = new EventAnonymous<>();
  private EventAnonymous<Integer> recallRadarPosition = new EventAnonymous<>();
  private EventAnonymousSimple pauseUnpauseSimulation = new EventAnonymousSimple();

  public EventAnonymousSimple getPauseUnpauseSimulation() {
    return pauseUnpauseSimulation;
  }

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
          case KeyEvent.VK_F1:
            pauseUnpauseSimulation.raise();
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
  private static JButtonExtender ext;
  private final Object target;
  private final String propertyName;
  private final JButton btn;
  private final EventAnonymousSimple onClicked = new EventAnonymousSimple();

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

class RouteComparator implements Comparator<DARoute> {

  private IMap<DARoute, IList<ActiveRunwayThreshold>> map;

  public RouteComparator(IMap<DARoute, IList<ActiveRunwayThreshold>> map) {
    this.map = map;
  }

  @Override
  public int compare(DARoute a, DARoute b) {
    int ret;

    String sa;
    String sb;
    IList<ActiveRunwayThreshold> tmp;
    tmp = map.get(a);
    sa = tmp.isEmpty() ? "" : tmp.getFirst().getName();
    tmp = map.get(b);
    sb = tmp.isEmpty() ? "" : tmp.getFirst().getName();
    ret = sa.compareTo(sb);
    if (ret == 0) {
      ret = a.getType().compareTo(b.getType());
      if (ret == 0)
        ret = a.getName().compareTo(b.getName());
    }
    return ret;
  }
}
