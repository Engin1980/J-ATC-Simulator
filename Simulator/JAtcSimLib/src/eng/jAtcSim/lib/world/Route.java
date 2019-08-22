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
public class Route {

  public enum eType {

    sid,
    star,
    transition,
    vectoring;

    public boolean isArrival() {
      return this == star || this == transition;
    }
  }

  public static Route createNewVectoringByFix(Navaid n) {
    Route ret = new Route(eType.vectoring, n.getName() + "/v",
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
  private final IList<String> mapping;

  public Route(eType type, String name, PlaneCategoryDefinitions category, Navaid mainNavaid, double routeLength,
               Integer entryFL, SpeechList<IAtcCommand> routeCommands, IList<Navaid> routeNavaids, Integer maxMrvaFL,
               IList<String> mapping,
               Airport parent) {
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
    this.mapping = mapping;
  }

  public boolean mappingAccepts(String mapped){
    IList<String> mappedList = new EList<>(mapped.split(";"));
    return this.mappingAccepts(mappedList);
  }

  public boolean mappingAccepts(IList<String> mapped){
    boolean ret = this.mapping.isAny(q -> mapped.contains(q));
    return ret;
  }

  public Integer getEntryFL() {
    return entryFL;
  }

  public int getMaxMrvaAltitude() {
    int ret = maxMrvaFL == null ? 0 : maxMrvaFL * 100;
    return ret;
  }

  public eType getType() {
    return type;
  }

  public double getRouteLength() {
    return routeLength;
  }

  public String getName() {
    return name;
  }

  public Airport getParent() {
    return parent;
  }

  public PlaneCategoryDefinitions getCategory() {
    return this.category;
  }

  public boolean isValidForCategory(char categoryChar) {
    boolean ret = this.category.contains(categoryChar);
    return ret;
  }

  public SpeechList<IAtcCommand> getCommands() {
    return routeCommands;
  }

  public SpeechList<IAtcCommand> getCommandsListClone() {
    SpeechList<IAtcCommand> ret = new SpeechList<>();
    ret.add(routeCommands);
    return ret;
  }

  public IReadOnlyList<Navaid> getNavaids() {
    return this.routeNavaids;
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }

  public Navaid getMainNavaid() {
    return mainNavaid;
  }
}


