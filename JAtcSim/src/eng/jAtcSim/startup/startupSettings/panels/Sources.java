package eng.jAtcSim.startup.startupSettings.panels;

import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.Area;

class Sources {

  private static final EventAnonymousSimple areaChanged = new EventAnonymousSimple();
  private static final EventAnonymousSimple typesChanged = new EventAnonymousSimple();
  private static final EventAnonymousSimple fleetsChanged = new EventAnonymousSimple();
  private static Area area;
  private static AirplaneTypes types;
  private static Fleets fleets;

  public static EventAnonymousSimple getAreaChanged() {
    return areaChanged;
  }

  public static EventAnonymousSimple getTypesChanged() {
    return typesChanged;
  }

  public static EventAnonymousSimple getFleetsChanged() {
    return fleetsChanged;
  }

  public static Area getArea() {
    return area;
  }

  public static void setArea(Area area) {
    Sources.area = area;
    areaChanged.raise();
  }

  public static AirplaneTypes getTypes() {
    return types;
  }

  public static void setTypes(AirplaneTypes types) {
    Sources.types = types;
    typesChanged.raise();
  }

  public static Fleets getFleets() {
    return fleets;
  }

  public static void setFleets(Fleets fleets) {
    Sources.fleets = fleets;
    fleetsChanged.raise();
  }
}
