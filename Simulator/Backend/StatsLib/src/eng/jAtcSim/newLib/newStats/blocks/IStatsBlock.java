package eng.jAtcSim.newLib.newStats.blocks;


import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newLib.newStats.values.IMMM;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public interface IStatsBlock {

  int getCoveredSecondsCount();

  EDayTimeStamp getStartTime();

  IMMM getAirproxErrors();

  IMMM getMrvaErrors();

  IMMM getFinishedArrivalDelays();

  IMMM getHoldingPointCounts();

  IMMM getHoldingPointDelays();

  IMMM getFinishedDeparturesCount();

  IMMM getFinishedArrivalsCount();

  IMMM getDeparturesCount();

  IMMM getArrivalsCount();

  IMMM getPlanesCount();

  IMMM getFinishedArrivalsMoodValues();

  IMMM getFinishedDeparturesMoodValues();

  IReadOnlyMap<AtcId, ? extends IMMM> getAppDeparturesCount();

  IReadOnlyMap<AtcId, ? extends IMMM> getAppArrivalsCount();

  IReadOnlyMap<AtcId, ? extends IMMM> getAppPlanesCount();

  default EDayTimeStamp getEndTime() {
    return getStartTime().addSeconds(getCoveredSecondsCount());
  }

}
