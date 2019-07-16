package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

public interface IPilot5Command extends IPilot5 {
  void setTargetAltitude(int altitudeInFt);
  void abortHolding();
  void setTargetCoordinate(Coordinate coordinate);
  void setTargetHeading(double targetHeading, boolean useLeftTurn);
  void setSpeedRestriction(Restriction speedRestriction);
  void processOrderedGoAround();
  void isFlyingOverNavaidInFuture(Navaid navaid);
  void applyShortcut(Navaid navaid);
  void startTakeOff(ActiveRunwayThreshold runwayThreshold);
  void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn);
  void setHasRadarContact();
  void setAltitudeRestriction(Restriction restriction);
  void processOrderedDivert();
  void setResponsibleAtc(Atc atc);
  void adviceGoAroundToAtcIfAny();
  void setRoute(Route route, ActiveRunwayThreshold runwayThreshold);
  void setApproachBehavior(NewApproachInfo newApproachInfo);
}
