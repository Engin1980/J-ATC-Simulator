/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.area;


import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

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
    Navaid ret = new Navaid();
    ret.read(source);
    return ret;
  }

  private void read(XElement source){
    XmlLoader.setContext(source);
    this. coordinate = XmlLoader.loadCoordinate("coordinate");
    this. name = XmlLoader.loadString("name");
    this. type = XmlLoader.loadEnum("type", eType.class);
  }

  public static double getOverNavaidDistance(int speed) {
    return speed * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;
  }

  private Coordinate coordinate;
  private String name;
  private eType type;

  private Navaid(){}

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
