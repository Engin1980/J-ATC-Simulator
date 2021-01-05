package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import exml.IXPersistable;
import exml.XContext;

public interface IAirplane extends IXPersistable {
  IAirplaneAtc getAtc();

  Callsign getCallsign();

  Coordinate getCoordinate();

  IAirplaneFlight getFlight();

  IAirplaneRouting getRouting();

  IAirplaneSHA getSha();

  Squawk getSqwk();

  AirplaneState getState();

  AirplaneType getType();

  boolean hasElapsedEmergencyTime();

  boolean isArrival();

  boolean isEmergency();

  GoingAroundNotification.GoAroundReason pullLastGoAroundReasonIfAny();

  default boolean isDeparture() {
    return !isArrival();
  }

  @Override
  default void load(XElement elm, XContext ctx) {

  }

  @Override
  default void save(XElement elm, XContext ctx) {
    elm.setContent(this.getCallsign().toString(true));
  }
}
