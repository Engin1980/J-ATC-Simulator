package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;

class ArrivalManager {
  private final TowerAtc parent;
  private IList<IAirplane> landingPlanesList = new EDistinctList<>(EDistinctList.Behavior.exception);
  private IList<IAirplane> goAroundedPlanesToSwitchList = new EDistinctList<>(EDistinctList.Behavior.exception);

  public ArrivalManager(TowerAtc parent) {
    this.parent = parent;
  }

  public boolean checkIfPlaneIsReadyToSwitchAndRemoveIt(IAirplane plane) {
    if (goAroundedPlanesToSwitchList.contains(plane)) {
      goAroundedPlanesToSwitchList.remove(plane);
      return true;
    } else
      return false;
  }

  public void deletePlane(IAirplane plane) {
    this.landingPlanesList.tryRemove(plane);
    this.goAroundedPlanesToSwitchList.tryRemove(plane);
  }

  public double getClosestLandingPlaneDistanceForThreshold(ActiveRunwayThreshold threshold) {
    IList<IAirplane> tmp = AirplaneAcc.getAirplaneList().toList()
        .where(q -> threshold.equals(q.getRouting().getAssignedRunwayThreshold()));
    double ret = Double.MAX_VALUE;
    for (IAirplane plane : tmp) {
      if (plane.getState() == AirplaneState.landed) {
        ret = 0;
        break;
      } else if (plane.getState().is(
          AirplaneState.shortFinal,
          AirplaneState.longFinal,
          AirplaneState.approachDescend
      )) {
        double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), threshold.getCoordinate());
        if (dist < ret)
          ret = dist;
      }
    }
    return ret;
  }

  public void goAroundPlane(IAirplane plane) {
    landingPlanesList.remove(plane);
    goAroundedPlanesToSwitchList.add(plane);
  }

  public boolean isSomeArrivalApproachingOrOnRunway(ActiveRunway runway) {
    if (runway == null) {
      throw new IllegalArgumentException("Value of {runway} cannot not be null.");
    }
    return this.landingPlanesList.where(q -> q.getRouting().getAssignedRunwayThreshold().getParent().equals(runway)).isEmpty();
  }

  public boolean isSomeArrivalOnRunway(ActiveRunway rwy) {
    boolean ret = this.landingPlanesList
        .where(q -> rwy.getThresholds().contains(q.getRouting().getAssignedRunwayThreshold()))
        .isAny(q -> q.getState() == AirplaneState.landed);
    return ret;
  }

  public void registerNewArrival(IAirplane plane) {
    if (plane == null) {
      throw new IllegalArgumentException("Value of {plane} cannot not be null.");
    }

    assert plane.isArrival();
    assert plane.getRouting().getAssignedRunwayThreshold() != null : "Assigned arrival for " + plane.getCallsign() + " is null.";
    if (plane.getState().is(AirplaneState.approachEnter, AirplaneState.approachDescend, AirplaneState.longFinal, AirplaneState.shortFinal))
      this.landingPlanesList.add(plane);
    else
      this.goAroundedPlanesToSwitchList.add(plane);
  }

  public void unregisterFinishedArrival(IAirplane plane) {
    this.landingPlanesList.remove(plane);
  }

  public void unregisterGoAroundedArrival(IAirplane plane) {
    this.goAroundedPlanesToSwitchList.remove(plane);
  }
}