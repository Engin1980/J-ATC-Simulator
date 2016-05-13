/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.airplanes.Airplanes;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.CenterAtc;
import jatcsimlib.atcs.TowerAtc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.commands.formatting.ShortParser;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.global.ERandom;
import jatcsimlib.global.ETime;
import jatcsimlib.global.ReadOnlyList;
import jatcsimlib.messaging.Message;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.traffic.GeneratedTraffic;
import jatcsimlib.traffic.TestTrafficOneApproach;
import jatcsimlib.traffic.Traffic;
import jatcsimlib.weathers.Weather;
import jatcsimlib.weathers.MetarDownloaderNoaaGov;
import jatcsimlib.weathers.MetarDownloader;
import jatcsimlib.world.Airport;
import jatcsimlib.world.RunwayThreshold;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Marek
 */
public class Simulation {

  public EventListener<Simulation, Object> secondElapsed = null;

  private final Timer tmr = new Timer(new EventListener<Timer, Object>() {
    @Override
    public void raise(Timer parent, Object e) {
      Simulation.this.elapseSecond();
      if (secondElapsed != null) {
        secondElapsed.raise(Simulation.this, null);
      }
    }
  });
  public static final ERandom rnd = new ERandom();
  private final ETime now;
  private final AirplaneTypes planeTypes;
  private final Airport airport;
  private Weather weather;
  private final Messenger messenger = new Messenger();
  private final UserAtc appAtc;
  private final TowerAtc twrAtc;
  private final CenterAtc ctrAtc;

  //private final Traffic traffic = new TestTrafficOneApproach(); 
  private final int TRAFFIC_COUNT = 35;
  private final Traffic traffic = new GeneratedTraffic(
    1, 1, new int[]{
      TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 3,
      TRAFFIC_COUNT / 2, TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 3, TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 4,
      TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 2, TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 3, TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 2,
      TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 3, TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 1, TRAFFIC_COUNT / 4, TRAFFIC_COUNT / 4});

  private final EventManager<Simulation, EventListener<Simulation, Simulation>, Simulation> tickEM = new EventManager(this);

  //TODO nemelo by byt soukrome
  public AirplaneTypes getPlaneTypes() {
    return planeTypes;
  }

  public Airport getActiveAirport() {
    return airport;
  }

  public String toAltitudeString(int altInFt, boolean appendFt) {
    if (altInFt > getActiveAirport().getTransitionAltitude()) {
      return String.format("FL%03d", altInFt / 100);
    } else {
      if (appendFt) {
        return String.format("%04d ft", altInFt);
      } else {
        return String.format("%04d", altInFt);
      }
    }
  }

  public ETime getNow() {
    return now;
  }

  public ReadOnlyList<Airplane.AirplaneInfo> getPlaneInfos() {
    return Acc.prm().getInfos();
  }

  public Messenger getMessenger() {
    return messenger;
  }

  private Simulation(Airport airport, AirplaneTypes types, Calendar now) {
    if (airport == null) {
      throw new IllegalArgumentException("Argument \"airport\" cannot be null.");
    }

    this.airport = airport;
    this.planeTypes = types;
    this.twrAtc = new TowerAtc(airport.getAtcTemplates().get(Atc.eType.twr));
    this.ctrAtc = new CenterAtc(airport.getAtcTemplates().get(Atc.eType.ctr));
    this.appAtc = new UserAtc(airport.getAtcTemplates().get(Atc.eType.app));

    this.now = new ETime(now);
  }

  public static Simulation create(Airport airport, AirplaneTypes types, Calendar now) {
    Simulation ret = new Simulation(airport, types, now);

    Acc.setSimulation(ret);

    // weather
    MetarDownloader wd = new MetarDownloaderNoaaGov();
    ret.weather = wd.downloadWeather(airport.getIcao());

    Acc.atcTwr().init();
    Acc.atcApp().init();
    Acc.atcCtr().init();

    return ret;
  }

  private boolean isBusy = false;

  public void start() {
    if (this.tmr.isRunning() == false) {
      this.tmr.start(1000); // initial speed 1sec
    }
  }

  public void stop() {
    this.tmr.stop();
  }

  private void elapseSecond() {
    if (isBusy) {
      System.out.println("## -- elapse second is busy!");
      return;
    }
    long start = System.currentTimeMillis();
    isBusy = true;
    now.increaseSecond();

    // system stuff
    this.processSystemMessages();

    // atc stuff
    this.ctrAtc.elapseSecond();
    this.twrAtc.elapseSecond();
    //this.appAtc.elapseSecond();

    // planes stuff
    generateNewPlanes();
    removeOldPlanes();
    updatePlanes();
    evalAirproxes();

    long end = System.currentTimeMillis();
    //System.out.println("## Sim elapse second: \t" + (end - start) + " at " + now.toString());

    tickEM.raise(this);
    isBusy = false;
  }

  private void updatePlanes() {
    for (Airplane plane : Acc.planes()) {
      plane.elapseSecond();
    }
  }

  private void generateNewPlanes() {
    Airplane[] newPlanes = traffic.getNewAirplanes();

    for (Airplane newPlane : newPlanes) {
      if (newPlane.isDeparture()) {
        Acc.prm().registerPlane(twrAtc, newPlane);
        twrAtc.registerNewPlane(newPlane);
      } else {
        Acc.prm().registerPlane(ctrAtc, newPlane);
        ctrAtc.registerNewPlane(newPlane);
      }
    }
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

  private void removeOldPlanes() {
    AirplaneList rem = new AirplaneList();
    for (Airplane p : Acc.planes()) {
      // landed
      if (p.isArrival() && p.getSpeed() < 11) {
        rem.add(p);
      }

      // departed
      if (p.isDeparture() && Acc.prm().getResponsibleAtc(p).equals(Acc.atcCtr())
        && (p.getAltitude() == p.getTargetAltitude() || p.getAltitude() > Acc.atcCtr().getReleaseAltitude() + 2000)) {
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

  public void setActiveRunwayThreshold(RunwayThreshold newRunwayThreshold) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public RunwayThreshold getActiveRunwayThreshold() {
    return Acc.threshold();
  }

  private void processSystemMessages() {
    List<Message> systemMessages = Acc.messenger().getSystems(true);

    for (Message m : systemMessages) {
      processSystemMessage(m);
    }
  }

  private static final String SYSMES_COMMANDS = "?";
  private static final Pattern SYSMES_CHANGE_SPEED = Pattern.compile("tick=(\\d+)");

  private void processSystemMessage(Message m) {
    String msgText = m.getAsString().text;
    if (msgText.equals(SYSMES_COMMANDS)) {
      printCommandsHelps();
    } else if (SYSMES_CHANGE_SPEED.asPredicate().test(msgText)) {
      processSystemMessageTick(msgText, m);
    }
  }

  private void processSystemMessageTick(String msgText, Message m) {
    Matcher matcher = SYSMES_CHANGE_SPEED.matcher(msgText);
    matcher.find();
    String tickS = matcher.group(1);
    int tickI;
    try {
      tickI = Integer.parseInt(tickS);
    } catch (NumberFormatException ex) {
      Acc.messenger().addMessage(
        Message.createFromSystem((UserAtc) m.source, "Unable to parse " + tickS + " to integer. Example: ?tick=750"));
      return;
    }
    this.tmr.stop();
    this.tmr.start(tickI);
    Acc.messenger().addMessage(
      Message.createFromSystem((UserAtc) m.source, "Tick speed changed to " + tickI + " miliseconds."));
  }

  private void printCommandsHelps() {
    String txt = new ShortParser().getHelp();

    Acc.messenger().addMessage(Message.createFromSystem(Acc.atcApp(), txt));
  }

}
