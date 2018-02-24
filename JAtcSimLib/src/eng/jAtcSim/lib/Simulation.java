/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib;

import eng.eSystem.events.EventSimple;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.airplanes.Airplanes;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.CenterAtc;
import eng.jAtcSim.lib.atcs.TowerAtc;
import eng.jAtcSim.lib.atcs.UserAtc;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.ERandom;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.ReadOnlyList;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.ShortParser;
import eng.jAtcSim.lib.traffic.Movement;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.RunwayThreshold;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marek
 */
public class Simulation {

  public static final ERandom rnd = new ERandom();
  private static final int MINIMAL_DEPARTURE_REMOVE_DISTANCE = 100;
  private static final String SYSMES_COMMANDS = "?";
  private static final Pattern SYSMES_CHANGE_SPEED = Pattern.compile("tick (\\d+)");
  private static final Pattern SYSMES_METAR = Pattern.compile("metar");
  private static final Pattern SYSMES_REMOVE = Pattern.compile("remove (\\d{4})");
  private final static double MAX_VICINITY_DISTANCE_IN_NM = 10;
  private final ETime now;
  private final AirplaneTypes planeTypes;
  private final Airport airport;
  private final Messenger messenger = new Messenger();
  private final UserAtc appAtc;
  private final TowerAtc twrAtc;
  private final CenterAtc ctrAtc;
  private final Traffic traffic;
  private final List<Airplane> newPlanesDelayedToAvoidCollision = new LinkedList();
  /**
   * Public event informing surrounding about elapsed second.
   */
  private EventSimple<Simulation> secondElapsedEvent =
      new EventSimple<>(this);
  private int simulationSecondLengthInMs;
  private Weather weather;
  private boolean isBusy = false;
  /**
   * Internal timer used to make simulation ticks.
   */
  private final Timer tmr = new Timer(o -> Simulation.this.elapseSecond());

  private Simulation(Airport airport, AirplaneTypes types, Weather weather, Traffic traffic, Calendar now, int simulationSecondLengthInMs) {
    if (airport == null) {
      throw new IllegalArgumentException("Argument \"airport\" cannot be null.");
    }
    if (types == null) {
      throw new IllegalArgumentException("Argument \"types\" cannot be null.");
    }
    if (weather == null) {
      throw new IllegalArgumentException("Argument \"weather\" cannot be null.");
    }
    if (traffic == null) {
      throw new IllegalArgumentException("Argument \"traffic\" cannot be null.");
    }
    if (now == null) {
      throw new IllegalArgumentException("Argument \"now\" cannot be null.");
    }

    this.airport = airport;
    this.planeTypes = types;
    this.weather = weather;
    this.traffic = traffic;
    this.twrAtc = new TowerAtc(airport.getAtcTemplates().get(Atc.eType.twr));
    this.ctrAtc = new CenterAtc(airport.getAtcTemplates().get(Atc.eType.ctr));
    this.appAtc = new UserAtc(airport.getAtcTemplates().get(Atc.eType.app));

    this.now = new ETime(now);
    this.simulationSecondLengthInMs = simulationSecondLengthInMs;
  }

  public static Simulation create(Airport airport, AirplaneTypes types, Weather weather, Traffic traffic, Calendar now, int simulationSecondLengthInMs) {
    Simulation ret = new Simulation(airport, types, weather, traffic, now, simulationSecondLengthInMs);

    Acc.setSimulation(ret);

    Acc.atcTwr().init();
    Acc.atcApp().init();
    Acc.atcCtr().init();

    traffic.generateNewMovementsIfRequired(); // this must be here, after "simTime" init

    return ret;
  }

  public EventSimple<Simulation> getSecondElapsedEvent() {
    return secondElapsedEvent;
  }

  //TODO shouldn't this be private?
  public AirplaneTypes getPlaneTypes() {
    return planeTypes;
  }

  public Movement[] getScheduledMovements() {
    Movement[] ret;
    ret = traffic.getScheduledMovements();
    return ret;
  }

  public Airport getActiveAirport() {
    return airport;
  }

  public String toAltitudeString(double altInFt, boolean appendFt) {
    if (altInFt > getActiveAirport().getTransitionAltitude()) {
      return String.format("FL%03d", ((int) altInFt) / 100);
    } else {
      if (appendFt) {
        return String.format("%04d ft", (int) altInFt);
      } else {
        return String.format("%04d", (int) altInFt);
      }
    }
  }

  public ETime getNow() {
    return now;
  }

  public ReadOnlyList<Airplane> getAirplanes() {
    return Acc.prm().getAll();
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

  public ReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
    return Acc.prm().getPlanesToDisplay();
  }

  public Atc getResponsibleAtc(Airplane plane) {
    return Acc.prm().getResponsibleAtc(plane);
  }

  public Weather getWeather() {
    return weather;
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

  public RunwayThreshold getActiveRunwayThreshold() {
    return Acc.threshold();
  }

  public void setActiveRunwayThreshold(RunwayThreshold newRunwayThreshold) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private void elapseSecond() {
    if (isBusy) {
      System.out.println("## -- elapse second is busy!");
      return;
    }
    //long start = System.currentTimeMillis();
    isBusy = true;
    now.increaseSecond();

    // system stuff
    this.processSystemMessages();

    // traffic stuff
    if (now.isIntegralMinute()) {
      traffic.generateNewMovementsIfRequired();
    }

    // atc stuff
    this.ctrAtc.elapseSecond();
    this.twrAtc.elapseSecond();
    //this.appAtc.elapseSecond();

    // planes stuff
    generateNewPlanes();
    removeOldPlanes();
    updatePlanes();
    evalAirproxes();

    //long end = System.currentTimeMillis();
    //System.out.println("## Sim elapse second: \t" + (end - start) + " at " + now.toString());

    isBusy = false;

    // raises event
    this.secondElapsedEvent.raise();
  }

  private void updatePlanes() {
    for (Airplane plane : Acc.planes()) {
      plane.elapseSecond();
    }
  }

  private void generateNewPlanes() {
    Airplane[] newPlanes = traffic.getNewAirplanes();

    if (newPlanesDelayedToAvoidCollision.isEmpty() == false) {
      for (Airplane newPlane : newPlanesDelayedToAvoidCollision) {
        if (isInVicinityOfSomeOtherPlane(newPlane) == false) {
          newPlanesDelayedToAvoidCollision.remove(newPlane);
          Acc.prm().registerPlane(ctrAtc, newPlane);
          ctrAtc.registerNewPlane(newPlane);
        }
      }
    }

    for (Airplane newPlane : newPlanes) {
      if (newPlane.isDeparture()) {
        Acc.prm().registerPlane(twrAtc, newPlane);
        twrAtc.registerNewPlane(newPlane);
      } else {
        // here are two possibilities
        // 1. new airplanes are delayed to avoid current airplanes. That is, as far as new plane is in vicinity of an other plane, it is added to "delayed" collection.
        //    when it is no more in vicinity, it is added into approaching planes
        // 2. new airplanes are raised to fly over current ones. This seems to be more innatural and more difficult to implement, so this option is not included now.
        if (isInVicinityOfSomeOtherPlane(newPlane)) {
          newPlanesDelayedToAvoidCollision.add(newPlane);
        } else {
          Acc.prm().registerPlane(ctrAtc, newPlane);
          ctrAtc.registerNewPlane(newPlane);
        }
      }
    }
  }

  private void removeOldPlanes() {
    AirplaneList rem = new AirplaneList();
    for (Airplane p : Acc.planes()) {
      // landed
      if (p.isArrival() && p.getSpeed() < 11) {
        rem.add(p);
      }

      // departed
      if (p.isDeparture() && Acc.prm().getResponsibleAtc(p).equals(Acc.atcCtr())
          && Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation()) > MINIMAL_DEPARTURE_REMOVE_DISTANCE) {
        rem.add(p);
      }
    }

    for (Airplane p : rem) {
      Acc.prm().unregisterPlane(p);
    }
  }

  private void evalAirproxes() {
    Airplanes.evaluateAirproxes(Acc.planes());
  }

  private void processSystemMessages() {
    List<Message> systemMessages = Acc.messenger().getByTarget(messenger.SYSTEM, true);

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
    }
  }

  private void processSystemMessageRemove(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_REMOVE.matcher(msgText);
    if (!matcher.find()){
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

    if (plane == null){
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

  private void printCommandsHelps() {
    String txt = new ShortParser().getHelp();

    Acc.messenger().send(
        new Message(
            messenger.SYSTEM,
            Acc.atcApp(),
            new StringMessageContent(txt))
    );
  }

  private boolean isInVicinityOfSomeOtherPlane(Airplane checkedPlane) {
    boolean ret = false;
    for (Airplane plane : Acc.planes()) {
      if (plane.isArrival() == false) {
        continue;
      }
      double dist = Coordinates.getDistanceInNM(checkedPlane.getCoordinate(), plane.getCoordinate());
      if (dist < MAX_VICINITY_DISTANCE_IN_NM) {
        ret = true;
        break;
      }
    }
    return ret;
  }
}
