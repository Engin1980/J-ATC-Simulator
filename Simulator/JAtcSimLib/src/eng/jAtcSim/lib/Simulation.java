/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib;

import eng.eSystem.ERandom;
import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.EventSimple;
import eng.eSystem.exceptions.EApplicationException;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.airplanes.*;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.atcs.*;
import eng.jAtcSim.lib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.managers.EmergencyManager;
import eng.jAtcSim.lib.managers.MrvaManager;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.newStats.StatsManager;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.traffic.Movement;
import eng.jAtcSim.lib.traffic.TrafficManager;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherManager;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.Border;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marek
 */
public class Simulation {

  public static final ERandom rnd = new ERandom();
  private static final boolean DEBUG_STYLE_TIMER = false;
  private static final String SYSMES_COMMANDS = "?";
  private static final Pattern SYSMES_CHANGE_SPEED = Pattern.compile("TICK (\\d+)");
  private static final Pattern SYSMES_METAR = Pattern.compile("METAR");
  private static final Pattern SYSMES_REMOVE = Pattern.compile("REMOVE (\\d{4})");
  private static final Pattern SYSMES_SHORTCUT = Pattern.compile("SHORTCUT ([\\\\?A-Z0-9]+)( (.+))?");
  private final ETime now;
  private final Messenger messenger = new Messenger();
  private final IList<Airplane> newPlanesDelayedToAvoidCollision = new EList<>();
  private final WeatherManager weatherManager;
  private final StatsManager stats;
  private final EmergencyManager emergencyManager;
  private final TrafficManager trafficManager;
  private final PlaneResponsibilityManager prm;
  private final Area area;
  private final AirplaneTypes airplaneTypes;
  private final Fleets fleets;
  private final UserAtc appAtc;
  private final TowerAtc twrAtc;
  private final CenterAtc ctrAtc;
  private final MrvaManager mrvaManager;
  private final Airport activeAirport;
  /**
   * Public event informing surrounding about elapsed second.
   */
  @XmlIgnore
  private final EventSimple<Simulation> onSecondElapsed =
      new EventSimple<>(this);
  @XmlIgnore
  private final EventSimple<Simulation> onRunwayChanged =
      new EventSimple<>(this);
  private int simulationSecondLengthInMs;
  @XmlIgnore
  private boolean isBusy = false;
  /**
   * Internal timer used to make simulation ticks.
   */
  @XmlIgnore
  private final Timer tmr = new Timer(o -> Simulation.this.elapseSecond());

  public Simulation(
      Area area, AirplaneTypes airplaneTypes, Fleets fleets, Traffic traffic, Airport activeAirport,
      WeatherProvider weatherProvider, ETime now, int simulationSecondLengthInMs, double emergencyPerDayProbability,
      TrafficManager.TrafficManagerSettings trafficManagerSettings, int statsSnapshotDistanceInMinutes) {

    if (area == null) {
      throw new IllegalArgumentException("Value of {area} cannot not be null.");
    }
    if (airplaneTypes == null) {
      throw new IllegalArgumentException("Value of {airplaneTypes} cannot not be null.");
    }
    if (fleets == null) {
      throw new IllegalArgumentException("Value of {fleets} cannot not be null.");
    }
    if (traffic == null) {
      throw new IllegalArgumentException("Value of {traffic} cannot not be null.");
    }
    if (weatherProvider == null) {
      throw new IllegalArgumentException("Value of {weatherProvider} cannot not be null.");
    }
    if (now == null) {
      throw new IllegalArgumentException("Value of {now} cannot not be null.");
    }
    if (activeAirport == null) {
      throw new IllegalArgumentException("Value of {activeAirport} cannot not be null.");
    }
    if (trafficManagerSettings == null) {
      throw new IllegalArgumentException("Value of {trafficManagerSettings} cannot not be null.");
    }

    this.now = now.clone();
    this.simulationSecondLengthInMs = simulationSecondLengthInMs;

    this.area = area;
    this.airplaneTypes = airplaneTypes;
    this.fleets = fleets;
    this.weatherManager = new WeatherManager(weatherProvider);

    this.activeAirport = activeAirport;
    this.twrAtc = new TowerAtc(this.activeAirport.getAtcTemplates().getFirst(q -> q.getType() == Atc.eType.twr));
    this.twrAtc.getOnRunwayChanged().add(this::twr_runwayChanged);
    this.ctrAtc = new CenterAtc(this.activeAirport.getAtcTemplates().getFirst(q -> q.getType() == Atc.eType.ctr));
    this.appAtc = new UserAtc(this.activeAirport.getAtcTemplates().getFirst(q -> q.getType() == Atc.eType.app));

    this.prm = new PlaneResponsibilityManager();

    this.emergencyManager = new EmergencyManager(emergencyPerDayProbability);
    this.emergencyManager.generateEmergencyTime(this.now);

    this.trafficManager = new TrafficManager(trafficManagerSettings, traffic);

    IList<Border> mrvaAreas =
        area.getBorders().where(q -> q.getType() == Border.eType.mrva);
    this.mrvaManager = new MrvaManager(mrvaAreas);

    this.stats = new StatsManager(statsSnapshotDistanceInMinutes);
  }

  public EventSimple<Simulation> getOnRunwayChanged() {
    return onRunwayChanged;
  }

  public void load(XElement root) {

    LoadSave.setRelativeArea(this.area, this.activeAirport, new Atc[]{this.twrAtc, this.ctrAtc, this.appAtc});
    LoadSave.setRelativeAirplaneTypes(this.airplaneTypes);

    {
      IList<Airplane> lst = new EList<>();
      XElement tmp = root.getChildren().getFirst(q -> q.getName().equals("planes"));
      for (XElement elm : tmp.getChildren()) {
        Airplane plane = Airplane.load(elm);
        lst.add(plane);
      }
      LoadSave.setRelativeAirplanes(lst);
    }

    {
      XElement tmp = root.getChild("atcs");
      this.ctrAtc.load(tmp);
      this.appAtc.load(tmp);
      this.twrAtc.load(tmp);
      this.twrAtc.getRunwayConfigurationInUse().bind();
      if (this.twrAtc.tryGetRunwayConfigurationScheduled() != null)
        this.twrAtc.tryGetRunwayConfigurationScheduled().bind();
      this.twrAtc.getOnRunwayChanged().add(this::twr_runwayChanged);
    }

    LoadSave.loadField(root, this, "prm");

    LoadSave.loadField(root, this, "now");
    this.stats.load(root);
    LoadSave.loadField(root, this, "emergencyManager");
    this.trafficManager.load(root);

    LoadSave.loadField(root, this, "simulationSecondLengthInMs");

    {
      IList<Airplane> lst = new EList<>();
      XElement tmp = root.getChildren().getFirst(q -> q.getName().equals("delayedPlanes"));
      for (XElement elm : tmp.getChildren()) {
        Airplane plane = Airplane.load(elm);
        this.newPlanesDelayedToAvoidCollision.add(plane);
      }
      LoadSave.setRelativeAirplanes(lst);
    }

    this.prm.getPlanes().forEach(q -> this.mrvaManager.registerPlane(q));
    this.prm.init();

    this.prm.getPlanes().forEach(
        q->this.messenger.registerListener(q, q));
  }

  public void setSimulationSecondInterval(int intervalMs) {
    if (intervalMs < 0)
      throw new EApplicationException("Interval " + intervalMs + " to be set as second length interval must be greater than 0.");
    this.simulationSecondLengthInMs = intervalMs;
    this.stop();
    this.start();
  }

  public PlaneResponsibilityManager getPrm() {
    return prm;
  }

  public void init() {
    Acc.setSimulation(this);
    Acc.setAirport(this.activeAirport);
    this.weatherManager.init();
    Acc.atcTwr().init();
    Acc.atcApp().init();
    Acc.atcCtr().init();
    this.prm.init();
    this.stats.init();

    Acc.messenger().registerListener(Acc.atcTwr(), Acc.atcTwr());
    Acc.messenger().registerListener(Acc.atcCtr(), Acc.atcCtr());
    Acc.messenger().registerListener(Acc.messenger().SYSTEM, Acc.messenger().SYSTEM);

    trafficManager.generateNewTrafficIfRequired();
    trafficManager.throwOutElapsedMovements(this.now.addMinutes(-5));
  }

  public EventSimple<Simulation> getOnSecondElapsed() {
    return onSecondElapsed;
  }

  //TODO shouldn't this be private?
  public AirplaneTypes getAirplaneTypes() {
    return airplaneTypes;
  }

  public IReadOnlyList<Movement> getScheduledMovements() {
    IReadOnlyList<Movement> ret;
    ret = trafficManager.getScheduledMovements();
    return ret;
  }

  public TrafficManager getTrafficManager() {
    return trafficManager;
  }

  public Airport getActiveAirport() {
    return activeAirport;
  }

  public String toAltitudeString(double altInFt, boolean appendFt) {
    if (altInFt > getActiveAirport().getTransitionAltitude()) {
      return String.format("FL%03d", ((int) altInFt) / 100);
    } else {
      if (appendFt) {
        return String.format("%d ft", (int) altInFt);
      } else {
        return String.format("%d", (int) altInFt);
      }
    }
  }

  public ETime getNow() {
    return now;
  }

  public Fleets getFleets() {
    return fleets;
  }

  public IReadOnlyList<Airplane> getAirplanes() {
    return Acc.prm().getPlanes();
  }

  public Messenger getMessenger() {
    return messenger;
  }

  public void start() {
    if (this.tmr.isRunning() == false) {
      this.tmr.start(this.simulationSecondLengthInMs); // initial speed 1sec
    }
  }

  public void stop() {
    this.tmr.stop();
  }

  public boolean isRunning() {
    return this.tmr.isRunning();
  }

  public IReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
    return Acc.prm().getPlanesToDisplay();
  }

  public Atc getResponsibleAtc(Airplane plane) {
    return Acc.prm().getResponsibleAtc(plane);
  }

  public Weather getWeather() {
    return this.weatherManager.getWeather();
  }

  public UserAtc getAppAtc() {
    return appAtc;
  }

  public TowerAtc getTwrAtc() {
    return twrAtc;
  }

  public CenterAtc getCtrAtc() {
    return ctrAtc;
  }

  public StatsManager getStats() {
    return stats;
  }

  public void sendTextMessageForUser(String text) {
    Message m = new Message(Messenger.SYSTEM, appAtc,
        new StringMessageContent(text));
    this.messenger.send(m);
  }

  public Area getArea() {
    return area;
  }

  public void save(XElement root) {

    {
      IReadOnlyList<Airplane> planes = this.prm.getPlanes();
      XElement tmp = new XElement("planes");
      root.addElement(tmp);
      for (Airplane plane : planes) {
        XElement pln = new XElement("plane");
        plane.save(pln);
        tmp.addElement(pln);
      }
    }

    {
      XElement tmp = new XElement("atcs");
      root.addElement(tmp);
      this.ctrAtc.save(tmp);
      this.twrAtc.save(tmp);
      this.appAtc.save(tmp);
    }

    LoadSave.saveField(root, this, "prm");

    LoadSave.saveField(root, this, "now");

    {
      XElement tmp = new XElement("delayedPlanes");
      root.addElement(tmp);
      for (Airplane plane : this.newPlanesDelayedToAvoidCollision) {
        XElement pln = new XElement("plane");
        plane.save(pln);
        tmp.addElement(pln);
      }
    }

    //mrvaManager
    this.stats.save(root);
    LoadSave.saveField(root, this, "emergencyManager");
    this.trafficManager.save(root);
    LoadSave.saveField(root, this, "simulationSecondLengthInMs");
  }

  public IMap<String, String> getCommandShortcuts() {
    return this.appAtc.getParser().getShortcuts().getAll2();
  }

  public void setCommandShortcuts(IMap<String, String> shortcuts) {
    this.appAtc.getParser().getShortcuts().setAll2(shortcuts);
  }

  public void pauseUnpauseSim() {
    if (this.isRunning()) {
      this.stop();
    } else {
      this.start();
    }
  }

  private void twr_runwayChanged() {
    this.onRunwayChanged.raise();
  }

  private synchronized void elapseSecond() {
    long elapseStartMs = System.currentTimeMillis();

    if (DEBUG_STYLE_TIMER)
      tmr.stop();

    if (isBusy) {
      return;
    }
    isBusy = true;
    now.increaseSecond();

    // system stuff
    this.processSystemMessages();

    // traffic stuff
    trafficManager.generateNewTrafficIfRequired();

    // atc stuff
    this.ctrAtc.elapseSecond();
    this.twrAtc.elapseSecond();

    // planes stuff
    generateNewPlanes();
    removeOldPlanes();
    generateEmergencyIfRequired();
    updatePlanes();
    evalAirproxes();
    evalMrvas();

    stats.elapseSecond();
    long elapseEndMs = System.currentTimeMillis();
stats.registerElapseSecondCalculationDuration((int) (elapseEndMs - elapseStartMs));

    // weather
    this.weatherManager.elapseSecond();
    if (this.weatherManager.isNewWeatherFlagAndResetIt()) {
      twrAtc.setUpdatedWeatherFlag();
      sendTextMessageForUser("Weather updated: " + this.weatherManager.getWeather().toInfoString());
    }

    isBusy = false;

    // raises event
    this.onSecondElapsed.raise();

    if (DEBUG_STYLE_TIMER)
      tmr.start(tmr.getTickLength());
  }

  private void generateEmergencyIfRequired() {
    if (this.emergencyManager.isEmergencyTimeElapsed()) {
      if (!Acc.planes().isAny(q -> q.isEmergency())) {
        Airplane p = Acc.planes()
            .where(q -> q.getState().is(Airplane.State.departingLow,
                Airplane.State.departingHigh, Airplane.State.arrivingHigh, Airplane.State.arrivingLow, Airplane.State.arrivingCloseFaf))
            .tryGetRandom();
        if (p != null)
          p.raiseEmergency();
      }
      this.emergencyManager.generateEmergencyTime(this.now);
    }
  }

  private void updatePlanes() {
    for (Airplane plane : Acc.planes()) {
      try {
        plane.elapseSecond();
      } catch (Exception ex) {
        throw new EApplicationException("Error processing elapseSecond() on plane " + plane.getCallsign() + ".", ex);
      }
    }
  }

  private void generateNewPlanes() {
    IReadOnlyList<Airplane> newPlanes = trafficManager.getNewAirplanes();

    if (newPlanesDelayedToAvoidCollision.isEmpty() == false) {
      Airplane newPlane = newPlanesDelayedToAvoidCollision.tryGetFirst(
          q -> isInVicinityOfSomeOtherPlane(q) == false);
      if (newPlane != null) {
        newPlanesDelayedToAvoidCollision.remove(newPlane);
        registerNewPlaneIntoTheSimulation(newPlane, ctrAtc);
      }
    }

    for (Airplane newPlane : newPlanes) {
      if (newPlane.isDeparture()) {
        registerNewPlaneIntoTheSimulation(newPlane, twrAtc);
      } else {
        // here are two possibilities
        // 1. new airplanes are delayed to avoid current airplanes. That is, as far as new plane is in vicinity of an other plane, it is added to "delayed" collection.
        //    when it is no more in vicinity, it is added into approaching planes
        // 2. new airplanes are raised to fly over current ones. This seems to be more innatural and more difficult to implement, so this option is not included now.
        if (isInVicinityOfSomeOtherPlane(newPlane)) {
          newPlanesDelayedToAvoidCollision.add(newPlane);
        } else {
          registerNewPlaneIntoTheSimulation(newPlane, ctrAtc);
        }
      }
    }
  }

  private void registerNewPlaneIntoTheSimulation(Airplane newPlane, Atc responsibleAtc) {
    Acc.prm().registerNewPlane(responsibleAtc, newPlane);
    Acc.messenger().registerListener(newPlane, newPlane);
    this.mrvaManager.registerPlane(newPlane);
  }

  private void removeOldPlanes() {
    AirplaneList rem = new AirplaneList(true);
    for (Airplane p : Acc.planes()) {
      // landed
      if (p.isArrival() && p.getSpeed() < 11) {
        rem.add(p);
        this.stats.registerFinishedPlane(p);
      }

      // departed
      if (p.isDeparture() && Acc.prm().getResponsibleAtc(p).equals(Acc.atcCtr())
          && Coordinates.getDistanceInNM(
          p.getCoordinate(), Acc.airport().getLocation())
          > Acc.airport().getCoveredDistance()) {
        rem.add(p);
        this.stats.registerFinishedPlane(p);
      }

      if (p.isEmergency() && p.hasElapsedEmergencyTime()) {
        rem.add(p);
      }
    }

    for (Airplane plane : rem) {
      Acc.prm().unregisterPlane(plane);
      Acc.messenger().unregisterListener(plane);
      this.mrvaManager.unregisterPlane(plane);
    }
  }

  private void evalAirproxes() {
    Airplanes.evaluateAirproxes(Acc.planes());
    Acc.planes()
        .where(q -> q.getAirprox() == AirproxType.full && Acc.prm().getResponsibleAtc(q) == Acc.atcApp())
        .forEach(q -> q.getMood().experience(Mood.SharedExperience.airprox));
  }

  private void evalMrvas() {
    this.mrvaManager.evaluateMrvaFails();
    Acc.planes()
        .where(q -> q.isMrvaError() && Acc.prm().getResponsibleAtc(q) == Acc.atcApp())
        .forEach(q -> q.getMood().experience(Mood.SharedExperience.mrvaViolation));
  }

  private void processSystemMessages() {
    IList<Message> systemMessages = Acc.messenger().getMessagesByListener(messenger.SYSTEM, true);

    for (Message m : systemMessages) {
      processSystemMessage(m);
    }
  }

  private void processSystemMessage(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    if (msgText.equals(SYSMES_COMMANDS)) {
      printCommandsHelps();
    } else if (SYSMES_CHANGE_SPEED.asPredicate().test(msgText)) {
      processSystemMessageTick(m);
    } else if (SYSMES_METAR.asPredicate().test(msgText)) {
      String metarText = Acc.sim().getWeather().toInfoString();
      Acc.messenger().send(
          new Message(Messenger.SYSTEM, Acc.atcApp(), new StringMessageContent(metarText)));
    } else if (SYSMES_REMOVE.asPredicate().test(msgText)) {
      processSystemMessageRemove(m);
    } else if (SYSMES_SHORTCUT.asPredicate().test(msgText)) {
      processSystemMessageShortcut(m);
    } else {
      String msg = m.<StringMessageContent>getContent().getMessageText();
      String resp = new ShortBlockParser().getHelp(msg);
      if (resp == null)
        Acc.messenger().send(
            new Message(
                messenger.SYSTEM,
                m.<UserAtc>getSource(),
                new StringMessageContent("Unknown system command '%s'.", msg)));
      else
        Acc.messenger().send(
            new Message(
                messenger.SYSTEM,
                m.getSource(),
                new StringMessageContent(resp)));
    }
  }

  private void processSystemMessageRemove(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_REMOVE.matcher(msgText);
    if (!matcher.find()) {
      Acc.messenger().send(
          new Message(
              messenger.SYSTEM,
              m.<UserAtc>getSource(),
              new StringMessageContent("Illegal {remove} command format. Try ?remove <squawk>.")));
    }
    String sqwk = matcher.group(1);

    Airplane plane = null;
    for (Airplane airplane : Acc.planes()) {
      if (airplane.getSqwk().toString().equals(sqwk)) {
        plane = airplane;
        break;
      }
    }

    if (plane == null) {
      Acc.messenger().send(
          new Message(
              messenger.SYSTEM,
              m.<UserAtc>getSource(),
              new StringMessageContent("Unable to remove airplane from game. Squawk {%s} not found.", sqwk)));
    } else {
      Acc.prm().unregisterPlane(plane);
      Acc.messenger().send(
          new Message(
              messenger.SYSTEM,
              m.<UserAtc>getSource(),
              new StringMessageContent("Airplane %s {%s} removed from game.",
                  plane.getCallsign().toString(),
                  plane.getSqwk().toString())));
    }
  }

  private void processSystemMessageTick(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_CHANGE_SPEED.matcher(msgText);
    matcher.find();
    String tickS = matcher.group(1);
    int tickI;
    try {
      tickI = Integer.parseInt(tickS);
    } catch (NumberFormatException ex) {
      Acc.messenger().send(
          new Message(
              messenger.SYSTEM,
              m.<UserAtc>getSource(),
              new StringMessageContent("Current tick speed is " + tmr.getTickLength() + ". To change use ?tick <value>.", tickS)));
      return;
    }
    this.tmr.stop();
    this.tmr.start(tickI);

    Acc.messenger().send(
        new Message(
            messenger.SYSTEM,
            m.<UserAtc>getSource(),
            new StringMessageContent("Tick speed changed to %d milliseconds.", tickI))
    );
  }

  private void processSystemMessageShortcut(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_SHORTCUT.matcher(msgText);
    matcher.find();
    String key = matcher.group(1);
    if (matcher.group(2) == null) {
      if (key.equals("?")) {
        // print all keys
        EStringBuilder sb = new EStringBuilder();
        sb.appendLine("Printing all shortcuts:");
        ISet<Map.Entry<String, String>> shortcuts = Acc.atcApp().getParser().getShortcuts().getEntries();
        for (Map.Entry<String, String> shortcut : shortcuts) {
          sb.appendFormatLine("Shortcut key '%s' is expanded as '%s'.", shortcut.getKey(), shortcut.getValue());
        }
        Acc.messenger().send(
            new Message(
                messenger.SYSTEM,
                m.<UserAtc>getSource(),
                new StringMessageContent(sb.toString())));
      } else {
        // delete key
        Acc.atcApp().getParser().getShortcuts().remove(key);
        Acc.messenger().send(
            new Message(
                messenger.SYSTEM,
                m.<UserAtc>getSource(),
                new StringMessageContent("Command shortcut key '%s' removed.", key)));
      }
    } else {
      String value = matcher.group(3);
      if (value.equals("?")) {
        // print current
        value = Acc.atcApp().getParser().getShortcuts().tryGet(key);
        Acc.messenger().send(
            new Message(
                messenger.SYSTEM,
                m.<UserAtc>getSource(),
                new StringMessageContent("Command shortcut '%s' has expansion '%s'.", key, value)));
      } else {
        Acc.atcApp().getParser().getShortcuts().add(key, value);
        Acc.messenger().send(
            new Message(
                messenger.SYSTEM,
                m.<UserAtc>getSource(),
                new StringMessageContent("Command shortcut '%s' is now defined as '%s'.", key, value)));
      }
    }
  }

  private void printCommandsHelps() {
    String txt = new ShortBlockParser().getHelp();

    Acc.messenger().send(
        new Message(
            messenger.SYSTEM,
            Acc.atcApp(),
            new StringMessageContent(txt))
    );
  }

  private boolean isInVicinityOfSomeOtherPlane(Airplane checkedPlane) {
    Integer checkedAtEntryPointSeconds = null;

    boolean ret = false;
    for (Airplane plane : Acc.planes()) {
      if (plane.isArrival() == false)
        continue;
      if (prm.getResponsibleAtc(plane) != ctrAtc)
        continue;
      if (checkedPlane.getEntryExitFix().equals(plane.getEntryExitFix()) == false)
        continue;

      double dist = Coordinates.getDistanceInNM(
          plane.getEntryExitFix().getCoordinate(), plane.getCoordinate());
      int atEntryPointSeconds = (int) (dist / plane.getSpeed() * 3600);

      if (checkedAtEntryPointSeconds == null) {
        dist = Coordinates.getDistanceInNM(
            checkedPlane.getEntryExitFix().getCoordinate(), checkedPlane.getCoordinate());
        checkedAtEntryPointSeconds = (int) (dist / checkedPlane.getSpeed() * 3600);
      }

      if (Math.abs(atEntryPointSeconds - checkedAtEntryPointSeconds) < 120) {
        ret = true;
        break;
      }
    }
    return ret;
  }
}