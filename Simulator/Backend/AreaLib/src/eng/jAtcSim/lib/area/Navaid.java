/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.area;


import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.area.xml.XmlLoader;

/**
 * @author Marek
 */
public class Navaid {

  public enum eType {
    vor,
    ndb,
    fix,
    fixMinor,
    airport,
    auxiliary
  }

  public static final double SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER = 0.007;

  public static Navaid load(XElement source) {
    XmlLoader.setContext(source);
    Coordinate coordinate = XmlLoader.loadCoordinate("coordinate");
    String name = XmlLoader.loadString("name");
    eType type = XmlLoader.loadEnum("type", eType.class);

    Navaid ret = new Navaid(name, type, coordinate);
    return ret;
  }

  public static double getOverNavaidDistance(int speed) {
    return speed * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;
  }

  public static NavaidList loadList(IReadOnlyList<XElement> sources) {
    NavaidList ret = new NavaidList();

    for (XElement child : sources) {
      Navaid navaid = Navaid.load(child);
      ret.add(navaid);
    }

    return ret;
  }

  private Coordinate coordinate;
  private String name;
  private eType type;

  private Navaid(String name, eType type, Coordinate coordinate) {
    this.coordinate = coordinate;
    this.name = name;
    this.type = type;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

  @Override
  public String toString() {
    return name + " {" + type + '}';
  }
}
