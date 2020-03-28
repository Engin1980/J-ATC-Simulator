package eng.jAtcSim.newLib.stats;

import eng.jAtcSim.newLib.stats.properties.TimedValue;

public class RecentStatsManager {

    public static void elapseSecond() {
    if (recentSecondsElapsed < RECENT_INTERVAL_IN_SECONDS)
      recentSecondsElapsed++;

    EDayTimeStamp nowTime = getNow().toStamp();
    IReadOnlyList<Airplane> planes = Acc.prm().getPlanes();
    int airproxErs = 0;
    int mrvaErs = 0;
    int arrs = 0;
    int deps = 0;
    int aarrs = 0;
    int adeps = 0;
    for (Airplane plane : planes) {
      if (plane.getMrvaAirproxModule().getAirprox() == AirproxType.full)
        airproxErs++;
      if (plane.getMrvaAirproxModule().isMrvaError())
        mrvaErs++;

      boolean isApp = Acc.prm().getResponsibleAtc(plane) == Acc.atcApp();
      if (plane.getFlightModule().isArrival()) {
        arrs++;
        if (isApp) aarrs++;
      } else {
        deps++;
        if (isApp) adeps++;
      }
    }
    this.airproxErrors.add(new TimedValue<>(nowTime, airproxErs));
    this.mrvaErrors.add(new TimedValue<>(nowTime, mrvaErs));

    int hpCount = Acc.atcTwr().getNumberOfPlanesAtHoldingPoint();
    if (hpCount != this.holdingPointCurrentCount)
      this.holdingPointMaximalCount.add(new TimedValue<>(nowTime, hpCount));
    this.holdingPointCurrentCount = hpCount;

    boolean upd = false;
    if (arrs != this.currentArrivals) {
      this.maximumArrivals.add(new TimedValue<>(nowTime, arrs));
      upd = true;
    }
    this.currentArrivals = arrs;
    if (deps != this.currentDepartures) {
      this.maximumDepartures.add(new TimedValue<>(nowTime, deps));
      upd = true;
    }
    this.currentDepartures = deps;
    if (upd) {
      this.maximumPlanes.add(new TimedValue<>(nowTime, arrs + deps));
      upd = false;
    }
    if (aarrs != this.currentArrivalsUnderApp) {
      this.maximumArrivalsUnderApp.add(new TimedValue<>(nowTime, aarrs));
      upd = true;
    }
    this.currentArrivalsUnderApp = aarrs;
    if (adeps != this.currentDeparturesUnderApp) {
      this.maximumDeparturesUnderApp.add(new TimedValue<>(nowTime, adeps));
      upd = true;
    }
    this.currentDeparturesUnderApp = adeps;
    if (upd)
      this.maximumPlanesUnderApp.add(new TimedValue<>(nowTime, aarrs + adeps));

    EDayTimeStamp lastTime = Acc.now().addHours(-1);
    cleanTimedList(this.airproxErrors, lastTime);
    cleanTimedList(this.mrvaErrors, lastTime);
    cleanTimedList(this.planeDelays, lastTime);
    cleanTimedList(this.holdingPointMaximalCount, lastTime);
    cleanTimedList(this.holdingPointDelays, lastTime);
    cleanTimedList(this.maximumArrivals, lastTime);
    cleanTimedList(this.maximumDepartures, lastTime);
    cleanTimedList(this.maximumPlanes, lastTime);
    cleanTimedList(this.maximumArrivalsUnderApp, lastTime);
    cleanTimedList(this.maximumDeparturesUnderApp, lastTime);
    cleanTimedList(this.maximumPlanesUnderApp, lastTime);
    this.numberOfDepartures.remove(q -> q.isBefore(lastTime));
    this.numberOfLandings.remove(q -> q.isBefore(lastTime));
  }



//  public void registerFinishedPlane(Airplane plane) {
//      // recentStats have a method for this
//    if (plane.getFlightModule().isArrival())
//      this.finishedArrivals++;
//    else
//      this.finishedDepartures++;
//    planeDelays.add(new TimedValue<>(Acc.now().clone(), plane.getFlightModule().getDelayDifference()));
//  }

}
