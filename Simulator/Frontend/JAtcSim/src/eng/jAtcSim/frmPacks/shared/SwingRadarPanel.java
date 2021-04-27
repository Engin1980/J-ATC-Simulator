//package eng.jAtcSim.frmPacks.shared;
//
//import eng.eSystem.EStringBuilder;
//import eng.eSystem.Tuple;
//import eng.eSystem.collections.*;
//import eng.eSystem.exceptions.EApplicationException;
//import eng.eSystem.exceptions.UnexpectedValueException;
//import eng.eSystem.functionalInterfaces.Selector;
//import eng.eSystem.swing.LayoutManager;
//import eng.jAtcSim.SwingRadar.SwingCanvas;
//import eng.jAtcSim.abstractRadar.Radar;
//import eng.jAtcSim.abstractRadar.RadarViewPort;
//import eng.jAtcSim.abstractRadar.settings.RadarBehaviorSettings;
//import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
//import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
//import eng.jAtcSim.app.extenders.CommandInputTextFieldExtender;
//import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
//import eng.jAtcSim.newLib.area.Area;
//import eng.jAtcSim.newLib.area.InitialPosition;
//import eng.jAtcSim.newLib.area.approaches.Approach;
//import eng.jAtcSim.newLib.area.routes.DARoute;
//import eng.jAtcSim.newLib.gameSim.ISimulation;
//import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParsersSet;
//import eng.jAtcSim.newLib.shared.AtcId;
//import eng.jAtcSim.newLib.shared.Callsign;
//import eng.jAtcSim.newLib.speeches.SpeechList;
//import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
//import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
//import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
//import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.AtcParser;
//import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
//import eng.jAtcSim.newLib.textProcessing.implemented.planeParser.PlaneParser;
//import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.SystemParser;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.FocusAdapter;
//import java.awt.event.FocusEvent;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Comparator;
//import java.util.Map;
//
//public class SwingRadarPanel extends JPanel {
//
//
//  public class CommandInputWrapper {
//    public void addCommandTextToLine(char keyChar) {
//      SwingRadarPanel.this.commandInputTextFieldExtender.appendText(Character.toString(keyChar), false);
//    }
//
//    public void addCommandTextToLine(String text) {
//      SwingRadarPanel.this.commandInputTextFieldExtender.appendText(text, true);
//    }
//
//    public void eraseCommand() {
//      SwingRadarPanel.this.commandInputTextFieldExtender.erase();
//    }
//
//    public void sendCommand() {
//      SwingRadarPanel.this.commandInputTextFieldExtender.send();
//    }
//
//    public void setFocus() {
//      SwingRadarPanel.this.commandInputTextFieldExtender.focus();
//
//    }
//  }
//
//  class RoutesAdjustSelectionPanelWrapperListener implements AdjustSelectionPanelWrapper.ActionSelectionPanelWraperListener<DARoute> {
//
//    @Override
//    public Tuple<Iterable<DARoute>, Selector<DARoute, String>> doInit(ISimulation simulation) {
//      Tuple<Iterable<DARoute>, Selector<DARoute, String>> ret = new Tuple<>();
//
//      IMap<DARoute, IList<ActiveRunwayThreshold>> tmp = new EMap<>();
//      for (ActiveRunwayThreshold threshold : simulation.getAirport().getAllThresholds()) {
//        for (DARoute route : threshold.getRoutes()) {
//          IList<ActiveRunwayThreshold> lst = tmp.tryGet(route);
//          if (lst == null) {
//            lst = new EList<>();
//            tmp.set(route, lst);
//          }
//          lst.add(threshold);
//        }
//      }
//      IList<DARoute> orderedRoutes = tmp.getKeys().toList();
//      orderedRoutes.sort(new RouteComparator(tmp));
//
//      ret.setA(orderedRoutes);
//      ret.setB(q -> {
//        EStringBuilder sb = new EStringBuilder();
//        sb.append(q.getName());
//        sb.append(" (");
//        switch (q.getType()) {
//          case sid:
//            sb.append("SID:");
//            break;
//          case star:
//            sb.append("STAR:");
//            break;
//          case transition:
//            sb.append("TRANS:");
//            break;
//          default:
//            throw new UnexpectedValueException(q.getType());
//        }
//        sb.appendItems(tmp.get(q), r -> r.getName(), ", ");
//        sb.append(")");
//        return sb.toString();
//      });
//
//      return ret;
//    }
//
//    @Override
//    public Iterable<DARoute> doRequest() {
//      return radar.getDrawnRoutes();
//    }
//
//    @Override
//    public void doResponse(Iterable<DARoute> routes) {
//      radar.setDrawnRoutes(routes);
//      radar.redraw(true);
//    }
//  }
//
//  class ApproachesAdjustSelectionPanelWrapperListener
//          implements AdjustSelectionPanelWrapper.ActionSelectionPanelWraperListener<Approach> {
//    @Override
//    public Tuple<Iterable<Approach>, Selector<Approach, String>> doInit(ISimulation sim) {
//      Tuple<Iterable<Approach>, Selector<Approach, String>> ret = new Tuple<>();
//      IList<Approach> tmp = new EList<>();
//
//      for (ActiveRunwayThreshold runwayThreshold : sim.getRunwayConfigurationInUse().getArrivals()
//              .select(q -> q.getThreshold())) {
//        tmp.addMany(runwayThreshold.getApproaches());
//      }
//
//      ret.setA(tmp);
//      ret.setB(q -> q.getParent().getName() + " " + q.getType().toString());
//
//      return ret;
//    }
//
//    @Override
//    public Iterable<Approach> doRequest() {
//      return radar.getDrawnApproaches();
//    }
//
//    @Override
//    public void doResponse(Iterable<Approach> routes) {
//      radar.setDrawnApproaches(routes);
//      radar.redraw(true);
//    }
//  }
//  public final CommandInputWrapper CommandInput = new CommandInputWrapper();
//  private Area area;
//  private AtcId userAtcId;
//  private RadarBehaviorSettings behaviorSettings;
//  private final IList<ButtonBinding> bndgs = new EList();
//  private RadarDisplaySettings displaySettings;
//  private final JButtonExtender extBtn = new JButtonExtender(
//          new Color(0, 0, 0),
//          new Color(150, 150, 150),
//          new Color(0, 0, 0),
//          new Color(0, 255, 0)
//  );
//  private InitialPosition initialPosition;
//  private Radar radar;
//  private ISimulation sim;
//  private final IMap<Integer, RadarViewPort> storedRadarPositions = new EMap<>();
//  private RadarStyleSettings styleSettings;
//  private AdjustSelectionPanelWrapper<Approach> wrpApproaches;
//  private AdjustSelectionPanelWrapper<DARoute> wrpRoutes;
//  private DynamicPlaneFormatter dynamicPlaneFormatter;
//  private CommandInputTextFieldExtender commandInputTextFieldExtender;
//
//  public Radar getRadar() {
//    return this.radar;
//  }
//
//  public IMap<Integer, RadarViewPort> getRadarStoredPositions() {
//    IMap<Integer, RadarViewPort> ret = new EMap<>(this.storedRadarPositions);
//    return ret;
//  }
//
//  public void setRadarStoredPositions(IMap<Integer, RadarViewPort> positions) {
//    //FIXME fix this
//    //this.storedRadarPositions.setMany(positions);
//  }
//
//  public void init(InitialPosition initialPosition,
//                   ISimulation sim, Area area, AtcId userAtcId,
//                   RadarStyleSettings styleSett,
//                   RadarDisplaySettings displaySett,
//                   RadarBehaviorSettings behSett,
//                   DynamicPlaneFormatter dynamicPlaneFormatter) {
//    this.sim = sim;
//    this.area = area;
//    this.userAtcId = userAtcId;
//    this.initialPosition = initialPosition;
//    this.styleSettings = styleSett;
//    this.displaySettings = displaySett;
//    this.behaviorSettings = behSett;
//    this.dynamicPlaneFormatter = dynamicPlaneFormatter;
//
//    this.setLayout(new BorderLayout());
//
//    JPanel pnlTop = this.buildTopPanel();
//    JPanel pnlContent = this.buildRadarPanel();
//
//    this.add(pnlContent, BorderLayout.CENTER);
//    this.add(pnlTop, BorderLayout.PAGE_START);
//
//    if (this.behaviorSettings.isPaintMessages()) {
//      JPanel pnlBottom = this.buildTextPanel();
//      this.add(pnlBottom, BorderLayout.PAGE_END);
//    }
//
//    this.addFocusListener(new FocusAdapter() {
//      @Override
//      public void focusGained(FocusEvent e) {
//        SwingRadarPanel.this.commandInputTextFieldExtender.getControl().requestFocus();
//      }
//    });
//  }
//
//  private Tuple<JPanel, JButton[]> buildButtonBlock(
//          String btnLabel, Object targetObject, String propertyName) {
//
//    JButton btn = new JButton(btnLabel);
//    ButtonBinding bb = new ButtonBinding(targetObject, propertyName, btn);
//    this.bndgs.add(bb);
//
//    JPanel ret = LayoutManager.createGridPanel(1, 1, 0, btn);
//    return new Tuple<>(ret, new JButton[]{btn});
//  }
//
//  private Tuple<JPanel, JButton[]> buildButtonBlock(
//          String btnLabel, Object targetObject, String propertyName,
//          String btnLabelB, Object targetObjectB, String propertyNameB) {
//
//    JButton btn = new JButton(btnLabel);
//    ButtonBinding bb = new ButtonBinding(targetObject, propertyName, btn);
//    this.bndgs.add(bb);
//
//    JButton btnB = new JButton(btnLabelB);
//    ButtonBinding bbB = new ButtonBinding(targetObjectB, propertyNameB, btnB);
//    this.bndgs.add(bbB);
//
//    JPanel ret = LayoutManager.createGridPanel(2, 1, 0, btn, btnB);
//    return new Tuple<>(ret, new JButton[]{btn, btnB});
//  }
//
//  private JPanel buildRadarPanel() {
//    JPanel ret = new JPanel();
//    ret.setLayout(new BorderLayout());
//    SwingCanvas canvas = new SwingCanvas();
//    this.radar = new Radar(
//            canvas,
//            this.initialPosition,
//            this.sim, this.area, this.userAtcId,
//            this.styleSettings,
//            this.displaySettings,
//            this.behaviorSettings,
//            this.dynamicPlaneFormatter);
//
//    this.radar.start(1, 3);
//
//    ret.add(canvas.getGuiControl());
//    // TODO this redirects text events to jtextfield. However not working now.
////    canvas.getGuiControl().addKeyListener(new MyKeyListener(this.txtInput));
//    return ret;
//  }
//
//  private JPanel buildTextPanel() {
//    JTextField txtInput = new JTextField();
//    Font font = new Font("Courier New", Font.PLAIN, txtInput.getFont().getSize());
//    txtInput.setFont(font);
//    JPanel ret = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle,
//            3,
//            txtInput);
//
//    this.commandInputTextFieldExtender = new CommandInputTextFieldExtender(txtInput,
//            this.buildParsers(),
//            () -> this.sim.getAtcs(),
//            () -> this.sim.getPlanesToDisplay().select(q -> q.callsign()));
//    this.commandInputTextFieldExtender.onAtcCommand.add(this::sendAtcCommand);
//    this.commandInputTextFieldExtender.onSystemCommand.add(this::sendSystemCommand);
//    this.commandInputTextFieldExtender.onPlaneCommand.add(this::sendPlaneCommand);
//    this.commandInputTextFieldExtender.onSpecialCommand.add(this::processSpecialCommand);
//    this.commandInputTextFieldExtender.onError.add(this::processError);
//
//    return ret;
//  }
//
//  private void processError(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.ErrorEventArgs e) {
//    String s = convertErrorToString(e.error, e.arguments);
//    this.radar.showMessageOnScreen(s);
//  }
//
//  private String convertErrorToString(CommandInputTextFieldExtender.ErrorType errorType, IReadOnlyMap<String, Object> arguments) {
//    EStringBuilder sb = new EStringBuilder();
//
//    switch (errorType) {
//      case atcUnableDecide:
//        sb.appendFormat("Unable to decide target ATC from '%s'.", arguments.get("command"));
//        break;
//      case atcUnableParse:
//        sb.appendFormat("Unable to parse ATC command from '%s'.", arguments.get("command"));
//        break;
//      case planeMultipleCallsignMatches:
//        sb.appendFormat("Multiple planes matches callsign shortcut from '%s'.", arguments.get("callsign"));
//        break;
//      case planeNoneCallsignMatch:
//        sb.appendFormat("No plane matches callsign/shortcut '%s'.", arguments.get("callsign"));
//        break;
//      case planeUnableParse:
//        sb.appendFormat("Unable to parse plane command for plane '%s' from '%s'.", arguments.get("callsign"), arguments.get("command"));
//        break;
//      case systemUnableParse:
//        sb.appendFormat("Unable to parse system command from '%s.", arguments.get("command"));
//        break;
//      default:
//        throw new UnexpectedValueException(errorType);
//    }
//
//    return sb.toString();
//  }
//
//  private void sendPlaneCommand(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.CommandEventArgs<Callsign, SpeechList<IForPlaneSpeech>> e) {
//    this.sim.sendPlaneCommands(this.userAtcId, e.target, e.command);
//  }
//
//  private void processSpecialCommand(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.SpecialCommandEventArgs e) {
//    switch (e.specialCommand) {
//      case recallRadarPosition:
//        this.recallRadarPosition((int) e.attributes.get("bank"));
//        break;
//      case storeRadarPosition:
//        this.storeRadarPosition((int) e.attributes.get("bank"));
//        break;
//      case toggleSimulatorRun:
//        this.sim.pauseUnpauseSim();
//        break;
//      default:
//        throw new UnexpectedValueException(e.specialCommand);
//    }
//  }
//
//  private void sendSystemCommand(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.CommandEventArgs<Object, ISystemSpeech> e) {
//    this.sim.sendSystemCommand(this.userAtcId, e.command);
//  }
//
//  private void sendAtcCommand(
//          CommandInputTextFieldExtender commandInputTextFieldExtender,
//          CommandInputTextFieldExtender.CommandEventArgs<AtcId, IAtcSpeech> e) {
//    this.sim.sendAtcCommand(this.userAtcId, e.target, e.command);
//  }
//
//
//  private JPanel buildTopPanel() {
//    JPanel ret = new JPanel();
//    ret.setBackground(Color.BLACK);
//    LayoutManager.fillFlowPanel(ret, LayoutManager.eVerticalAlign.middle, 4);
//
//    ButtonBinding.init(this.extBtn);
//
//    Tuple<JPanel, JButton[]> pnlx;
//    pnlx = this.buildButtonBlock(
//            "Cntr",
//            this.displaySettings, "CountryBorderVisible");
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "CTR", this.displaySettings, "CtrBorderVisible",
//            "TMA", this.displaySettings, "TmaBorderVisible"
//    );
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "MRVA", this.displaySettings, "MrvaBorderVisible",
//            "MRVA(lbl)", this.displaySettings, "MrvaBorderAltitudeVisible"
//    );
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "PROH", this.displaySettings, "RestrictedBorderVisible",
//            "PROH(lbl)", this.displaySettings, "RestrictedBorderAltitudeVisible"
//    );
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "VOR", this.displaySettings, "VorVisible",
//            "NDB", this.displaySettings, "NdbVisible"
//    );
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "FIX",
//            this.displaySettings, "FixVisible");
//    ret.add(pnlx.getA());
//    pnlx = this.buildButtonBlock(
//            "FIX-R", this.displaySettings, "FixRouteVisible",
//            "FIX-M", this.displaySettings, "FixMinorVisible"
//    );
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "AIP",
//            this.displaySettings, "AirportVisible");
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "SID", this.displaySettings, "SidVisible",
//            "STAR", this.displaySettings, "StarVisible"
//    );
//    this.wrpRoutes =
//            new AdjustSelectionPanelWrapper<>(new RoutesAdjustSelectionPanelWrapperListener(), this.sim, pnlx.getB());
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "Apps",
//            this.displaySettings, "ApproachesVisible");
//    this.wrpApproaches =
//            new AdjustSelectionPanelWrapper<>(new ApproachesAdjustSelectionPanelWrapperListener(), this.sim, pnlx.getB());
//    ret.add(pnlx.getA());
//
//    pnlx = this.buildButtonBlock(
//            "P(rngs)",
//            this.displaySettings, "RingsVisible");
//    ret.add(pnlx.getA());
//    pnlx = this.buildButtonBlock(
//            "P(hdg)", this.displaySettings, "PlaneHeadingLineVisible",
//            "P(hist)", this.displaySettings, "PlaneHistoryVisible"
//    );
//    ret.add(pnlx.getA());
//
//    {
//      JTextField txtA = new JTextField("0");
//      JTextField txtB = new JTextField("380");
//      JPanel pnl = LayoutManager.createGridPanel(2, 1, 0, txtA, txtB);
//      ret.add(pnl);
//      JButton btn = new JButton("(set)");
//      this.extBtn.set(btn, false);
//      ret.add(btn);
//    }
//
//    return ret;
//  }
//
//  private void recallRadarPosition(int index) {
//    RadarViewPort rp = storedRadarPositions.tryGet(index);
//    if (rp != null) {
//      radar.setViewPort(rp);
//    }
//  }
//
//  private ParsersSet buildParsers() {
//    return new ParsersSet(
//            new PlaneParser(),
//            new AtcParser(),
//            new SystemParser());
//  }
//
//  private void storeRadarPosition(int index) {
//    RadarViewPort rp = this.radar.getViewPort();
//    this.storedRadarPositions.set(index, rp);
//  }
//}
//
////class JButtonExtender {
////  public final Color backOff;
////  public final Color backOn;
////  public final Color foreOff;
////  public final Color foreOn;
////
////  public JButtonExtender(Color backOff, Color foreOff, Color backOn, Color foreOn) {
////    this.backOff = backOff;
////    this.backOn = backOn;
////    this.foreOff = foreOff;
////    this.foreOn = foreOn;
////  }
////
////  public void set(JButton btn, boolean state) {
////    if (state) {
////      btn.setBackground(this.backOn);
////      btn.setForeground(this.foreOn);
////    } else {
////      btn.setBackground(this.backOff);
////      btn.setForeground(this.foreOff);
////    }
////  }
////}
////
////class ButtonBinding {
////  private static JButtonExtender ext;
////
////  public static void init(JButtonExtender ext) {
////    ButtonBinding.ext = ext;
////  }
////
////  private final JButton btn;
////  private final String propertyName;
////  private final Object target;
////
////  public ButtonBinding(Object target, String propertyName, JButton btn) {
////    this.target = target;
////    this.propertyName = propertyName;
////    this.btn = btn;
////
////    btn.addActionListener(new AbstractAction() {
////      @Override
////      public void actionPerformed(ActionEvent e) {
////        doSwitch();
////      }
////    });
////
////    boolean state = getStateValue();
////    ext.set(this.btn, state);
////  }
////
////  private void doSwitch() {
////    boolean state = getStateValue();
////    state = !state;
////    setStateValue(state);
////    ext.set(this.btn, state);
////  }
////
////  private boolean getStateValue() {
////    Class cls = this.target.getClass();
////    Method mi;
////    try {
////      mi = cls.getMethod("is" + propertyName, new Class[0]);
////    } catch (NoSuchMethodException e) {
////      throw new EApplicationException("Unable to find property is" + propertyName + " over " + target + ".", e);
////    }
////    boolean val;
////    try {
////      val = (boolean) mi.invoke(target, (Object[]) null);
////    } catch (IllegalAccessException | InvocationTargetException e) {
////      throw new EApplicationException("Unable to read property is" + propertyName + " over " + target + ".", e);
////    }
////    return val;
////  }
////
////  private void setStateValue(boolean val) {
////    Class cls = this.target.getClass();
////    Method mi;
////    try {
////      mi = cls.getMethod("set" + propertyName, new Class[]{boolean.class});
////    } catch (NoSuchMethodException e) {
////      throw new EApplicationException("Unable to find property set" + propertyName + " over " + target + ".", e);
////    }
////    try {
////      mi.invoke(target, new Object[]{val});
////    } catch (IllegalAccessException | InvocationTargetException e) {
////      throw new EApplicationException("Unable to write property set" + propertyName + " over " + target + ".", e);
////    }
////  }
////}
//
////class RouteComparator implements Comparator<DARoute> {
////
////  private IMap<DARoute, IList<ActiveRunwayThreshold>> map;
////
////  public RouteComparator(IMap<DARoute, IList<ActiveRunwayThreshold>> map) {
////    this.map = map;
////  }
////
////  @Override
////  public int compare(DARoute a, DARoute b) {
////    int ret;
////
////    String sa;
////    String sb;
////    IList<ActiveRunwayThreshold> tmp;
////    tmp = map.get(a);
////    sa = tmp.isEmpty() ? "" : tmp.getFirst().getName();
////    tmp = map.get(b);
////    sb = tmp.isEmpty() ? "" : tmp.getFirst().getName();
////    ret = sa.compareTo(sb);
////    if (ret == 0) {
////      ret = a.getType().compareTo(b.getType());
////      if (ret == 0)
////        ret = a.getName().compareTo(b.getName());
////    }
////    return ret;
////  }
////}
