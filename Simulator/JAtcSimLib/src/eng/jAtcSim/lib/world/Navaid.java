/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;


import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.world.xml.XmlLoader;

/**
 * @author Marek
 */
public class Navaid {

  public static Navaid load(XElement source){
    Coordinate coordinate = XmlLoader.loadCoordinate(source, "coordinate");
    String name = XmlLoader.loadString(source, "name");
    eType type = XmlLoader.loadEnum(source, "type", eType.class);

    Navaid ret = new Navaid(name, type, coordinate);
    return ret;
  }

  public enum eType {
    vor,
    ndb,
    fix,
    fixMinor,
    airport,
    auxiliary
  }
  public static final double SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER = 0.007;

  public static double getOverNavaidDistance(int speed) {
    return speed * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;
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
