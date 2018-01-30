package eng.jAtcSim.radarBase;

import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.radarBase.global.Color;
import eng.jAtcSim.radarBase.global.Point;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.radarBase.global.TextBlockLocation;
import eng.jAtcSim.radarBase.global.events.EMouseEventArg;
import eng.jAtcSim.radarBase.global.events.KeyEventArg;
import eng.jAtcSim.radarBase.global.events.WithCoordinateEventArg;
import eng.eSystem.events.Event;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.PlaneSwitchMessage;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.coordinates.RadarRange;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.EStringBuilder;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.messaging.IMessageParticipant;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;

import java.util.*;

public class Radar {

  static class MessageSet {

    public final List<String> atc = new ArrayList<>();
    public final List<String> plane = new ArrayList<>();
    public final List<String> system = new ArrayList<>();
  }
  static class MessageManager {
    private final int delay;
    private List<VisualisedMessage> items = new ArrayList<>();

    public MessageManager(int delay) {
      this.delay = delay;
    }

    public void add(IMessageParticipant source, String text) {
      VisualisedMessage di = new VisualisedMessage(source, text, delay);
      items.add(di);
    }

    public void decreaseMessagesLifeCounter() {
      for (VisualisedMessage item : items) {
        item.decreaseLifeCounter();
      }
      items.removeIf(q -> q.getLifeCounter() <= 0);
    }

    public List<VisualisedMessage> getCurrent() {
      return items;
    }
  }
  static class VisualisedMessage{
    private final IMessageParticipant source;
    private final String text;
    private int lifeCounter;

    public VisualisedMessage(IMessageParticipant source, String text, int lifeCounter) {
      this.source = source;
      this.text = text;
      this.lifeCounter = lifeCounter;
    }

    public IMessageParticipant getSource() {
      return source;
    }

    public String getText() {
      return text;
    }

    public int getLifeCounter() {
      return lifeCounter;
    }

    public void decreaseLifeCounter(){
      this.lifeCounter--;
    }
  }

  static class PlaneHistoryDotManager {

    private final Map<Callsign, List<Coordinate>> inner = new HashMap<>();
    private final Map<Callsign, Integer> maxCount = new HashMap<>();

    public void add(Callsign cs, Coordinate c, int maxHistory) {
      if (inner.containsKey(cs) == false) {
        inner.put(cs, new LinkedList<Coordinate>());
        maxCount.put(cs, maxHistory);
      }

      inner.get(cs).add(c);
      if (maxCount.get(cs) != maxHistory) {
        maxCount.put(cs, maxHistory);
      }
    }

    public void removeOneHistoryFromAll() {
      List<Callsign> tr = new LinkedList<>();
      for (Callsign cs : inner.keySet()) {
        List<Coordinate> l = inner.get(cs);
        if (l.size() > maxCount.get(cs)) {
          l.remove(0);
        }
        if (l.isEmpty()) {
          tr.add(cs);
        }
      }

      for (Callsign cs : tr) {
        inner.remove(cs);
        maxCount.remove(cs);
      }
    }

    List<Coordinate> get(Callsign callsign) {
      return inner.get(callsign);
    }
  }

  private final TransformationLayer tl;
  private final ICanvas c;
  private final Event<Radar, WithCoordinateEventArg> mouseMoveEvent = new Event(this);
  private final Event<Radar, WithCoordinateEventArg> mouseClickEvent = new Event(this);
  private final EventSimple<Radar> paintEvent = new EventSimple(this);
  private final Event<Radar, KeyEventArg> keyPressEvent = new Event(this);

  private final DisplaySettings displaySettings;
  private final BehaviorSettings behaviorSettings;

  private final Simulation simulation;
  private final Area area;


  private final PlaneHistoryDotManager planeDotHistory = new PlaneHistoryDotManager();
  /**
   * Last drawn positions of planes.
   */
  private final Map<Callsign, Coordinate> lastPlanePositions = new HashMap();
  /**
   * Definition of the shift of the airplane info label
   */
  private final Map<Callsign, Point> customPlaneLabelShift = new HashMap();

  private final MessageManager messageManager;

  public Radar(ICanvas canvas, RadarRange radarRange,
               Simulation sim, Area area,
               DisplaySettings displaySettings,
               BehaviorSettings behaviorSettings) {
    this.c = canvas;
    this.tl = new TransformationLayer(this.c, radarRange.topLeft, radarRange.bottomRight);
    this.displaySettings = displaySettings;
    this.behaviorSettings = behaviorSettings;
    this.simulation = sim;
    this.area = area;

    this.messageManager = new MessageManager(this.behaviorSettings.getDisplayTextDelay());

    this.c.getMouseEvent().add(
        (sender, e) -> Radar.this.canvas_onMouseMove((ICanvas) sender, (EMouseEventArg) e));
    this.c.getPaintEvent().add(
        (c) -> Radar.this.canvas_onPaint((ICanvas) c));
    this.c.getKeyEvent().add(
        (c, o) -> Radar.this.canvas_onKeyPress((ICanvas) c, (KeyEventArg) o));

    // listen to simulation seconds for redraw
    this.simulation.secondElapsedEvent.add(o -> redraw());
  }

  public void zoomIn() {
    zoomBy(0.9);
  }

  public void zoomOut() {
    zoomBy(1.1);
  }

  public void centerAt(Coordinate coordinate) {
    double distLat
        = tl.getTopLeft().getLatitude().get() - tl.getBottomRight().getLatitude().get();
    double distLon
        = tl.getTopLeft().getLongitude().get() - tl.getBottomRight().getLongitude().get();

    distLat = distLat / 2d;
    distLon = distLon / 2d;

    tl.setCoordinates(
        new Coordinate(
            coordinate.getLatitude().get() + distLat,
            coordinate.getLongitude().get() + distLon),
        new Coordinate(
            coordinate.getLatitude().get() - distLat,
            coordinate.getLongitude().get() - distLon));
    redraw();
  }

  public void redraw() {

    this.c.invokeRepaint();

  }

  private void canvas_onMouseMove(ICanvas sender, EMouseEventArg e) {
    Point pt = e.getPoint();
    Coordinate coord = tl.toCoordinate(pt);
    switch (e.type) {
      case WheelScroll:
        if (e.wheel > 0) {
          zoomOut();
        } else {
          zoomIn();
        }
        break;
      case Click:
        this.mouseClickEvent.raise(new WithCoordinateEventArg(coord));
        break;
      case DoubleClick:
        centerAt(coord);
        break;
      case Move:
        this.mouseMoveEvent.raise(new WithCoordinateEventArg(coord));
        break;
      case Drag:
        // drag bez priznaku je posun mapy, jinak je to posun letadla
        if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.right) {
          coord = tl.toCoordinateDelta(e.getDropRangePoint());
          moveMapBy(coord);
        }
        break;
    }
  }

  private void canvas_onPaint(ICanvas sender) {
    c.beforeDraw();
    drawBackground();
    drawBorders();
    drawRoutes(true, false);
    drawRoutes(false,true);
    drawApproaches();
    drawNavaids();
    drawAirports();
    drawAirplanes();
    if (behaviorSettings.isPaintMessages())
      drawCaptions();
    drawTime();
    c.afterDraw();
  }

  private void canvas_onKeyPress(ICanvas sender, KeyEventArg e) {
    // Key codes from KeyEvent class from java.awt
    switch (e.getKeyCode()) {
      case 0x22: // Page down
        zoomIn();
        break;
      case 0x21: // Page up
        zoomOut();
        break;
      default:
        this.keyPressEvent.raise(e);
    }

  }

  private void zoomBy(double multiplier) {
    double distLat
        = tl.getTopLeft().getLatitude().get() - tl.getBottomRight().getLatitude().get();
    double distLon
        = tl.getTopLeft().getLongitude().get() - tl.getBottomRight().getLongitude().get();

    distLat = distLat / 2d;
    distLon = distLon / 2d;

    double distShiftLat = distLat * multiplier - distLat;
    double distShiftLon = distLon * multiplier - distLon;

    tl.setCoordinates(
        new Coordinate(
            tl.getTopLeft().getLatitude().get() + distShiftLat,
            tl.getTopLeft().getLongitude().get() + distShiftLon),
        new Coordinate(
            tl.getBottomRight().getLatitude().get() - distShiftLat,
            tl.getBottomRight().getLongitude().get() - distShiftLon));

    redraw();

  }

  private void moveMapBy(Coordinate c) {
    tl.setCoordinates(
        tl.getTopLeft().add(c),
        tl.getBottomRight().add(c));
    redraw();
  }

  private void drawBackground() {
    Color color = displaySettings.mapBackcolor;
    tl.clear(color);
  }

  private void drawBorders() {
    for (Border b : area.getBorders()) {
      drawBorder(b);
    }
  }

  private void drawBorder(Border border) {

    DisplaySettings.ColorWidthSettings ds = getDispSettBy(border);

    Coordinate last = null;
    for (int i = 0; i < border.getPoints().size(); i++) {
      BorderPoint bp = border.getPoints().get(i);
      if (bp instanceof BorderExactPoint) {
        BorderExactPoint bep = (BorderExactPoint) bp;
        if (last != null) {
          tl.drawLine(last, bep.getCoordinate(), ds.getColor(), ds.getWidth());
        }
        last = bep.getCoordinate();
      } else if (bp instanceof BorderArcPoint) {
        BorderExactPoint bPrev = (BorderExactPoint) border.getPoints().get(i - 1);
        BorderExactPoint bNext = (BorderExactPoint) border.getPoints().get(i + 1);
        drawArc(bPrev, (BorderArcPoint) bp, bNext, ds.getColor());
        last = null;
      } else {
        throw new UnsupportedOperationException();
      }
    }

    if (border.isEnclosed() && !border.getPoints().isEmpty()) {
      BorderExactPoint lastP = (BorderExactPoint) border.getPoints().get(border.getPoints().size() - 1);
      BorderExactPoint firstP = (BorderExactPoint) border.getPoints().get(0);
      tl.drawLine(
          lastP.getCoordinate(), firstP.getCoordinate(),
          ds.getColor(), ds.getWidth());
    }
  }

  private void drawRoutes(boolean drawArrivalRoutes, boolean drawDepartureRoutes) {
    for (Route r : simulation.getActiveRunwayThreshold().getRoutes()) {
      if (drawArrivalRoutes && (r.getType() == Route.eType.star || r.getType() == Route.eType.transition))
        drawStar(r.getNavaids());
      else if (drawDepartureRoutes && r.getType() == Route.eType.sid)
        drawSid(r.getNavaids());
    }
  }

  private void drawStar(List<Navaid> navaidPoints) {
    DisplaySettings.ColorWidthSettings sett = displaySettings.star;
    for (int i = 0; i < navaidPoints.size() - 1; i++) {
      tl.drawLine(
          navaidPoints.get(i).getCoordinate(),
          navaidPoints.get(i + 1).getCoordinate(),
          sett.getColor(),
          sett.getWidth());
    }
  }

  private void drawSid(List<Navaid> navaidPoints) {
    DisplaySettings.ColorWidthSettings sett = displaySettings.sid;
    for (int i = 0; i < navaidPoints.size() - 1; i++) {
      tl.drawLine(
          navaidPoints.get(i).getCoordinate(),
          navaidPoints.get(i + 1).getCoordinate(),
          sett.getColor(),
          sett.getWidth());
    }
  }

  private void drawApproaches() {
    Approach a = simulation.getActiveRunwayThreshold().getHighestApproach();
    if (a != null) {
      drawApproach(a);
    }
  }

  private void drawApproach(Approach approach) {
    Coordinate start = Coordinates.getCoordinate(
        approach.getPoint(),
        Headings.add(approach.getRadial(), 180),
        17);
    //TODO colors should be configurable
    tl.drawLine(start, approach.getPoint(), Color.MAGENTA, 1);
    if (approach.getParent().getFafCross() != null) {
      tl.drawCross(approach.getParent().getFafCross(), Color.MAGENTA, 5, 1);
    }

  }

  private void drawNavaids() {
    for (Navaid n : area.getNavaids()) {
      drawNavaid(n);
    }
  }

  private void drawNavaid(Navaid navaid) {
    DisplaySettings.ColorWidthBorderSettings ds = getDispSettBy(navaid);
    DisplaySettings.TextSettings dt = displaySettings.navaid;

    switch (navaid.getType()) {
      case VOR:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawCircleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
      case NDB:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawTriangleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
      case Fix:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        //p.drawCircleAround(navaid.getCoordinate(), 9, ds.getColor(), 1);
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 0, dt.getFont(), ds.getColor());
        break;
      case FixMinor:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        break;
      case Airport:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
    }

  }

  private void drawAirports() {
    for (Airport a : area.getAirports()) {
      drawAirport(a);
    }
  }

  private void drawAirport(Airport a) {
    for (Runway r : a.getRunways()) {
      drawRunway(r);
    }
  }

  private void drawRunway(Runway runway) {
    DisplaySettings.ColorWidthSettings ds = getDispSettBy(runway);

    tl.drawLine(
        runway.getThresholdA().getCoordinate(),
        runway.getThresholdB().getCoordinate(),
        ds.getColor(), ds.getWidth());
  }

  private void drawAirplanes() {
    for (Airplane.AirplaneInfo ai : simulation.getPlaneInfos()) {
      drawPlane(ai);
    }
  }

  private void drawPlane(Airplane.AirplaneInfo planeInfo) {

    DisplaySettings.PlaneLabelSettings dp = getPlaneLabelDisplaySettingsBy(planeInfo);
    DisplaySettings.TextSettings dt = displaySettings.callsign;
    Color c = dp.getColor();
    if (planeInfo.isAirprox()) {
      c = new Color(0xFF, 0, 0);
    }

    if (dp.isVisible() == false) {
      return;
    }

    // plane dot and direction line
    tl.drawPoint(planeInfo.coordinate(), c, dp.getPointWidth()); // point of plane
    tl.drawLineByHeadingAndDistance(planeInfo.coordinate(), planeInfo.heading(), dp.getHeadingLineLength(), c, 1);

    // separation ring
    if (planeInfo.speed() > 100 || planeInfo.verticalSpeed() != 0) {
      tl.drawCircleAroundInNM(planeInfo.coordinate(), dp.getSeparationRingRadius(),
          c, 1);
    }

    // plane label
    StringBuilder sb = new StringBuilder();
    sb.append(
        buildPlaneString(dp.getFirstLineFormat(), planeInfo));

    if (planeInfo.tunedAtc() != planeInfo.responsibleAtc()) {
      sb.append("*");
    }

    sb.append("\r\n");
    sb.append(
        buildPlaneString(dp.getSecondLineFormat(), planeInfo));
    sb.append("\r\n");
    sb.append(
        buildPlaneString(dp.getThirdLineFormat(), planeInfo));

    Point labelShift;
    if (customPlaneLabelShift.containsKey(planeInfo.callsign())) {
      labelShift = customPlaneLabelShift.get(planeInfo.callsign());
    } else {
      labelShift = new Point(3, 3);
    }
    tl.drawText(sb.toString(), planeInfo.coordinate(), labelShift.x, labelShift.y, dt.getFont(), c);

    // plane history
    // TODO implement
    //if (refreshTick % dp.getHistoryDotStep() == 0) {
      this.planeDotHistory.add(planeInfo.callsign(), planeInfo.coordinate(), dp.getHistoryDotCount());
    //}
    List<Coordinate> hist = planeDotHistory.get(planeInfo.callsign());
    if (hist != null) {
      for (Coordinate coordinate : hist) {
        tl.drawPoint(coordinate, c, 3);
      }
    }

    // plane positions
    lastPlanePositions.put(planeInfo.callsign(), planeInfo.coordinate());
  }

  private DisplaySettings.PlaneLabelSettings getPlaneLabelDisplaySettingsBy(Airplane.AirplaneInfo planeInfo) {
    DisplaySettings.PlaneLabelSettings ret;

    if (planeInfo.speed() == 0)
      ret = displaySettings.stopped;
    else if (planeInfo.responsibleAtc().getType() == Atc.eType.app)
      ret = displaySettings.app;
    else if (planeInfo.responsibleAtc().getType() == Atc.eType.twr)
      ret = displaySettings.twr;
    else if (planeInfo.responsibleAtc().getType() == Atc.eType.ctr)
      ret = displaySettings.ctr;
    else
      throw new ENotSupportedException();

    return ret;
  }

  private void drawCaptions() {
    Messenger ms = simulation.getMessenger();
    List<Message> msgs = ms.getByTarget(simulation.getAppAtc(), true);

    for (Message msg : msgs) {
      String formattedText =
          getMessageContentAsString(msg);
      messageManager.add(msg.getSource(), formattedText);
    }

    boolean containsAtcMessage =
        msgs.stream().anyMatch(q -> q.isSourceOfType(Atc.class));
    boolean containsPlaneMessage =
        msgs.stream().anyMatch(q -> q.isSourceOfType(Airplane.class));

    if (containsAtcMessage) {
      SoundManager.playAtcNewMessage();
    } else if (containsPlaneMessage) {
      SoundManager.playPlaneNewMessage();
    }

    drawMessages(messageManager.getCurrent());

    messageManager.decreaseMessagesLifeCounter();
  }

  private void drawMessages(List<VisualisedMessage> msgs) {
    MessageSet ms = createMessageSet(msgs);

    DisplaySettings.TextSettings dt;

    dt = displaySettings.atc;
    tl.drawTextBlock(ms.atc, TextBlockLocation.bottomRight, dt.getFont(), dt.getColor());

    dt = displaySettings.plane;
    tl.drawTextBlock(ms.plane, TextBlockLocation.bottomLeft, dt.getFont(), dt.getColor());

    dt = displaySettings.system;
    tl.drawTextBlock(decodeSystemMultilines(ms.system), TextBlockLocation.topRight, dt.getFont(), dt.getColor());
  }

  private void drawTime() {

    // todo rewritten, check
    DisplaySettings.TextSettings dt = displaySettings.time;
    List<String> lst = new ArrayList(1);
    lst.add(simulation.getNow().toString());
    tl.drawTextBlock(lst, TextBlockLocation.topLeft, dt.getFont(), dt.getColor());
  }

  private MessageSet createMessageSet(List<VisualisedMessage> msgs) {
    MessageSet ret = new MessageSet();

    for (VisualisedMessage m : msgs) {
      if (m.getSource() == Messenger.SYSTEM) {
        ret.system.add(">> " + m.getText());
      } else if (m.getSource() instanceof Atc) {
        ret.atc.add(m.getText() + " [" + m.getSource().getName() + "]");
      } else if (m.getSource() instanceof Airplane) {
        ret.plane.add(m.getSource().getName() + ": " + m.getText());
      } else {
        throw new ENotSupportedException();
      }
    }
    return ret;
  }

  private List<String> decodeSystemMultilines(List<String> system) {
    List<String> ret = new ArrayList<>();
    String del = "\r\n";
    for (String s : system) {
      String[] spl = s.split(del);
      ret.addAll(Arrays.asList(spl));
    }
    return ret;
  }

  private void drawArc(BorderExactPoint bPrev, BorderArcPoint borderArcPoint, BorderExactPoint bNext, Color color) {
    double startBear = Coordinates.getBearing(borderArcPoint.getCoordinate(), bPrev.getCoordinate());
    double endBear = Coordinates.getBearing(borderArcPoint.getCoordinate(), bNext.getCoordinate());
    double distance = Coordinates.getDistanceInNM(borderArcPoint.getCoordinate(), bPrev.getCoordinate());
    if (borderArcPoint.getDirection() == BorderArcPoint.eDirection.counterclockwise) {
      double tmp = startBear;
      startBear = endBear;
      endBear = tmp;
    }

    tl.drawArc(borderArcPoint.getCoordinate(), startBear, endBear, distance, color);
  }

  private DisplaySettings.ColorWidthSettings getDispSettBy(Border border) {
    switch (border.getType()) {
      case CTR:
        return displaySettings.borderCtr;
      case Country:
        return displaySettings.borderCountry;
      case TMA:
        return displaySettings.borderTma;
      default:
        throw new ERuntimeException("Border type " + border.getType() + " not implemented.");
    }
  }

  private DisplaySettings.ColorWidthSettings getDispSettBy(Runway runway) {
    if (runway.isActive()) {
      return displaySettings.activeRunway;
    } else {
      return displaySettings.closedRunway;
    }
  }

  private DisplaySettings.ColorWidthBorderSettings getDispSettBy(Navaid navaid) {
    switch (navaid.getType()) {
      case Fix:
        return displaySettings.navFix;
      case FixMinor:
        return displaySettings.navFixMinor;
      case NDB:
        return displaySettings.navNDB;
      case VOR:
        return displaySettings.navVOR;
      case Airport:
        return displaySettings.navAirport;
      default:
        throw new ERuntimeException("Navaid type " + navaid.getType() + " is not supported.");
    }
  }

  private String buildPlaneString(String lineFormat, Airplane.AirplaneInfo planeInfo) {
    String ret = planeInfo.format(lineFormat);
    return ret;
  }

  private String getMessageContentAsString(Message msg) {
    String ret;
    if (msg.isSourceOfType(Airplane.class)) {
      if (msg.isContentOfType(List.class)) {
        EStringBuilder esb = new EStringBuilder();
        SpeechList<ISpeech> lst = msg.getContent();
        for (int i = 0; i < lst.size(); i++) {
          ISpeech sp = lst.get(i);
          String sentence = behaviorSettings.getFormatter().format(sp);
          if (i == 0) {
            esb.append(makeBeginSentence(sentence));
          } else
            esb.append(sentence);
          if (i < lst.size() - 1)
            esb.append(", ");
          else
            esb.append(".");
        }
        ret = esb.toString();
      } else {
        ISpeech sp = msg.getContent();
        ret = behaviorSettings.getFormatter().format(sp);
      }
    } else if (msg.isSourceOfType(Atc.class)) {
      if (msg.isContentOfType(PlaneSwitchMessage.class)) {
        PlaneSwitchMessage psm = msg.<PlaneSwitchMessage>getContent();
        ret = behaviorSettings.getFormatter().format(msg.getSource(), psm);
      } else if (msg.isContentOfType(StringMessageContent.class)) {
        ret = msg.<StringMessageContent>getContent().getMessageText();
      } else {
        throw new ENotSupportedException();
      }
    } else {
      // system messages
      ret = msg.<StringMessageContent>getContent().getMessageText();
    }
    return ret;
  }

  private String makeBeginSentence(String sentence) {
    StringBuilder ret = new StringBuilder();

    if (sentence.length() > 0)
      ret.append(Character.toUpperCase(sentence.charAt(0)));

    if (sentence.length() > 1)
      ret.append(sentence.substring(1));

    return ret.toString();
  }
}
