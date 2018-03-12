package eng.jAtcSim.radarBase;

import eng.eSystem.collections.ReadOnlyList;
import eng.eSystem.events.Event;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.*;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.PlaneSwitchMessage;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.eSystem.EStringBuilder;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.messaging.IMessageParticipant;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.radarBase.global.Color;
import eng.jAtcSim.radarBase.global.Point;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.radarBase.global.TextBlockLocation;
import eng.jAtcSim.radarBase.global.events.EMouseEventArg;
import eng.jAtcSim.radarBase.global.events.KeyEventArg;
import eng.jAtcSim.radarBase.global.events.WithCoordinateEventArg;
import sun.text.resources.cldr.ia.FormatData_ia;

import java.util.*;
import java.util.stream.Collectors;

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

  static class VisualisedMessage {
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

    public void decreaseLifeCounter() {
      this.lifeCounter--;
    }
  }

  static class AirplaneDisplayInfo {
    private static final int DEFAULT_LABEL_SHIFT = 3;
    public final List<Coordinate> planeDotHistory = new LinkedList<>();
    public boolean wasUpdatedFlag = false;
    public Callsign callsign;
    public boolean isAirprox;
    public Coordinate coordinate;
    public int heading;
    public int speed;
    public int verticalSpeed;
    public Atc tunedAtc;
    public Atc responsibleAtc;
    public AirplaneType type;
    public Point labelShift;
    private Squawk squawk;
    private int targetHeading;
    private double targetSpeed;
    private int altitude;
    private int targetAltitude;

    public AirplaneDisplayInfo(Airplane.Airplane4Display planeInfo) {
      this.labelShift = new Point(DEFAULT_LABEL_SHIFT, DEFAULT_LABEL_SHIFT);

      this.callsign = planeInfo.callsign();
      this.type = planeInfo.planeType();
      this.squawk = planeInfo.squawk();
    }

    public void updateInfo(Airplane.Airplane4Display plane) {
      wasUpdatedFlag = true;

      this.speed = plane.speed();
      this.targetSpeed = plane.targetSpeed();
      this.altitude = plane.altitude();
      this.targetAltitude = plane.targetAltitude();
      this.verticalSpeed = plane.verticalSpeed();
      this.heading = plane.heading();
      this.targetHeading = plane.targetHeading();

      this.tunedAtc = plane.tunedAtc();
      this.responsibleAtc = plane.responsibleAtc();

      this.isAirprox = plane.isAirprox();

      this.coordinate = plane.coordinate();

      planeDotHistory.add(plane.coordinate());
    }

    public String format(String pattern) {
      StringBuilder sb = new StringBuilder(pattern);
      int[] p = new int[2];

      while (true) {
        updatePair(sb, p);
        if (p[0] < 0) {
          break;
        }

        String tmp = sb.substring(p[0] + 1, p[1]);
        int index = Integer.parseInt(tmp);
        sb.replace(p[0], p[1] + 1, getFormatValueByIndex(index));
      }

      return sb.toString();
    }

    private String getFormatValueByIndex(int index) {
      switch (index) {
        case 1:
          return this.callsign.toString();
        case 2:
          return this.callsign.getCompany();
        case 3:
          return this.callsign.getNumber();
        case 4:
          return this.type.name;
        case 5:
          return AirplaneDataFormatter.formatTypeCategory(this.type);
        case 8:
          return AirplaneDataFormatter.formatSqwk(this.squawk);
        case 11:
          return AirplaneDataFormatter.formatHeadingLong(this.heading);
        case 12:
          return AirplaneDataFormatter.formatHeadingShort(this.heading);
        case 15:
          return AirplaneDataFormatter.formatHeadingLong(this.targetHeading);
        case 16:
          return AirplaneDataFormatter.formatHeadingShort(this.targetHeading);
        case 21:
          return AirplaneDataFormatter.formatSpeedLong(this.speed);
        case 22:
          return AirplaneDataFormatter.formatSpeedShort(this.speed);
        case 23:
          return AirplaneDataFormatter.formatSpeedAligned(this.speed);
        case 31:
          return AirplaneDataFormatter.formatSpeedLong(this.targetSpeed);
        case 32:
          return AirplaneDataFormatter.formatSpeedShort(this.targetSpeed);
        case 33:
          return AirplaneDataFormatter.formatAltitudeLong(this.altitude);
        case 34:
          return AirplaneDataFormatter.formatAltitudeShort(this.altitude, true);
        case 35:
          return AirplaneDataFormatter.formatAltitudeInFt(this.altitude, true);
        case 36:
          return AirplaneDataFormatter.formatAltitudeLong(this.targetAltitude);
        case 37:
          return AirplaneDataFormatter.formatAltitudeShort(this.targetAltitude, true);
        case 38:
          return AirplaneDataFormatter.formatAltitudeInFt(this.targetAltitude, true);
        case 41:
          return AirplaneDataFormatter.formatVerticalSpeedLong(this.verticalSpeed);
        case 42:
          return AirplaneDataFormatter.formatVerticalSpeedShort(this.verticalSpeed);
        case 43:
          return AirplaneDataFormatter.getClimbDescendChar(this.verticalSpeed);
        default:
          return "???";
      }
    }

    private void updatePair(StringBuilder ret, int[] p) {
      int start = ret.indexOf("{");
      if (start < 0) {
        p[0] = -1;
        return;
      }
      p[0] = start;
      int end = ret.indexOf("}", start);
      p[1] = end;
    }

  }

  static class AirplaneDisplayInfoList {

    private Map<Callsign, AirplaneDisplayInfo> inner = new HashMap<>();

    public void update(ReadOnlyList<Airplane.Airplane4Display> planes) {
      resetWasUpdatedFlag();

      for (Airplane.Airplane4Display plane : planes) {
        AirplaneDisplayInfo adi = tryGetOrAdd(plane);
        adi.updateInfo(plane);
      }

      removeUnupdated();
    }

    public Collection<AirplaneDisplayInfo> getList() {
      return inner.values();
    }

    private void removeUnupdated() {
      List<Callsign> toRem =
          inner.keySet().stream().filter(q -> inner.get(q).wasUpdatedFlag == false).collect(Collectors.toList());
      toRem.forEach(q -> inner.remove(q));
    }

    private AirplaneDisplayInfo tryGetOrAdd(Airplane.Airplane4Display plane) {
      AirplaneDisplayInfo ret;
      if (inner.containsKey(plane.callsign()))
        ret = inner.get(plane.callsign());
      else {
        ret = new AirplaneDisplayInfo(plane);
        inner.put(plane.callsign(), ret);
      }
      return ret;
    }

    private void resetWasUpdatedFlag() {
      inner.values().stream().forEach(q -> q.wasUpdatedFlag = false);
    }
  }

  static class NavaidDisplayInfo {
    public Navaid navaid;
    public boolean isRoute;
  }

  static class NavaidDisplayInfoList implements Iterable<NavaidDisplayInfo> {
    private List<NavaidDisplayInfo> inner = new ArrayList<>();

    public void add(NavaidDisplayInfo ndi) {
      inner.add(ndi);
    }

    public NavaidDisplayInfo getByNavaid(Navaid navaid) {
      NavaidDisplayInfo ret = null;
      for (NavaidDisplayInfo navaidDisplayInfo : inner) {
        if (navaidDisplayInfo.navaid == navaid) {
          ret = navaidDisplayInfo;
          break;
        }
      }
      return ret;
    }

    @Override
    public Iterator<NavaidDisplayInfo> iterator() {
      return inner.iterator();
    }
  }
  private static final double MAX_NM_DIFFERENCE_FOR_SELECTION = 2.5;
  private final TransformationLayer tl;
  private final ICanvas c;
  private final Event<Radar, WithCoordinateEventArg> mouseMoveEvent = new Event(this);
  private final Event<Radar, WithCoordinateEventArg> mouseClickEvent = new Event(this);
  private final Event<Radar, KeyEventArg> keyPressEvent = new Event(this);
  private final Event<Radar, Callsign> selectedAirplaneChangedEvent = new Event<>(this);
  private final DisplaySettings displaySettings;
  private final BehaviorSettings behaviorSettings;
  private final LocalSettings localSettings;
  private final Simulation simulation;
  private final Area area;
  private final MessageManager messageManager;
  private final AirplaneDisplayInfoList planeInfos = new AirplaneDisplayInfoList();
  private final NavaidDisplayInfoList navaids = new NavaidDisplayInfoList();
  private int redrawTick = 0;
  private InfoLine infoLine;
  private Callsign selectedCallsign;

  public Radar(ICanvas canvas, InitialPosition initialPosition,
               Simulation sim, Area area,
               DisplaySettings displaySettings,
               BehaviorSettings behaviorSettings) {
    this.c = canvas;

    this.tl = new TransformationLayer(this.c, initialPosition.coordinate, initialPosition.range);
    this.displaySettings = displaySettings;
    this.behaviorSettings = behaviorSettings;
    this.localSettings = new LocalSettings();
    this.simulation = sim;
    this.area = area;

    buildLocalNavaidList();

    this.messageManager = new MessageManager(this.behaviorSettings.getDisplayTextDelay());

    this.c.getMouseEvent().add(
        (sender, e) -> Radar.this.canvas_onMouseMove((ICanvas) sender, (EMouseEventArg) e));
    this.c.getPaintEvent().add(
        (c) -> Radar.this.canvas_onPaint((ICanvas) c));
    this.c.getKeyEvent().add(
        (c, o) -> Radar.this.canvas_onKeyPress((ICanvas) c, (KeyEventArg) o));
    this.c.getResizedEvent().add(o -> tl.resetPosition());

    // listen to simulation seconds for redraw
    this.simulation.getSecondElapsedEvent().add(o -> redraw(false));
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

    tl.setPosition(
        new Coordinate(
            coordinate.getLatitude().get() + distLat,
            coordinate.getLongitude().get() + distLon));
    redraw(true);
  }

  public void redraw(boolean force) {

    if (!force) {
      if (redrawTick <= 0) {
        planeInfos.update(simulation.getPlanesToDisplay());
        this.redrawTick = displaySettings.refreshRate - 1;
      } else {
        this.redrawTick--;
      }
    }
    this.c.invokeRepaint();
  }

  public LocalSettings getLocalSettings() {
    return localSettings;
  }

  public Event<Radar, Callsign> getSelectedAirplaneChangedEvent() {
    return selectedAirplaneChangedEvent;
  }

  public Callsign getSelectedCallsign() {
    return selectedCallsign;
  }

  public void setSelectedCallsign(Callsign selectedCallsign) {
    Callsign bef = this.selectedCallsign;
    this.selectedCallsign = selectedCallsign;
    if (bef != this.selectedCallsign)
      this.selectedAirplaneChangedEvent.raise(this.selectedCallsign);
  }

  private void buildLocalNavaidList() {

    for (Navaid navaid : area.getNavaids()) {
      NavaidDisplayInfo ndi = new NavaidDisplayInfo();
      ndi.navaid = navaid;
      ndi.isRoute = false;
      this.navaids.add(ndi);
    }

    for (Runway runway : simulation.getActiveAirport().getRunways()) {
      for (RunwayThreshold runwayThreshold : runway.getThresholds()) {
        for (Route route : runwayThreshold.getRoutes()) {
          //TODO this is incredibly time consuming, do it better way?
          for (IAtcCommand command : route.getCommands()) {
            if (command instanceof ProceedDirectCommand) {
              ProceedDirectCommand pdc = (ProceedDirectCommand) command;
              NavaidDisplayInfo ndi = this.navaids.getByNavaid(pdc.getNavaid());
              ndi.isRoute = true;
            }
          }
        }
      }
    }
  }

  private void canvas_onMouseMove(ICanvas sender, EMouseEventArg e) {
    Point pt = e.getPoint();
    Coordinate coord = tl.toCoordinate(pt);
    switch (e.type) {
      case wheelScroll:
        if (e.wheel > 0) {
          zoomOut();
        } else {
          zoomIn();
        }
        break;
      case click:
        if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.left) {
          // try to select an airplane
          Point p = e.getPoint();
          Coordinate c = tl.toCoordinate(p);
          AirplaneDisplayInfo plane = tryGetSelectedAirplane(c);
          if (plane == null)
            this.setSelectedCallsign(null);
          else if (this.selectedCallsign == plane.callsign)
            this.setSelectedCallsign(null);
          else
            this.setSelectedCallsign(plane.callsign);
        }
        this.mouseClickEvent.raise(new WithCoordinateEventArg(coord));
        break;
      case doubleClick:
        centerAt(coord);
        break;
      case move:
        this.mouseMoveEvent.raise(new WithCoordinateEventArg(coord));
        break;
      case dragged:
        if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.right) {
          coord = tl.toCoordinateDelta(e.getDropRangePoint());
          moveMapBy(coord);
        } else if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.left) {
          this.infoLine = null;
          this.redraw(true);
        } else {
        }
        break;
      case dragging:
        if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.left) {
          Coordinate fromCoordinate = tl.toCoordinate(e.getPoint());
          Coordinate toCoordinate = tl.toCoordinate(e.getDropPoint());
          infoLine = new InfoLine(fromCoordinate, toCoordinate);
          this.redraw(true);
        }
        break;
    }
  }

  private AirplaneDisplayInfo tryGetSelectedAirplane(Coordinate c) {
    AirplaneDisplayInfo bestAdi = null;
    double bestDiff = Double.MAX_VALUE;
    for (AirplaneDisplayInfo adi : this.planeInfos.getList()) {
      double tmpDif = Coordinates.getDistanceInNM(c, adi.coordinate);
      if (tmpDif < bestDiff) {
        bestDiff = tmpDif;
        bestAdi = adi;
      }
    }

    if (bestDiff > MAX_NM_DIFFERENCE_FOR_SELECTION)
      return null;
    else
      return bestAdi;
  }

  private void canvas_onPaint(ICanvas sender) {
    c.beforeDraw();
    drawBackground();
    drawBorders();
    drawRoutes(true, false);
    drawRoutes(false, true);
    drawApproaches();
    drawNavaids();
    drawAirports();
    drawAirplanes();
    if (behaviorSettings.isPaintMessages())
      drawCaptions();
    drawTime();

    drawInfoLine();

    c.afterDraw();
  }

  private void drawInfoLine() {
    if (this.infoLine != null) {

      DisplaySettings.ColorWidthFontSettings cwfs = this.displaySettings.infoLine;

      tl.drawLine(
          this.infoLine.from,
          this.infoLine.to,
          cwfs.getColor(),
          cwfs.getWidth()
      );

      String distS = String.format("%03d %.1fnm", this.infoLine.heading, this.infoLine.distanceInNm);
      String timeS = String.format("%s:%s/%s:%s/%s:%s",
          InfoLine.toIntegerMinutes(this.infoLine.seconds280),
          infoLine.toIntegerSeconds(this.infoLine.seconds280),
          InfoLine.toIntegerMinutes(this.infoLine.seconds250),
          infoLine.toIntegerSeconds(this.infoLine.seconds250),
          InfoLine.toIntegerMinutes(this.infoLine.seconds200),
          infoLine.toIntegerSeconds(this.infoLine.seconds200));


      tl.drawText(distS, this.infoLine.to,
          3, -cwfs.getFont().getSize() * 2 - 3,
          cwfs.getFont(), cwfs.getColor());

      tl.drawText(timeS, this.infoLine.to,
          3, -cwfs.getFont().getSize(),
          cwfs.getFont(), cwfs.getColor());
    }
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

    tl.setPosition(
        new Coordinate(
            tl.getTopLeft().getLatitude().get() + distShiftLat,
            tl.getTopLeft().getLongitude().get() + distShiftLon),
        tl.getWidthInNM() * multiplier);

    redraw(true);

  }

  private void moveMapBy(Coordinate c) {
    tl.setPosition(
        tl.getTopLeft().add(c));
    redraw(true);
  }

  private void drawBackground() {
    Color color = displaySettings.mapBackcolor;
    tl.clear(color);
  }

  private void drawBorders() {
    for (Border b : area.getBorders()) {
      switch (b.getType()) {
        case TMA:
          if (localSettings.isTmaBorderVisible())
            drawBorder(b);
          break;
        case CTR:
          if (localSettings.isCtrBorderVisible())
            drawBorder(b);
          break;
        case Country:
          if (localSettings.isCountryBorderVisible())
            drawBorder(b);
          break;
        default:
          drawBorder(b);
      }
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
      if (drawArrivalRoutes && (r.getType() == Route.eType.star || r.getType() == Route.eType.transition)) {
        if (localSettings.isStarVisible()) drawStar(r.getNavaids());
      } else if (drawDepartureRoutes && r.getType() == Route.eType.sid) {
        if (localSettings.isSidVisible()) drawSid(r.getNavaids());
      }
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
    //for (Navaid n : area.getNavaids()) {
    for (NavaidDisplayInfo ndi : this.navaids) {
      switch (ndi.navaid.getType()) {
        case NDB:
          if (localSettings.isNdbVisible()) drawNavaid(ndi.navaid);
          break;
        case Airport:
          if (localSettings.isAirportVisible()) drawNavaid(ndi.navaid);
          break;
        case VOR:
          if (localSettings.isVorVisible()) drawNavaid(ndi.navaid);
          break;
        case Fix:
        case FixMinor:
          boolean isVisible = false;
          if (ndi.navaid.getType() == Navaid.eType.Fix && localSettings.isFixVisible())
            isVisible = true;
          if (ndi.navaid.getType() == Navaid.eType.FixMinor && localSettings.isFixMinorVisible())
            isVisible = true;
          if (ndi.isRoute && localSettings.isFixRouteVisible())
            isVisible = true;
          if (isVisible) drawNavaid(ndi.navaid);
          break;
        default:
          drawNavaid(ndi.navaid);
      }
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
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 0, dt.getFont(), ds.getColor());
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
    for (AirplaneDisplayInfo adi : this.planeInfos.getList()) {
      drawPlane(adi);
    }
  }

  private void drawPlane(AirplaneDisplayInfo adi) {

    DisplaySettings.PlaneLabelSettings dp = getPlaneLabelDisplaySettingsBy(adi);
    if (dp.isVisible() == false) {
      return;
    }
    DisplaySettings.TextSettings dt = displaySettings.callsign;

    // eval special color for airproxes and selected plane
    Color c = dp.getColor();
    if (adi.isAirprox) {
      c = new Color(0xFF, 0, 0);
    } else if (this.selectedCallsign == adi.callsign) {
      c = displaySettings.selected.getColor();
    }

    // plane dot and direction line
    tl.drawPoint(adi.coordinate, c, dp.getPointWidth()); // point of plane
    tl.drawLineByHeadingAndDistance(adi.coordinate, adi.heading, dp.getHeadingLineLength(), c, 1);

    // separation ring
    if (localSettings.isRingsVisible()) {
      if (adi.speed > 100 || adi.verticalSpeed != 0) {
        tl.drawCircleAroundInNM(adi.coordinate, dp.getSeparationRingRadius(),
            c, 1);
      }
    }

    // plane label
    StringBuilder sb = new StringBuilder();
    sb.append(
        buildPlaneString(dp.getFirstLineFormat(), adi));

    if (adi.tunedAtc != adi.responsibleAtc) {
      sb.append("*");
    }

    sb.append("\r\n");
    sb.append(
        buildPlaneString(dp.getSecondLineFormat(), adi));
    sb.append("\r\n");
    sb.append(
        buildPlaneString(dp.getThirdLineFormat(), adi));

    Point labelShift = adi.labelShift;
    tl.drawText(sb.toString(), adi.coordinate, labelShift.x, labelShift.y, dt.getFont(), c);

    List<Coordinate> hist = adi.planeDotHistory;
    int printedDots = 0;
    int index = hist.size() - 1;
    while (printedDots < dp.getHistoryDotCount()) {
      if (index % dp.getHistoryDotStep() == 0) {
        tl.drawPoint(hist.get(index), c, 3);
        printedDots++;
      }
      index--;
      if (index < 0) break;
    }
  }

  private DisplaySettings.PlaneLabelSettings getPlaneLabelDisplaySettingsBy(AirplaneDisplayInfo adi) {
    DisplaySettings.PlaneLabelSettings ret;

    if (adi.speed == 0)
      ret = displaySettings.stopped;
    else if (adi.responsibleAtc.getType() == Atc.eType.app)
      ret = displaySettings.app;
    else if (adi.responsibleAtc.getType() == Atc.eType.twr)
      ret = displaySettings.twr;
    else if (adi.responsibleAtc.getType() == Atc.eType.ctr)
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

  private String buildPlaneString(String lineFormat, AirplaneDisplayInfo adi) {
    String ret = adi.format(lineFormat);
    return ret;
  }

  private String getMessageContentAsString(Message msg) {
    String ret;
    if (msg.isSourceOfType(Airplane.class)) {
      if (msg.isContentOfType(List.class)) {
        //EStringBuilder esb = new EStringBuilder();
        List<String> sentences = new ArrayList();
        SpeechList<ISpeech> lst = msg.getContent();
        for (ISpeech iSpeech : lst) {
          String sentence = behaviorSettings.getFormatter().format(iSpeech);
          if (sentence == null || sentence.trim().length() == 0) continue;
          sentences.add(sentence);
        }
        ret = formatToVisualSentence(sentences);
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

  private String formatToVisualSentence(List<String> sentences) {
    EStringBuilder ret = new EStringBuilder();
    for (int i = 0; i < sentences.size(); i++) {
      String sentence = sentences.get(i);
      if (sentence.trim().length() == 0) continue;
      if (i == 0)
        sentence = makeBeginSentence(sentence);
      else
        ret.append(", ");
      ret.append(sentence);
    }
    ret.append(".");
    return ret.toString();
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

class InfoLine {
  public final Coordinate from;
  public final Coordinate to;
  public final int heading;
  public final double distanceInNm;
  public final double seconds200;
  public final double seconds250;
  public final double seconds280;

  public InfoLine(Coordinate from, Coordinate to) {
    this.from = from;
    this.to = to;
    this.distanceInNm = Coordinates.getDistanceInNM(from, to);
    this.heading = (int) Coordinates.getBearing(from, to);
    this.seconds200 = this.distanceInNm / 200d * 3600d;
    this.seconds250 = this.distanceInNm / 250d * 3600d;
    this.seconds280 = this.distanceInNm / 280d * 3600d;
  }

  public static String toIntegerMinutes(double value) {
    int tmp = (int) (value / 60);
    return Integer.toString(tmp);
  }

  public static String toIntegerSeconds(double value) {
    double tmp = value % 60;
    return String.format("%02.0f", tmp);
  }
}