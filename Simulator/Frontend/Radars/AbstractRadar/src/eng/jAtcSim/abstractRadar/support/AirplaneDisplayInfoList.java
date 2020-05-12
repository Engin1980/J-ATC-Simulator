package eng.jAtcSim.abstractRadar.support;

import eng.eSystem.collections.*;
import eng.jAtcSim.abstractRadar.published.IAirplaneInfo;
import eng.jAtcSim.newLib.shared.Callsign;

public class AirplaneDisplayInfoList {

  private final IMap<Callsign, AirplaneDisplayInfo> inner = new EMap<>();

  public ICollection<AirplaneDisplayInfo> getList() {
    return inner.getValues();
  }

  public boolean isEmpty() {
    return inner.isEmpty();
  }

  public void update(IReadOnlyList<IAirplaneInfo> planes) {
    resetWasUpdatedFlag();

    for (IAirplaneInfo plane : planes) {
      AirplaneDisplayInfo adi = tryGetOrAdd(plane);
      adi.updateInfo(plane);
    }

    removeUnupdated();
  }

  private void removeUnupdated() {
    // todo rewrite with ISet.remove(predicate) function
    ISet<Callsign> toRem =
        inner.getKeys().where(q -> inner.get(q).wasUpdatedFlag == false);
    toRem.forEach(q -> inner.remove(q));
  }

  private void resetWasUpdatedFlag() {
    inner.forEach(q -> q.getValue().wasUpdatedFlag = false);
  }

  private AirplaneDisplayInfo tryGetOrAdd(IAirplaneInfo plane) {
    AirplaneDisplayInfo ret;
    if (inner.containsKey(plane.callsign()))
      ret = inner.get(plane.callsign());
    else {
      ret = new AirplaneDisplayInfo(plane);
      inner.set(plane.callsign(), ret);
    }
    return ret;
  }
}