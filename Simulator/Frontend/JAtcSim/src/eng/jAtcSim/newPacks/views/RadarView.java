package eng.jAtcSim.newPacks.views;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.SwingRadar.SwingCanvas;
import eng.jAtcSim.abstractRadar.Radar;
import eng.jAtcSim.abstractRadar.RadarViewPort;
import eng.jAtcSim.abstractRadar.settings.RadarBehaviorSettings;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.frmPacks.shared.AdjustSelectionPanelWrapper;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.InitialPosition;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.newPacks.IView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

public class RadarView implements IView {
  class RoutesAdjustSelectionPanelWrapperListener implements AdjustSelectionPanelWrapper.ActionSelectionPanelWraperListener<DARoute> {

    @Override
    public Tuple<Iterable<DARoute>, Selector<DARoute, String>> doInit(ISimulation simulation) {
      Tuple<Iterable<DARoute>, Selector<DARoute, String>> ret = new Tuple<>();

      IMap<DARoute, IList<ActiveRunwayThreshold>> tmp = new EMap<>();
      for (ActiveRunwayThreshold threshold : simulation.getAirport().getAllThresholds()) {
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
      ret.setB(q -> {
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

  class ApproachesAdjustSelectionPanelWrapperListener
          implements AdjustSelectionPanelWrapper.ActionSelectionPanelWraperListener<Approach> {
    @Override
    public Tuple<Iterable<Approach>, Selector<Approach, String>> doInit(ISimulation sim) {
      Tuple<Iterable<Approach>, Selector<Approach, String>> ret = new Tuple<>();
      IList<Approach> tmp = new EList<>();

      for (ActiveRunwayThreshold runwayThreshold : sim.getRunwayConfigurationInUse().getArrivals()
              .select(q -> q.getThreshold())) {
        tmp.addMany(runwayThreshold.getApproaches());
      }

      ret.setA(tmp);
      ret.setB(q -> q.getParent().getName() + " " + q.getType().toString());

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

  private JPanel parent;
  private Area area;
  private AtcId userAtcId;
  private final RadarBehaviorSettings behaviorSettings = new RadarBehaviorSettings();
  private final IList<ButtonBinding> bndgs = new EList<>();
  private RadarDisplaySettings displaySettings;
  private final JButtonExtender extBtn = new JButtonExtender(
          new Color(0, 0, 0),
          new Color(150, 150, 150),
          new Color(0, 0, 0),
          new Color(0, 255, 0)
  );
  private InitialPosition initialPosition;
  private Radar radar;
  private ISimulation sim;
  private final IMap<Integer, RadarViewPort> storedRadarPositions = new EMap<>();
  private RadarStyleSettings radarStyleSettings;
  private AdjustSelectionPanelWrapper<Approach> wrpApproaches;
  private AdjustSelectionPanelWrapper<DARoute> wrpRoutes;
  private DynamicPlaneFormatter dynamicPlaneFormatter;

  public RadarBehaviorSettings getBehaviorSettings() {
    return behaviorSettings;
  }

  public Radar getRadar() {
    return this.radar;
  }

  public IMap<Integer, RadarViewPort> getRadarStoredPositions() {
    IMap<Integer, RadarViewPort> ret = new EMap<>(this.storedRadarPositions);
    return ret;
  }

  public void setRadarStoredPositions(IMap<Integer, RadarViewPort> positions) {
    //FIXME fix this
    //this.storedRadarPositions.setMany(positions);
  }

  @Override
  public void init(JPanel panel, ViewInitInfo initInfo) {
    this.parent = panel;

    this.sim = initInfo.getSimulation();
    this.area = initInfo.getSimulation().getArea();
    this.userAtcId = initInfo.getUserAtcId();
    this.initialPosition = initInfo.getAirport().getInitialPosition();
    this.radarStyleSettings = initInfo.getSettings().getRadarStyleSettings();
    this.displaySettings = initInfo.getSettings().getRadarDisplaySettings();
    this.dynamicPlaneFormatter = initInfo.getDynamicAirplaneSpeechFormatter();

    this.parent.setLayout(new BorderLayout());

    JPanel pnlTop = this.buildTopPanel();
    JPanel pnlContent = this.buildRadarPanel();

    this.parent.add(pnlContent, BorderLayout.CENTER);
    this.parent.add(pnlTop, BorderLayout.PAGE_START);

    // TODO solve somehow key presses
//    this.parent.addFocusListener(new FocusAdapter() {
//      @Override
//      public void focusGained(FocusEvent e) {
//        SwingRadarPanel.this.commandInputTextFieldExtender.getControl().requestFocus();
//      }
//    });
  }

  private Tuple<JPanel, JButton[]> buildButtonBlock(
          String btnLabel, Object targetObject, String propertyName) {

    JButton btn = new JButton(btnLabel);
    ButtonBinding bb = new ButtonBinding(targetObject, propertyName, btn);
    this.bndgs.add(bb);

    JPanel ret = eng.eSystem.swing.LayoutManager.createGridPanel(1, 1, 0, btn);
    return new Tuple<>(ret, new JButton[]{btn});
  }

  private Tuple<JPanel, JButton[]> buildButtonBlock(
          String btnLabel, Object targetObject, String propertyName,
          String btnLabelB, Object targetObjectB, String propertyNameB) {

    JButton btn = new JButton(btnLabel);
    ButtonBinding bb = new ButtonBinding(targetObject, propertyName, btn);
    this.bndgs.add(bb);

    JButton btnB = new JButton(btnLabelB);
    ButtonBinding bbB = new ButtonBinding(targetObjectB, propertyNameB, btnB);
    this.bndgs.add(bbB);

    JPanel ret = eng.eSystem.swing.LayoutManager.createGridPanel(2, 1, 0, btn, btnB);
    return new Tuple<>(ret, new JButton[]{btn, btnB});
  }

  private JPanel buildRadarPanel() {
    JPanel ret = new JPanel();
    ret.setLayout(new BorderLayout());
    SwingCanvas canvas = new SwingCanvas();
    this.radar = new Radar(
            canvas,
            this.initialPosition,
            this.sim, this.area, this.userAtcId,
            this.radarStyleSettings,
            this.displaySettings,
            this.behaviorSettings,
            this.dynamicPlaneFormatter);

    this.radar.start(1, 3);

    ret.add(canvas.getGuiControl());
    // TODO this redirects text events to jtextfield. However not working now.
//    canvas.getGuiControl().addKeyListener(new MyKeyListener(this.txtInput));
    return ret;
  }


  private JPanel buildTopPanel() {
    JPanel ret = new JPanel();
    ret.setBackground(Color.BLACK);
    eng.eSystem.swing.LayoutManager.fillFlowPanel(ret, eng.eSystem.swing.LayoutManager.eVerticalAlign.middle, 4);

    ButtonBinding.init(this.extBtn);

    Tuple<JPanel, JButton[]> pnlx;
    pnlx = this.buildButtonBlock(
            "Cntr",
            this.displaySettings, "CountryBorderVisible");
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "CTR", this.displaySettings, "CtrBorderVisible",
            "TMA", this.displaySettings, "TmaBorderVisible"
    );
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "MRVA", this.displaySettings, "MrvaBorderVisible",
            "MRVA(lbl)", this.displaySettings, "MrvaBorderAltitudeVisible"
    );
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "PROH", this.displaySettings, "RestrictedBorderVisible",
            "PROH(lbl)", this.displaySettings, "RestrictedBorderAltitudeVisible"
    );
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "VOR", this.displaySettings, "VorVisible",
            "NDB", this.displaySettings, "NdbVisible"
    );
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "FIX",
            this.displaySettings, "FixVisible");
    ret.add(pnlx.getA());
    pnlx = this.buildButtonBlock(
            "FIX-R", this.displaySettings, "FixRouteVisible",
            "FIX-M", this.displaySettings, "FixMinorVisible"
    );
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "AIP",
            this.displaySettings, "AirportVisible");
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "SID", this.displaySettings, "SidVisible",
            "STAR", this.displaySettings, "StarVisible"
    );
    this.wrpRoutes =
            new AdjustSelectionPanelWrapper<>(new RoutesAdjustSelectionPanelWrapperListener(), this.sim, pnlx.getB());
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "Apps",
            this.displaySettings, "ApproachesVisible");
    this.wrpApproaches =
            new AdjustSelectionPanelWrapper<>(new ApproachesAdjustSelectionPanelWrapperListener(), this.sim, pnlx.getB());
    ret.add(pnlx.getA());

    pnlx = this.buildButtonBlock(
            "P(rngs)",
            this.displaySettings, "RingsVisible");
    ret.add(pnlx.getA());
    pnlx = this.buildButtonBlock(
            "P(hdg)", this.displaySettings, "PlaneHeadingLineVisible",
            "P(hist)", this.displaySettings, "PlaneHistoryVisible"
    );
    ret.add(pnlx.getA());

    {
      JTextField txtA = new JTextField("0");
      JTextField txtB = new JTextField("380");
      JPanel pnl = LayoutManager.createGridPanel(2, 1, 0, txtA, txtB);
      ret.add(pnl);
      JButton btn = new JButton("(set)");
      this.extBtn.set(btn, false);
      ret.add(btn);
    }

    return ret;
  }

  private void recallRadarPosition(int index) {
    RadarViewPort rp = storedRadarPositions.tryGet(index);
    if (rp != null) {
      radar.setViewPort(rp);
    }
  }

  private void storeRadarPosition(int index) {
    RadarViewPort rp = this.radar.getViewPort();
    this.storedRadarPositions.set(index, rp);
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
      btn.setBackground(this.backOn);
      btn.setForeground(this.foreOn);
    } else {
      btn.setBackground(this.backOff);
      btn.setForeground(this.foreOff);
    }
  }
}

class ButtonBinding {
  private static JButtonExtender ext;

  public static void init(JButtonExtender ext) {
    ButtonBinding.ext = ext;
  }

  private final JButton btn;
  private final String propertyName;
  private final Object target;

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
      mi = cls.getMethod("is" + propertyName);
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
      mi = cls.getMethod("set" + propertyName, boolean.class);
    } catch (NoSuchMethodException e) {
      throw new EApplicationException("Unable to find property set" + propertyName + " over " + target + ".", e);
    }
    try {
      mi.invoke(target, val);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new EApplicationException("Unable to write property set" + propertyName + " over " + target + ".", e);
    }
  }
}

class RouteComparator implements Comparator<DARoute> {

  private final IMap<DARoute, IList<ActiveRunwayThreshold>> map;

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