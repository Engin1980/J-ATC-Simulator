package eng.jAtcSim.abstractRadar;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.Event;
import eng.eSystem.events.EventSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.abstractRadar.global.*;
import eng.jAtcSim.abstractRadar.global.events.EMouseEventArg;
import eng.jAtcSim.abstractRadar.global.events.KeyEventArg;
import eng.jAtcSim.abstractRadar.global.events.WithCoordinateEventArg;
import eng.jAtcSim.abstractRadar.settings.RadarBehaviorSettings;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.abstractRadar.support.*;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.shared.Global;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Radar {

  public static final int BORDER_ALTITUDE_MAXIMUM_VALUE = 99000;
  //  private static final int DRAW_STEP = 10;
  public static final int BORDER_ALTITUDE_MINIMUM_VALUE = 0;
  private static final double MAX_NM_DIFFERENCE_FOR_SELECTION = 2.5;
  private final Area area;
  private final RadarBehaviorSettings behaviorSettings;
  private final ICanvas<?> c;
  private final RadarDisplaySettings displaySettings;
  private final IList<Approach> drawnApproaches = new EDistinctList<>(EDistinctList.Behavior.skip);
  private final IList<DARoute> drawnRoutes = new EDistinctList<>(EDistinctList.Behavior.skip);
  private final EventSimple<Radar> gotFocusEvent = new EventSimple<>(this);
  private InfoLine infoLine;
  private final Event<Radar, KeyEventArg> keyPressEvent = new Event<>(this);
  private final VisualisedMessageManager messageManager;
  private final Event<Radar, WithCoordinateEventArg> mouseClickEvent = new Event<>(this);
  private final Event<Radar, WithCoordinateEventArg> mouseMoveEvent = new Event<>(this);
  private final NavaidDisplayInfoList navaids = new NavaidDisplayInfoList();
  private final AirplaneDisplayInfoList planeInfos = new AirplaneDisplayInfoList();
  private Counter planeRedrawCounter;
  private Counter radarRedrawCounter;
  private final Event<Radar, Callsign> selectedAirplaneChangedEvent = new Event<>(this);
  private Callsign selectedCallsign;
  private final ISimulation simulation;
  private int simulationSecondListenerHandler = -1;
  private final RadarStyleSettings styleSettings;
  private boolean switchFlagTrue = false;
  private final TransformationLayer tl;

  public Radar(ICanvas<?> canvas, InitialPosition initialPosition,
               ISimulation sim,
               Area area,
               RadarStyleSettings styleSettings,
               RadarDisplaySettings displaySettings,
               RadarBehaviorSettings behaviorSettings) {
    assert canvas != null;
    assert initialPosition != null;
    assert sim != null;
    assert area != null;
    assert styleSettings != null;
    assert displaySettings != null;
    assert behaviorSettings != null;

    this.c = canvas;

    this.tl = new TransformationLayer(this.c, initialPosition.getCoordinate(), initialPosition.getRange());
    this.styleSettings = styleSettings;
    this.behaviorSettings = behaviorSettings;
    this.displaySettings = displaySettings;
    this.simulation = sim;
    this.area = area;
    this.simulation.registerMessageListener(
        this,
        new Messenger.ListenerAim(
            Participant.createAtc(this.simulation.getUserAtcId()),
            Messenger.eListenerDirection.receiver));

    buildLocalNavaidList();
    buildDrawnRoutesList();
    buildDrawnApproachesList();

    this.messageManager = new VisualisedMessageManager(
        this.styleSettings.displayTextDelay,
        (ParserFormatterStartInfo.Formatters<String>) this.simulation.getParserFormatterInfo().formatters);
    if (this.styleSettings.displayTextDelay > Global.REPEATED_SWITCH_REQUEST_SECONDS ||
        this.styleSettings.displayTextDelay > Global.REPEATED_RADAR_CONTACT_REQUEST_SECONDS) {
      simulation.getAppLog().write(ApplicationLog.eType.warning,
          "Radar message display interval in seconds (%d) is higher than plane repeated " +
              "radar-contact request interval (%d) or ATC repeated request switch interval (%d). " + "" +
              "The repetition messages will overlap.",
          this.styleSettings.displayTextDelay,
          Global.REPEATED_RADAR_CONTACT_REQUEST_SECONDS,
          Global.REPEATED_SWITCH_REQUEST_SECONDS);
    }

    sim.registerOnRunwayChanged(this::sim_runwayChanged);

    this.c.getMouseEvent().add(
        (sender, e) -> Radar.this.canvas_onMouseMove(sender, e));
    this.c.getPaintEvent().add(
        (c) -> Radar.this.canvas_onPaint(c));
    this.c.getKeyEvent().add(
        (c, o) -> Radar.this.canvas_onKeyPress(c, (KeyEventArg) o));
    this.c.getResizedEvent().add(o -> tl.resetPosition());
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

  public RadarDisplaySettings getDisplaySettings() {
    return displaySettings;
  }

  public Iterable<Approach> getDrawnApproaches() {
    return drawnApproaches;
  }

  public void setDrawnApproaches(Iterable<Approach> drawnApproaches) {
    this.drawnApproaches.clear();
    this.drawnApproaches.addMany(drawnApproaches);
  }

  public IReadOnlyList<DARoute> getDrawnRoutes() {
    return this.drawnRoutes;
  }

  public void setDrawnRoutes(Iterable<DARoute> drawnRoutes) {
    this.drawnRoutes.clear();
    this.drawnRoutes.addMany(drawnRoutes);
  }

  public EventSimple<Radar> getGotFocusEvent() {
    return gotFocusEvent;
  }

  public InitialPosition getPosition() {
    InitialPosition ret = InitialPosition.create(
        this.tl.getMiddle(),
        (int) this.tl.getWidthInNM());
    return ret;
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

  public RadarViewPort getViewPort() {
    return tl.getViewPort();
  }

  public void setViewPort(RadarViewPort viewPort) {
    tl.setViewPort(viewPort);
    redraw(true);
  }

  /**
   * Redraws radar. If redraw is caused by simulation second elapsing, it is not forced (it is normal).
   * Forced redraw occurs when e.g. radar canvas has changed its size or became visible.
   * Forced redraw will occur allays but will not update message positions.
   *
   * @param force True if radar should be redrawn not due to simulation second elapsed state.
   */
  public void redraw(boolean force) {
    if (force || radarRedrawCounter.increase()) { // only if forced or second has elapsed
      if (!force && planeRedrawCounter.increase()) { // only if a second has elapsed
        if (styleSettings.switchingPlaneAlternatingColor != null) switchFlagTrue = !switchFlagTrue;
        planeInfos.update(simulation.getPlanesToDisplay());
      }
      this.c.invokeRepaint();
    }
  }

  public void start(int redrawInterval, int planeRepositionInterval) {
    assert redrawInterval > 0;
    assert planeRepositionInterval > 0;
    this.planeRedrawCounter = new Counter(planeRepositionInterval);
    this.radarRedrawCounter = new Counter(redrawInterval);
    // listen to simulation seconds for redraw
    this.simulationSecondListenerHandler = this.simulation.registerOnSecondElapsed((s) -> redraw(false));
  }

  public void stop() {
    this.simulation.unregisterOnSecondElapsed(this.simulationSecondListenerHandler);
  }

  public void zoomIn() {
    zoomBy(0.9);
  }

  public void zoomOut() {
    zoomBy(1.1);
  }

  private void buildDrawnApproachesList() {
    this.drawnApproaches.clear();
    RunwayConfiguration rc =
        simulation.getRunwayConfigurationInUse();

    IList<Approach> approachesToDraw =
        rc.getArrivals()
            .where(q -> q.isShowApproach())
            .select(q -> q.getThreshold().tryGetHighestApproachExceptVisuals());

    approachesToDraw.remove(q -> q == null);
    this.drawnApproaches.addMany(approachesToDraw);
  }

  private void buildDrawnRoutesList() {
    this.drawnRoutes.clear();
    RunwayConfiguration rc = simulation.getRunwayConfigurationInUse();
    rc.getArrivals()
        .where(q -> q.isShowRoutes())
        .forEach(q -> this.drawnRoutes.addMany(
            q.getThreshold().getRoutes().where(p -> p.getType() != DARouteType.sid)));
    rc.getDepartures()
        .where(q -> q.isShowRoutes())
        .forEach(q -> this.drawnRoutes.addMany(
            q.getThreshold().getRoutes().where(p -> p.getType() == DARouteType.sid)));
  }

  private void buildLocalNavaidList() {

    for (Navaid navaid : area.getNavaids()) {
      NavaidDisplayInfo ndi = new NavaidDisplayInfo();
      ndi.navaid = navaid;
      ndi.isRoute = false;
      this.navaids.add(ndi);
    }

    IReadOnlyList<ActiveRunwayThreshold> rts =
        simulation.getRunwayConfigurationInUse().getArrivals()
            .where(q -> q.isShowRoutes())
            .select(q -> q.getThreshold());
    for (ActiveRunwayThreshold rt : rts) {
      for (DARoute route : rt.getRoutes().where(q -> q.getType() != DARouteType.sid)) {
        for (Navaid navaid : route.getRouteNavaids()) {
          NavaidDisplayInfo ndi = this.navaids.getByNavaid(navaid);
          ndi.isRoute = true;
        }
      }
    }
    for (ActiveRunwayThreshold rt : rts) {
      for (DARoute route : rt.getRoutes().where(q -> q.getType() == DARouteType.sid)) {
        for (Navaid navaid : route.getRouteNavaids()) {
          NavaidDisplayInfo ndi = this.navaids.getByNavaid(navaid);
          ndi.isRoute = true;
        }
      }
    }
  }

  private String buildPlaneString(String lineFormat, AirplaneDisplayInfo adi) {
    String ret = adi.format(lineFormat);
    return ret;
  }

  private void canvas_onKeyPress(ICanvas<?> sender, KeyEventArg e) {
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

  private void canvas_onMouseMove(ICanvas<?> sender, EMouseEventArg e) {
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
          AirplaneDisplayInfo plane = tryGetAirplaneDisplayInfoByPoint(e.getPoint());
          if (plane == null)
            this.setSelectedCallsign(null);
          else if (this.selectedCallsign == plane.callsign)
            this.setSelectedCallsign(null);
          else
            this.setSelectedCallsign(plane.callsign);
          this.redraw(true);
        }
        this.mouseClickEvent.raise(new WithCoordinateEventArg(coord));
        break;
      case doubleClick:
        if (e.modifiers.is(false, true, false) && e.button == EMouseEventArg.eButton.left) {
          // recenter plane label
          AirplaneDisplayInfo adi = tryGetAirplaneDisplayInfoByPoint(e.getPoint());
          if (adi != null)
            adi.setDefaultLabelPosition();
        } else {
          centerAt(coord);
        }
        break;
      case move:
        this.mouseMoveEvent.raise(new WithCoordinateEventArg(coord));
        break;
      case dragged:
        if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.right) {
          coord = tl.toCoordinateDelta(e.getDropRangePoint());
          moveMapBy(coord);
        } else {
          if (infoLine != null) this.infoLine = null;

          if (e.modifiers.is(false, true, false) && e.button == EMouseEventArg.eButton.left) {
            AirplaneDisplayInfo adi = tryGetAirplaneDisplayInfoByPoint(e.getPoint());
            if (adi != null) {
              adi.labelShift = e.getDropRangePoint();
            }
          }
          this.redraw(true);
        }
        break;
      case dragging:
        if (e.modifiers.is(false, false, false) && e.button == EMouseEventArg.eButton.left) {
          Coordinate fromCoordinate = tl.toCoordinate(e.getPoint());
          Coordinate toCoordinate = tl.toCoordinate(e.getDropPoint());
          Double relativeSpeed = tryGetRelativeSpeed(fromCoordinate);
          infoLine = new InfoLine(fromCoordinate, toCoordinate, relativeSpeed);
          this.redraw(true);
        }
        break;
    }

  }

  private void canvas_onPaint(ICanvas<?> sender) {
    if (tl.isReady() == false) {
      drawBackground();
      return;
    }
    c.beforeDraw();
    drawBackground();
    drawBorders();
    drawRoutes();
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

  private MessageSet createMessageSet(List<VisualisedMessage> msgs) {
    MessageSet ret = new MessageSet();

    for (VisualisedMessage m : msgs) {
      if (m.getMessage().getSource().getType() == Participant.eType.system) {
        ret.system.add(">> " + m.getMessageText());
      } else if (m.getMessage().getSource().getType() == Participant.eType.atc) {
        ret.atc.add("[" + m.getMessage().getSource().getId() + "] " + m.getMessageText());
      } else if (m.getMessage().getSource().getType() == Participant.eType.airplane) {
        ret.plane.add(m.getMessage().getSource().getId() + ": " + m.getMessageText());
      } else {
        throw new UnsupportedOperationException();
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

  private void drawAirplanes() {
    if (this.planeInfos.isEmpty()) return;

    RadarStyleSettings.TextSettings dt = styleSettings.callsign;
    Size s = c.getEstimatedTextSize(dt.getFont(), 12, 3);
    tl.adjustPlaneLabelOverlying(s.width, s.height);

    // first draw non-user controlled planes
    for (AirplaneDisplayInfo adi : this.planeInfos.getList()) {
      if (isUserControlledPlane(adi)) continue;
      drawPlanePoint(adi);
    }
    for (AirplaneDisplayInfo adi : this.planeInfos.getList()) {
      if (isUserControlledPlane(adi)) continue;
      drawPlaneLabel(adi);
    }
    // over them draw user-controlled planes
    for (AirplaneDisplayInfo adi : this.planeInfos.getList()) {
      if (!isUserControlledPlane(adi)) continue;
      drawPlanePoint(adi);
    }
    for (AirplaneDisplayInfo adi : this.planeInfos.getList()) {
      if (!isUserControlledPlane(adi)) continue;
      drawPlaneLabel(adi);
    }

    boolean isFullAirprox = this.planeInfos.getList().isAny(
        p -> p.airprox == AirproxType.full);
    if (isFullAirprox)
      SoundManager.playAirprox();
  }

  private void drawAirport(Airport a) {
    for (InactiveRunway r : a.getInactiveRunways()) {
      drawInactiveRunway(r);
    }

    for (ActiveRunway r : a.getRunways()) {
      drawRunway(r);
    }
  }

  private void drawAirports() {
    for (Airport a : area.getAirports()) {
      drawAirport(a);
    }
  }

  private void drawApproach(Approach approach) {
    RadarStyleSettings.ColorWidthLengthSettings dispSett;
    if (approach.getType().isILS())
      dispSett = styleSettings.ilsApproach;
    else if (approach.getType() == ApproachType.gnss)
      dispSett = styleSettings.gnssApproach;
    else if (approach.getType() == ApproachType.visual)
      return;
    else if (approach.getType() == ApproachType.vor) {
      dispSett = styleSettings.vorApproach;
    } else if (approach.getType() == ApproachType.ndb) {
      dispSett = styleSettings.ndbApproach;
    } else
      throw new EApplicationException("Not supported");
    Coordinate start = Coordinates.getCoordinate(
        approach.getParent().getCoordinate(),
        Headings.getOpposite(approach.getGeographicalRadial()),
        dispSett.getLength());
    tl.drawLine(start, approach.getParent().getCoordinate(), dispSett.getColor(), dispSett.getWidth());
  }

  private void drawApproaches() {
    if (displaySettings.isApproachesVisible() == false) return;
    for (Approach drawnApproach : this.drawnApproaches) {
      drawApproach(drawnApproach);
    }
  }

//  private void drawArc(BorderExactPoint bPrev, BorderArcPoint borderArcPoint, BorderExactPoint bNext, Color color) {
//    double startBear = Coordinates.getBearing(borderArcPoint.getCoordinate(), bPrev.getCoordinate());
//    double endBear = Coordinates.getBearing(borderArcPoint.getCoordinate(), bNext.getCoordinate());
//    double distance = Coordinates.getDistanceInNM(borderArcPoint.getCoordinate(), bPrev.getCoordinate());
//    if (borderArcPoint.getDirection() == BorderArcPoint.eDirection.counterclockwise) {
//      double tmp = startBear;
//      startBear = endBear;
//      endBear = tmp;
//    }
//
//    tl.drawArc(borderArcPoint.getCoordinate(), startBear, endBear, distance, color);
//  }

  private void drawBackground() {
    Color color = styleSettings.mapBackcolor;
    tl.clear(color);
  }

  private void drawBorder(Border border) {

    RadarStyleSettings.ColorWidthSettings ds = getDispSettBy(border);

    Coordinate last = null;
    for (int i = 0; i < border.getPoints().size(); i++) {
      BorderPoint bep = border.getPoints().get(i);
      if (last != null) {
        tl.drawLine(last, bep.getCoordinate(), ds.getColor(), ds.getWidth());
      }
      last = bep.getCoordinate();
    }
  }

  private void drawBorderAltitude(Border border) {
    String maxS =
        border.getMaxAltitude() == BORDER_ALTITUDE_MAXIMUM_VALUE ?
            null
            :
            Format.Altitude.toAlfOrFLLong(border.getMaxAltitude());
    String minS =
        border.getMinAltitude() == BORDER_ALTITUDE_MINIMUM_VALUE ?
            null
            :
            Format.Altitude.toAlfOrFLLong(border.getMinAltitude());
    if (minS == null && maxS == null) return;
    RadarStyleSettings.ColorWidthFontSettings ds = (RadarStyleSettings.ColorWidthFontSettings) getDispSettBy(border);
    tl.drawAltitudeRangeText(
        border.getLabelCoordinate(),
        minS, maxS,
        0, 0, ds.getFont(), ds.getColor());
  }

  private void drawBorders() {
    for (Border b : area.getBorders()) {
      switch (b.getType()) {
        case tma:
          if (displaySettings.isTmaBorderVisible())
            drawBorder(b);
          break;
        case ctr:
          if (displaySettings.isCtrBorderVisible())
            drawBorder(b);
          break;
        case country:
          if (displaySettings.isCountryBorderVisible())
            drawBorder(b);
          break;
        case restricted:
        case danger:
          if (displaySettings.isRestrictedBorderVisible()) {
            drawBorder(b);
            //drawBorderCaption(b);
            if (displaySettings.isMrvaBorderAltitudeVisible()) {
              drawBorderAltitude(b);
            }
          }
          break;
        case mrva:
          if (displaySettings.isMrvaBorderVisible()) {
            drawBorder(b);
            if (displaySettings.isMrvaBorderAltitudeVisible()) {
              drawBorderAltitude(b);
            }
          }
          break;
        default:
          drawBorder(b);
      }
    }
  }

  private void drawCaptions() {
    IList<Message> msgs = this.simulation.getMessages(this);

    for (Message msg : msgs) {
      messageManager.add(msg);
    }

    boolean containsSystemMessage =
        msgs.isAny(q -> q.getSource().getType() == Participant.eType.system);

    IList<Message> atcMsgs = msgs.where(q -> q.getSource().getType() == Participant.eType.atc);
    boolean containsAtcMessage = atcMsgs.isEmpty() == false;
    boolean isAtcMessageNegative = atcMsgs.isAny(q -> q.getContent().isRejection());

    IList<Message> planeMsgs = msgs.where(q -> q.getSource().getType() == Participant.eType.airplane);
    boolean containsPlaneMessage = planeMsgs.isEmpty() == false;
    boolean isPlaneMessageNegative = planeMsgs.isAny(q -> q.getContent().isRejection());

    if (containsAtcMessage) {
      SoundManager.playAtcNewMessage(isAtcMessageNegative);
    } else if (containsPlaneMessage) {
      SoundManager.playPlaneNewMessage(isPlaneMessageNegative);
    } else if (containsSystemMessage) {
      SoundManager.playSystemMessage();
    }

    drawMessages(messageManager.getCurrent());

    messageManager.decreaseMessagesLifeCounter();
  }

  private void drawInactiveRunway(InactiveRunway runway) {
    RadarStyleSettings.ColorWidthSettings ds = getDispSettBy(runway);

    tl.drawLine(
        runway.getThresholdA().getCoordinate(),
        runway.getThresholdB().getCoordinate(),
        ds.getColor(), ds.getWidth());
  }

  private void drawInfoLine() {
    if (this.infoLine != null) {

      RadarStyleSettings.ColorWidthFontSettings cwfs = this.styleSettings.infoLine;

      tl.drawLine(
          this.infoLine.from,
          this.infoLine.to,
          cwfs.getColor(),
          cwfs.getWidth()
      );

      String distS = String.format("%03d %.1fnm", this.infoLine.heading, this.infoLine.distanceInNm);
      String timeS;
      if (this.infoLine.isRelativeSpeedUsed == false)
        timeS = String.format("%s:%s/%s:%s/%s:%s",
            InfoLine.toIntegerMinutes(this.infoLine.seconds280),
            InfoLine.toIntegerSeconds(this.infoLine.seconds280),
            InfoLine.toIntegerMinutes(this.infoLine.seconds250),
            InfoLine.toIntegerSeconds(this.infoLine.seconds250),
            InfoLine.toIntegerMinutes(this.infoLine.seconds200),
            InfoLine.toIntegerSeconds(this.infoLine.seconds200));
      else
        timeS = String.format("%s:%s",
            InfoLine.toIntegerMinutes(this.infoLine.secondsSpeed),
            InfoLine.toIntegerSeconds(this.infoLine.secondsSpeed));


      tl.drawText(distS, this.infoLine.to,
          3, -cwfs.getFont().getSize() * 2 - 3,
          cwfs.getFont(), cwfs.getColor());

      tl.drawText(timeS, this.infoLine.to,
          3, -cwfs.getFont().getSize(),
          cwfs.getFont(), cwfs.getColor());
    }
  }

  private void drawMessages(List<VisualisedMessage> msgs) {
    MessageSet ms = createMessageSet(msgs);

    RadarStyleSettings.TextSettings dt;

    dt = styleSettings.atc;
    tl.drawTextBlock(ms.atc, TextBlockLocation.bottomRight, dt.getFont(), dt.getColor());

    dt = styleSettings.plane;
    tl.drawTextBlock(ms.plane, TextBlockLocation.bottomLeft, dt.getFont(), dt.getColor());

    dt = styleSettings.system;
    tl.drawTextBlock(decodeSystemMultilines(ms.system), TextBlockLocation.topRight, dt.getFont(), dt.getColor());
  }

  private void drawNavaid(Navaid navaid) {
    RadarStyleSettings.ColorWidthBorderSettings ds = getDispSettBy(navaid);
    RadarStyleSettings.TextSettings dt = styleSettings.navaid;

    switch (navaid.getType()) {
      case vor:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawCircleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
      case ndb:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawTriangleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
      case fix:
      case fixMinor:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 0, dt.getFont(), ds.getColor());
        break;
      case airport:
        tl.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        tl.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
    }

  }

  private void drawNavaids() {
    for (NavaidDisplayInfo ndi : this.navaids) {
      switch (ndi.navaid.getType()) {
        case ndb:
          if (displaySettings.isNdbVisible()) drawNavaid(ndi.navaid);
          break;
        case airport:
          if (displaySettings.isAirportVisible()) drawNavaid(ndi.navaid);
          break;
        case vor:
          if (displaySettings.isVorVisible()) drawNavaid(ndi.navaid);
          break;
        case fix:
        case fixMinor:
          boolean isVisible = false;
          if (ndi.navaid.getType() == Navaid.eType.fix && displaySettings.isFixVisible())
            isVisible = true;
          if (ndi.navaid.getType() == Navaid.eType.fixMinor && displaySettings.isFixMinorVisible())
            isVisible = true;
          if (ndi.isRoute && displaySettings.isFixRouteVisible())
            isVisible = true;
          if (isVisible) drawNavaid(ndi.navaid);
          break;
        case auxiliary:
          // not drawn
          break;
        default:
          drawNavaid(ndi.navaid);
      }
    }
  }

  private void drawPlaneLabel(AirplaneDisplayInfo adi) {
    RadarStyleSettings.PlaneLabelSettings dp = getPlaneLabelDisplaySettingsBy(adi);
    if (dp.isVisible() == false) {
      return;
    }
    RadarStyleSettings.TextSettings dt = styleSettings.callsign;

    Color c = resolvePlaneDrawColor(adi, dp);
    Color cc = dp.getConnectorColor();

    // plane label
    StringBuilder sb = new StringBuilder();
    sb.append(buildPlaneString(dp.getFirstLineFormat(), adi));
    if (isAirplaneUnderConfirmedSwitch(adi)) {
      sb.append("*");
    }
    sb.append("\r\n");
    sb.append(buildPlaneString(dp.getSecondLineFormat(), adi));
    sb.append("\r\n");
    sb.append(buildPlaneString(dp.getThirdLineFormat(), adi));

    // if is of ATC, intelligent drawing with respect to the other radar labels
    // silly drawing otherwise
    if (adi.tunedAtc.getType() == AtcType.app)
      tl.drawPlaneLabel(sb.toString(), adi.fixedLabelShift, adi.coordinate, adi.labelShift, dt.getFont(), c, cc);
    else
      tl.drawText(sb.toString(), adi.coordinate, adi.labelShift.x, adi.labelShift.y, dt.getFont(), c);
  }

  private void drawPlanePoint(AirplaneDisplayInfo adi) {

    RadarStyleSettings.PlaneLabelSettings dp = getPlaneLabelDisplaySettingsBy(adi);
    if (dp.isVisible() == false) {
      return;
    }

    // eval special color for airproxes and selected plane
    Color c = resolvePlaneDrawColor(adi, dp);

    // plane dot and direction line
    tl.drawPlanePoint(adi.coordinate, c, dp.getPointWidth()); // point of plane
    if (this.displaySettings.isPlaneHeadingLineVisible()) {
      double len = adi.tas * dp.getHeadingLineLength() / 3600d;
      tl.drawLineByHeadingAndDistance(adi.coordinate, adi.heading, len, c, 1);
    }

    // separation ring
    if (displaySettings.isRingsVisible()) {
      if (adi.altitude > this.simulation.getAirport().getAltitude()) {
        tl.drawCircleAroundInNM(adi.coordinate, dp.getSeparationRingRadius(),
            c, 1);
      }
    }

    if (this.displaySettings.isPlaneHistoryVisible()) {
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
  }

  private void drawRoute(IReadOnlyList<Navaid> navaidPoints, RadarStyleSettings.ColorWidthSettings sett) {
    for (int i = 0; i < navaidPoints.size() - 1; i++) {
      tl.drawLine(
          navaidPoints.get(i).getCoordinate(),
          navaidPoints.get(i + 1).getCoordinate(),
          sett.getColor(),
          sett.getWidth());
    }
  }

  private void drawRoutes() {
    for (DARoute route : drawnRoutes) {
      if (route.getRouteNavaids().isEmpty()) continue;
      switch (route.getType()) {
        case sid:
          if (!displaySettings.isSidVisible()) continue;
          for (ActiveRunwayThreshold runwayThreshold : this.simulation.getRunwayConfigurationInUse()
              .getDepartures()
              .select(q -> q.getThreshold())
              .where(q -> q.getRoutes().contains(route))) {
            drawSidIntro(runwayThreshold.getOtherThreshold().getCoordinate(), route.getRouteNavaids().getFirst());
          }
          drawRoute(route.getRouteNavaids(), styleSettings.sid);
          break;
        case star:
        case transition:
          if (!displaySettings.isStarVisible()) continue;
          drawRoute(route.getRouteNavaids(), styleSettings.star);
          break;
        default:
          throw new EEnumValueUnsupportedException(route.getType());
      }
    }
  }

  private void drawRunway(ActiveRunway runway) {
    RadarStyleSettings.ColorWidthSettings ds = getDispSettBy(runway);

    tl.drawLine(
        runway.getThresholdA().getCoordinate(),
        runway.getThresholdB().getCoordinate(),
        ds.getColor(), ds.getWidth());
  }

  private void drawSidIntro(Coordinate thresholdCoordinate, Navaid firstNavaid) {
    RadarStyleSettings.ColorWidthSettings sett = styleSettings.sid;
    tl.drawLine(
        thresholdCoordinate,
        firstNavaid.getCoordinate(),
        sett.getColor(),
        sett.getWidth());
  }

  private void drawTime() {

    // todo rewritten, check
    //TODO rewrite using my list?
    RadarStyleSettings.TextSettings dt = styleSettings.time;
    List<String> lst = new ArrayList<>(1);
    lst.add(simulation.getNow().toTimeString());
    tl.drawTextBlock(lst, TextBlockLocation.topLeft, dt.getFont(), dt.getColor());
  }

  // never used, commented
//  private String formatToVisualSentence(List<String> sentences) {
//    EStringBuilder ret = new EStringBuilder();
//    for (int i = 0; i < sentences.size(); i++) {
//      String sentence = sentences.get(i);
//      if (sentence.trim().length() == 0) continue;
//      if (i == 0)
//        sentence = makeBeginSentence(sentence);
//      else
//        ret.append(", ");
//      ret.append(sentence);
//    }
//    ret.append(".");
//    return ret.toString();
//  }

  private RadarStyleSettings.ColorWidthSettings getDispSettBy(Border border) {
    switch (border.getType()) {
      case ctr:
        return styleSettings.borderCtr;
      case country:
        return styleSettings.borderCountry;
      case tma:
        return styleSettings.borderTma;
      case mrva:
        return styleSettings.borderMrva;
      case restricted:
      case danger:
        return styleSettings.borderRestricted;
      default:
        throw new EEnumValueUnsupportedException(border.getType());
    }
  }

  private RadarStyleSettings.ColorWidthSettings getDispSettBy(ActiveRunway runway) {
    return styleSettings.activeRunway;
  }

  private RadarStyleSettings.ColorWidthSettings getDispSettBy(InactiveRunway runway) {
    return styleSettings.closedRunway;
  }

  private RadarStyleSettings.ColorWidthBorderSettings getDispSettBy(Navaid navaid) {
    switch (navaid.getType()) {
      case fix:
        return styleSettings.navFix;
      case fixMinor:
        return styleSettings.navFixMinor;
      case ndb:
        return styleSettings.navNDB;
      case vor:
        return styleSettings.navVOR;
      case airport:
        return styleSettings.navAirport;
      default:
        throw new EEnumValueUnsupportedException(navaid.getType());
    }
  }

//  private String getMessageContentAsString(Message msg) {
//    String ret;
//    if (msg.isSourceOfType(Airplane.class)) {
//      if (msg.isContentOfType(IList.class)) {
//        List<String> sentences = new ArrayList();
//        SpeechList<ISpeech> lst = msg.getContent();
//        for (ISpeech iSpeech : lst) {
//          String sentence = behaviorSettings.getFormatter().format(iSpeech);
//          if (sentence == null || sentence.trim().length() == 0) continue;
//          sentences.add(sentence);
//        }
//        ret = formatToVisualSentence(sentences);
//      } else {
//        ISpeech sp = msg.getContent();
//        ret = behaviorSettings.getFormatter().format(sp);
//      }
//    } else if (msg.isSourceOfType(Atc.class)) {
//      if (msg.isContentOfType(PlaneSwitchMessage.class)) {
//        PlaneSwitchMessage psm = msg.getContent();
//        ret = behaviorSettings.getFormatter().format(msg.getSource(), psm);
//      } else if (msg.isContentOfType(StringResponse.class)) {
//        ret = msg.<StringResponse>getContent().text;
//      } else if (msg.isContentOfType(StringMessageContent.class)) {
//        ret = msg.<StringMessageContent>getContent().getMessageText();
//      } else {
//        throw new UnsupportedOperationException();
//      }
//    } else {
//      // system messages
//      ret = msg.<StringMessageContent>getContent().getMessageText();
//    }
//    return ret;
//  }

  private RadarStyleSettings.PlaneLabelSettings getPlaneLabelDisplaySettingsBy(AirplaneDisplayInfo adi) {
    RadarStyleSettings.PlaneLabelSettings ret;

    if (adi.emergency)
      ret = styleSettings.emergency;
    else if (adi.ias == 0)
      ret = styleSettings.stopped;
    else if (adi.responsibleAtc.getType() == AtcType.app)
      ret = styleSettings.app;
    else if (adi.responsibleAtc.getType() == AtcType.twr)
      ret = styleSettings.twr;
    else if (adi.responsibleAtc.getType() == AtcType.ctr)
      ret = styleSettings.ctr;
    else
      throw new UnsupportedOperationException();

    return ret;
  }

  private boolean isAirplaneUnderConfirmedSwitch(AirplaneDisplayInfo adi) {
    return adi.isConfirmedSwitch && adi.altitude > this.simulation.getAirport().getAltitude();
  }

  private boolean isUserControlledPlane(AirplaneDisplayInfo adi) {
    boolean ret = adi.tunedAtc.getType() == AtcType.app;
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

  private void moveMapBy(Coordinate c) {
    tl.setPosition(
        tl.getTopLeft().add(c));
    redraw(true);
  }

  private Color resolvePlaneDrawColor(AirplaneDisplayInfo adi, RadarStyleSettings.PlaneLabelSettings dp) {
    Color c = dp.getColor();
    if (adi.airprox == AirproxType.full) {
      c = styleSettings.airproxFull;
    } else if (adi.airprox == AirproxType.partial) {
      c = styleSettings.airproxPartial;
    } else if (adi.mrvaError) {
      c = styleSettings.mrvaError;
    } else if (adi.airprox == AirproxType.warning) {
      c = styleSettings.airproxWarning;
    } else if (switchFlagTrue && isAirplaneUnderConfirmedSwitch(adi)) {
      c = styleSettings.switchingPlaneAlternatingColor;
    } else if (this.selectedCallsign == adi.callsign) {
      c = styleSettings.selected.getColor();
    }
    return c;
  }

  private void sim_runwayChanged(ISimulation simulation) {
    buildLocalNavaidList();
    buildDrawnRoutesList();
    buildDrawnApproachesList();
  }

  private AirplaneDisplayInfo tryGetAirplaneDisplayInfoByPoint(Point p) {
    Coordinate c = tl.toCoordinate(p);
    return tryGetSelectedAirplane(c);
  }

  private Double tryGetRelativeSpeed(Coordinate fromCoordinate) {
    AirplaneDisplayInfo adi = null;
    double dist = Double.MAX_VALUE;
    for (AirplaneDisplayInfo radi : this.planeInfos.getList()) {
      double rdist = Coordinates.getDistanceInNM(radi.coordinate, fromCoordinate);
      if (rdist < dist) {
        dist = rdist;
        adi = radi;
      }
    }
    if (dist > 3)
      adi = null;
    if (adi != null)
      return (double) adi.tas;
    else
      return null;
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
}
