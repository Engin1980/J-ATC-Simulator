/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;

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

  private eType type;
  private String name;
  private Airport parent;
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();
  private SpeechList<IAtcCommand> routeCommands = null;
  private IList<Navaid> routeNavaids = null;
  private double routeLength = -1;
  private Navaid mainNavaid = null;
  private Integer entryFL = null;
  private Integer maxMrvaFL = null;

  public Route(eType type, String name, PlaneCategoryDefinitions category, Navaid mainNavaid, double routeLength, Integer entryFL, SpeechList<IAtcCommand> routeCommands, IList<Navaid> routeNavaids, Integer maxMrvaFL, Airport parent) {
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

  public static Route createNewVectoringByFix(Navaid n, boolean arrival) {
    Route ret = new Route();

    ret.name = n.getName() + "/v";

    ret.routeCommands = new SpeechList<>();
    ret.mainNavaid = n;
    ret.type = eType.vectoring;
    ret.parent = Acc.airport(); // only formal for binding
    ret.bind();

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

  public void setParent(Airport airport) {
    this.parent = airport;
  }

}


