/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

/**
 * @author Marek
 */
public class DARoute extends Route {

  public enum eType {

    sid,
    star,
    transition,
    vectoring;

    public boolean isArrival() {
      return this == star || this == transition;
    }
  }

  public static DARoute createNewVectoringByFix(Navaid n) {
    DARoute ret = new DARoute(eType.vectoring, n.getName() + "/v",
        PlaneCategoryDefinitions.getAll(),
        n, -1, null, new SpeechList<>(),
        new EList<>(), null, null, Acc.airport());

    return ret;
  }

  private final eType type;
  private final String name;
  private final Airport parent;
  private final PlaneCategoryDefinitions category;
  private final SpeechList<IAtcCommand> routeCommands;
  private final IList<Navaid> routeNavaids;
  private final double routeLength;
  private final Navaid mainNavaid;
  private final Integer entryFL;
  private final Integer maxMrvaFL;

  public DARoute(eType type, String name, PlaneCategoryDefinitions category, Navaid mainNavaid, double routeLength,
                 Integer entryFL, SpeechList<IAtcCommand> routeCommands, IList<Navaid> routeNavaids, Integer maxMrvaFL,
                 String mapping,
                 Airport parent) {
    super(mapping, routeCommands);
    this.type = type;
    this.name = name;
    this.parent = parent;
    this.category = category;
    this.routeCommands = routeCommands;
    this.routeNavaids = routeNavaids;
    this.routeLength = routeLength;
    this.mainNavaid = mainNavaid;
    this.entryFL = entryFL;
    this.maxMrvaFL = maxMrvaFL;
  }

  public PlaneCategoryDefinitions getCategory() {
    return this.category;
  }

  public SpeechList<IAtcCommand> getCommands() {
    return routeCommands;
  }

  public SpeechList<IAtcCommand> getCommandsListClone() {
    SpeechList<IAtcCommand> ret = new SpeechList<>();
    ret.add(routeCommands);
    return ret;
  }

  public Integer getEntryFL() {
    return entryFL;
  }

  public Navaid getMainNavaid() {
    return mainNavaid;
  }

  public int getMaxMrvaAltitude() {
    int ret = maxMrvaFL == null ? 0 : maxMrvaFL * 100;
    return ret;
  }

  public String getName() {
    return name;
  }

  public IReadOnlyList<Navaid> getNavaids() {
    return this.routeNavaids;
  }

  public Airport getParent() {
    return parent;
  }

  public double getRouteLength() {
    return routeLength;
  }

  public eType getType() {
    return type;
  }

  public boolean isValidForCategory(char categoryChar) {
    boolean ret = this.category.contains(categoryChar);
    return ret;
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }
}


