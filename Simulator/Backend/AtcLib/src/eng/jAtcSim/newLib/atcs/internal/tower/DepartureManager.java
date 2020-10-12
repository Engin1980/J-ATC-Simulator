package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToRouteCommand;

class DepartureManager {

  private final TowerAtc parent;
  private final IList<IAirplane> holdingPointNotReady = new EDistinctList<>(EDistinctList.Behavior.exception);
  private final IList<IAirplane> holdingPointReady = new EDistinctList<>(EDistinctList.Behavior.exception);
  private final IList<IAirplane> departing = new EList<>();
  private final IMap<IAirplane, Double> departureSwitchAltitude = new EMap<>();
  private final IMap<IAirplane, EDayTimeStamp> holdingPointWaitingTimeMap = new EMap<>();
  private final IMap<ActiveRunwayThreshold, IAirplane> lastDepartingPlane = new EMap<>();
  private final IMap<ActiveRunwayThreshold, EDayTimeStamp> lastDeparturesTime = new EMap<>();

  public DepartureManager(TowerAtc parent) {
    this.parent = parent;
  }

  public boolean canBeSwitched(IAirplane plane) {
    if (departureSwitchAltitude.containsKey(plane) && departureSwitchAltitude.get(plane) < plane.getSha().getAltitude()) {
      departureSwitchAltitude.remove(plane);
      return true;
    } else
      return false;
  }

  public void confirmedByApproach(IAirplane plane) {
    if (this.holdingPointNotReady.contains(plane)) {
      this.holdingPointNotReady.remove(plane);
      this.holdingPointReady.add(plane);
    }
  }

  public void deletePlane(IAirplane plane) {
    holdingPointNotReady.tryRemove(plane);
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

  public EDayTimeStamp getAndEraseHoldingPointEntryTime(IAirplane plane) {
    EDayTimeStamp ret = holdingPointWaitingTimeMap.get(plane);
    holdingPointWaitingTimeMap.remove(plane);
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
    return this.holdingPointNotReady.size() + this.holdingPointReady.size();
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

  public boolean isSomeDepartureOnRunway(String rwyName) {
    ActiveRunway runway = Context.Internal.getRunway(rwyName);
    for (ActiveRunwayThreshold rt : runway.getThresholds()) {
      IAirplane aip = this.lastDepartingPlane.tryGet(rt);
      if (aip != null && aip.getState() == AirplaneState.takeOffRoll)
        return true;
    }
    return false;
  }

  public void registerNewDeparture(IAirplane plane, ActiveRunwayThreshold runwayThreshold) {
    this.holdingPointNotReady.add(plane);
    holdingPointWaitingTimeMap.set(plane, Context.getShared().getNow().toStamp());
    DARoute r = getDepartureRouteForPlane(runwayThreshold,plane.getType(), plane.getRouting().getEntryExitPoint(), true);
    Message m = new Message(
        Participant.createAtc(this.parent.getAtcId()),
        Participant.createAirplane(plane.getCallsign()),
        new SpeechList<ICommand>(ClearedToRouteCommand.create(r.getName(), r.getType(), runwayThreshold.getName())));
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
