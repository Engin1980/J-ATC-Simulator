package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ToNavaidCommand;

public class DARoute extends Route {

  public static DARoute createNewVectoringByFix(Navaid n) {
    DARoute ret = new DARoute(new EList<>(),
        DARouteType.vectoring, n.getName() + "/v", PlaneCategoryDefinitions.getAll(),
        -1, n, null, null);
    return ret;
  }

  private final DARouteType type;
  private final String name;
  private final PlaneCategoryDefinitions category;
  private final double routeLength;
  private final Navaid mainNavaid;
  private final Integer entryAltitude;
  private final Integer maxMrvaAltitude;
  private final IReadOnlyList<Navaid> calculatedRouteNavaids;

  public DARoute(IList<ICommand> routeCommands, DARouteType type, String name,
                 PlaneCategoryDefinitions category, double routeLength,
                 Navaid mainNavaid, Integer entryAltitude, Integer maxMrvaAltitude) {
    super(routeCommands);
    this.type = type;
    this.name = name;
    this.category = category;
    this.routeLength = routeLength;
    this.mainNavaid = mainNavaid;
    this.entryAltitude = entryAltitude;
    this.maxMrvaAltitude = maxMrvaAltitude;
    this.calculatedRouteNavaids = calculateRouteNavaids();
  }

  public IReadOnlyList<Navaid> getRouteNavaids() {
    return calculatedRouteNavaids;
  }

  public PlaneCategoryDefinitions getCategory() {
    return this.category;
  }

  public Integer getEntryAltitude() {
    return entryAltitude;
  }

  public Navaid getMainNavaid() {
    return mainNavaid;
  }

  public int getMaxMrvaAltitude() {
    int ret = maxMrvaAltitude == null ? 0 : maxMrvaAltitude * 100;
    return ret;
  }

  public String getName() {
    return name;
  }

  public double getRouteLength() {
    return routeLength;
  }

  public DARouteType getType() {
    return type;
  }

  public boolean isValidForCategory(char categoryChar) {
    boolean ret = this.category.contains(categoryChar);
    return ret;
  }

  private IReadOnlyList<Navaid> calculateRouteNavaids(){
    IList<String> navaidNames = this.getRouteCommands()
        .whereItemClassIs(ToNavaidCommand.class,true)
        .select(q->q.getNavaidName());
    IList<Navaid> ret = navaidNames.select(q-> AreaAcc.getNavaids().get(q));
    return ret;
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }
}
