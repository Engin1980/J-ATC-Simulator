package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

public interface IPilot5Command {
  void abortHolding();

  void applyShortcut(Navaid navaid);

  boolean isFlyingOverNavaidInFuture(Navaid navaid);

  void processOrderedDivert();

  void processOrderedGoAround();

  void setAltitudeRestriction(Restriction restriction);

  void setApproachBehavior(NewApproachInfo newApproachInfo);

  void setHasRadarContact();

  void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn);

  void setResponsibleAtc(Atc atc);

  void setRoute(Route route, ActiveRunwayThreshold runwayThreshold);

  void setSpeedRestriction(Restriction speedRestriction);

  void startTakeOff(ActiveRunwayThreshold runwayThreshold);
}
