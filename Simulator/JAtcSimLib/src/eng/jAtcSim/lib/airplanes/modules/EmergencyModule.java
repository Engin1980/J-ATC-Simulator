package eng.jAtcSim.lib.airplanes.modules;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.modules.IEmergencyModuleRO;
import eng.jAtcSim.lib.global.ETime;

public class EmergencyModule implements IEmergencyModuleRO {
  private final Airplane parent;
  private ETime emergencyWanishTime = null;

  public EmergencyModule(Airplane parent) {
    this.parent = parent;
  }

  public boolean isEmergency() {
    return this.emergencyWanishTime != null;
  }

  public void raiseEmergency() {
    int minsE = Acc.rnd().nextInt(5, 60);
    double distToAip = Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.airport().getLocation());
    int minA = (int) (distToAip / 250d * 60);
    ETime wt = Acc.now().addMinutes(minsE + minA);

    int alt = Math.max((int) parent.getAltitude(), Acc.airport().getAltitude() + 4000);
    alt = (int) NumberUtils.ceil(alt, 3);
    parent.getSha().setTargetAltitude(alt);

    this.emergencyWanishTime = wt;
    parent.getFlightModule().raiseEmergency();
    parent.getPilot().raiseEmergency();
  }

  public boolean hasElapsedEmergencyTime() {

    assert this.emergencyWanishTime != null;
    boolean ret = this.emergencyWanishTime.isBefore(Acc.now());
    return ret;
  }
}
