package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.IAirplaneList;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToRouteCommand;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import java.util.function.Consumer;

// region Inner
class DepartureManager implements IXPersistable {

  @XIgnored
  private TowerAtc parent;
  @XIgnored
  private Consumer<Message> messageSenderConsumer;
  @XIgnored
  private final IList<IAirplane> holdingPointNotAssigned = new EDistinctList<>(EDistinctList.Behavior.exception);
  @XIgnored
  private final IList<IAirplane> holdingPointWaitingForAppSwitchConfirmation = new EDistinctList<>(EDistinctList.Behavior.exception);
  @XIgnored
  private final IList<IAirplane> holdingPointReady = new EDistinctList<>(EDistinctList.Behavior.exception);
  @XIgnored
  private final IList<IAirplane> departing = new EList<>();
  @XIgnored
  private final IMap<IAirplane, Double> departureSwitchAltitude = new EMap<>();
  @XIgnored
  private final IMap<IAirplane, EDayTimeStamp> holdingPointWaitingTimeMap = new EMap<>();
  @XIgnored
  private final IMap<ActiveRunwayThreshold, IAirplane> lastDepartingPlane = new EMap<>();
  private final IMap<ActiveRunwayThreshold, EDayTimeStamp> lastDeparturesTime = new EMap<>();

  @XConstructor
  @XmlConstructor
  DepartureManager() {
    PostContracts.register(this, () -> parent != null);
  }

  public void bind(TowerAtc parent, Consumer<Message> messageSenderConsumer) {
    this.parent = parent;
    this.messageSenderConsumer = messageSenderConsumer;
  }

  public void confirmedByApproach(IAirplane plane) {
    if (this.holdingPointWaitingForAppSwitchConfirmation.contains(plane)) {
      this.holdingPointWaitingForAppSwitchConfirmation.remove(plane);
      this.holdingPointReady.add(plane);
    }
  }

  public void deletePlane(IAirplane plane) {
    holdingPointNotAssigned.tryRemove(plane);
    holdingPointWaitingForAppSwitchConfirmation.tryRemove(plane);
    holdingPointReady.tryRemove(plane);
    departing.tryRemove(plane);
    for (ActiveRunwayThreshold rt : this.lastDepartingPlane.getKeys()) {
      if (this.lastDepartingPlane.containsKey(rt) && plane.equals(this.lastDepartingPlane.get(rt))) {
        this.lastDepartingPlane.set(rt, null);
        this.lastDeparturesTime.set(rt, null);
      }
    }
    holdingPointWaitingTimeMap.tryRemove(plane);
  }

  public void departAndGetHoldingPointEntryTime(IAirplane plane, ActiveRunwayThreshold th, double switchAltitude) {
    this.holdingPointReady.remove(plane);
    this.departing.add(plane);
    this.lastDepartingPlane.set(th, plane);
    this.lastDeparturesTime.set(th, Context.getShared().getNow().toStamp());
    this.departureSwitchAltitude.set(plane, switchAltitude);
  }

  public IReadOnlyList<IAirplane> getAllPlanes() {
    //TODO do in something more efficient way
    IList<IAirplane> ret = new EList<>();
    ret.addMany(this.holdingPointNotAssigned);
    ret.addMany(this.holdingPointWaitingForAppSwitchConfirmation);
    ret.addMany(this.holdingPointReady);
    ret.addMany(this.departing);
    return ret;
  }

  public EDayTimeStamp getAndEraseHoldingPointEntryTime(IAirplane plane) {
    EDayTimeStamp ret = holdingPointWaitingTimeMap.get(plane);
    holdingPointWaitingTimeMap.remove(plane);
    return ret;
  }

  public IReadOnlyList<IAirplane> getDepartedPlanesReadyToHangoff(boolean releaseFromThemFromDepartureManager) {
    IReadOnlyList<IAirplane> ret = this.departing.where(q -> departureSwitchAltitude.get(q) < q.getSha().getAltitude());
    if (releaseFromThemFromDepartureManager)
      ret.forEach(q -> {
        this.departing.remove(q);
        this.departureSwitchAltitude.remove(q);
      });
    return ret;
  }

  public EDayTimeStamp getLastDepartureTime(ActiveRunwayThreshold rt) {
    EDayTimeStamp ret;
    ret = this.lastDeparturesTime.tryGet(rt);
    if (ret == null)
      ret = new EDayTimeStamp(0);
    return ret;
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return this.holdingPointWaitingForAppSwitchConfirmation.size() + this.holdingPointReady.size();
  }

  public IMap<ActiveRunwayThreshold, IAirplane> getTheLinedUpPlanes() {
    IMap<ActiveRunwayThreshold, IAirplane> ret = new EMap<>();
    for (IAirplane airplane : holdingPointReady) {
      if (ret.containsKey(airplane.getRouting().getAssignedRunwayThreshold()) == false) {
        ret.set(airplane.getRouting().getAssignedRunwayThreshold(), airplane);
      }
    }
    return ret;
  }

  public boolean isPlaneReadyToSwitch(IAirplane plane) {
    return this.holdingPointWaitingForAppSwitchConfirmation.contains(plane);
  }

  public boolean isSomeDepartureOnRunway(String rwyName) {
    ActiveRunway runway = Context.Internal.getRunway(rwyName);
    for (ActiveRunwayThreshold rt : runway.getThresholds()) {
      IAirplane aip = this.lastDepartingPlane.tryGet(rt);
      if (aip != null && aip.getState() == AirplaneState.takeOffRoll)
        return true;
    }
    return false;
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    IAirplaneList planes = ctx.loader.values.get(IAirplaneList.class);

    ctx.loader
            .loadItems(elm.getChild("holdingPointNotAssigned"), Callsign.class)
            .forEach(q -> this.holdingPointNotAssigned.add(planes.get(q)));

    ctx.loader
            .loadItems(elm.getChild("holdingPointWaitingForAppSwitchConfirmation"), Callsign.class)
            .forEach(q -> this.holdingPointWaitingForAppSwitchConfirmation.add(planes.get(q)));

    ctx.loader
            .loadItems(elm.getChild("holdingPointReady"), Callsign.class)
            .forEach(q -> this.holdingPointReady.add(planes.get(q)));

    ctx.loader
            .loadItems(elm.getChild("departing"), Callsign.class)
            .forEach(q -> this.departing.add(planes.get(q)));

    ctx.loader
            .loadEntries(elm.getChild("lastDepartingPlane"), String.class, Callsign.class)
            .forEach(q -> {
              ActiveRunwayThreshold k = ctx.loader.parents.get(Airport.class).getRunwayThreshold(q.getKey());
              IAirplane v = ctx.loader.values.get(IAirplaneList.class).get(q.getValue());
              this.lastDepartingPlane.set(k, v);
            });

    ctx.loader
            .loadEntries(elm.getChild("departureSwitchAltitude"), Callsign.class, Double.class)
            .forEach(q -> {
              IAirplane k = ctx.loader.values.get(IAirplaneList.class).get(q.getKey());
              this.departureSwitchAltitude.set(k, q.getValue());
            });

    ctx.loader
            .loadEntries(elm.getChild("holdingPointWaitingTimeMap"), Callsign.class, EDayTimeStamp.class)
            .forEach(q -> {
              IAirplane k = ctx.loader.values.get(IAirplaneList.class).get(q.getKey());
              this.holdingPointWaitingTimeMap.set(k, q.getValue());
            });
  }

  public void movePlanesToHoldingPoint() {
    for (IAirplane plane : holdingPointNotAssigned.where(q -> q.getRouting().getAssignedRunwayThreshold() != null)) {
      this.holdingPointNotAssigned.remove(plane);
      this.holdingPointWaitingForAppSwitchConfirmation.add(plane);
      this.holdingPointWaitingTimeMap.set(plane, Context.getShared().getNow().toStamp());
    }
  }

  public void registerNewDeparture(IAirplane plane, ActiveRunwayThreshold runwayThreshold) {
    EAssert.Argument.isTrue(plane.getState() == AirplaneState.holdingPoint);
    this.holdingPointNotAssigned.add(plane);
    DARoute r = getDepartureRouteForPlane(runwayThreshold, plane.getType(), plane.getRouting().getEntryExitPoint(), true);
    Message m = new Message(
            Participant.createAtc(this.parent.getAtcId()),
            Participant.createAirplane(plane.getCallsign()),
            new SpeechList<ICommand>(ClearedToRouteCommand.create(r.getName(), r.getType(), runwayThreshold.getName())));
    this.messageSenderConsumer.accept(m);
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.saveItems(holdingPointNotAssigned.select(q -> q.getCallsign()), Callsign.class, elm, "holdingPointNotAssigned");
    ctx.saver.saveItems(holdingPointWaitingForAppSwitchConfirmation.select(q -> q.getCallsign()), Callsign.class, elm, "holdingPointWaitingForAppSwitchConfirmation");
    ctx.saver.saveItems(holdingPointReady.select(q -> q.getCallsign()), Callsign.class, elm, "holdingPointReady");
    ctx.saver.saveItems(departing.select(q -> q.getCallsign()), Callsign.class, elm, "departing");

    ctx.saver.saveEntries(this.lastDepartingPlane.select(q -> q.getName(), q -> q.getCallsign()),
            String.class, Callsign.class, elm, "lastDepartingPlane");
    ctx.saver.saveEntries(this.holdingPointWaitingTimeMap.select(q -> q.getCallsign(), q -> q),
            Callsign.class, EDayTimeStamp.class, elm, "holdingPointWaitingTimeMap");
    ctx.saver.saveEntries(this.departureSwitchAltitude.select(q -> q.getCallsign(), q -> q),
            Callsign.class, Double.class, elm, "departureSwitchAltitude");
  }

  public IAirplane tryGetTheLastDepartedPlane(ActiveRunwayThreshold rt) {
    IAirplane ret;
    ret = this.lastDepartingPlane.tryGet(rt);
    return ret;
  }

  public void unregisterFinishedDeparture(IAirplane plane) {
    departing.remove(plane);
    lastDepartingPlane.remove(q -> q.getValue() == plane);
  }

  private DARoute getDepartureRouteForPlane(ActiveRunwayThreshold rt, AirplaneType type, Navaid mainNavaid, boolean canBeVectoring) {
    DARoute ret = rt.getRoutes().where(
            q -> q.getType() == DARouteType.sid
                    && q.isValidForCategory(type.category)
                    && q.getMaxMrvaAltitude() < type.maxAltitude
                    && q.getMainNavaid().equals(mainNavaid))
            .tryGetRandom();
    if (ret == null && canBeVectoring)
      ret = DARoute.createNewVectoringByFix(mainNavaid);
    return ret;
  }
}
// endregion Inner
