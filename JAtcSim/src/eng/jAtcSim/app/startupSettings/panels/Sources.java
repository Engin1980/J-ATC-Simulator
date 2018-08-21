package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.Area;

class Sources {

  private static final EventAnonymousSimple onAreaChanged = new EventAnonymousSimple();
  private static final EventAnonymousSimple onTypesChanged = new EventAnonymousSimple();
  private static final EventAnonymousSimple onFleetsChanged = new EventAnonymousSimple();
  private static final EventAnonymousSimple onTrafficChanged = new EventAnonymousSimple();
  private static Area area;
  private static AirplaneTypes types;
  private static Fleets fleets;
  private static IList<Traffic> traffics;

  public static EventAnonymousSimple getOnAreaChanged() {
    return onAreaChanged;
  }

  public static EventAnonymousSimple getOnTypesChanged() {
    return onTypesChanged;
  }

  public static EventAnonymousSimple getOnFleetsChanged() {
    return onFleetsChanged;
  }

  public static EventAnonymousSimple getOnTrafficChanged() {
    return onTrafficChanged;
  }

  public static Area getArea() {
    return area;
  }

  public static void setArea(Area area) {
    Sources.area = area;
    onAreaChanged.raise();
  }

  public static void setTraffic(IList<Traffic> traffics) {
    Sources.traffics = traffics;
    onTrafficChanged.raise();
  }

  public static AirplaneTypes getTypes() {
    return types;
  }

  public static void setTypes(AirplaneTypes types) {
    Sources.types = types;
    onTypesChanged.raise();
  }

  public static Fleets getFleets() {
    return fleets;
  }

  public static IReadOnlyList<Traffic> getTraffics() {
    return traffics;
  }

  public static void setFleets(Fleets fleets) {
    Sources.fleets = fleets;
    onFleetsChanged.raise();
  }
}
