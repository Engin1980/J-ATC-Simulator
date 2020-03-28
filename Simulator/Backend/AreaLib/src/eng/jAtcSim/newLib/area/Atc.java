/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

/**
 * @author Marek
 */
public class Atc {

  public enum eType{
    gnd,
    twr,
    app,
    ctr
  }
  
  static class XmlReader{
    public static Atc load(XElement source) {
      Atc ret = new Atc();
      read(source,ret);
      return ret;
    }

    private static void read(XElement source, Atc atc) {
      XmlLoader.setContext(source);
      atc.type = XmlLoader.loadEnum("type", Atc.eType.class);
      atc.name = XmlLoader.loadString("name");
      atc.frequency = XmlLoader.loadDouble("frequency");
      atc.acceptAltitude = XmlLoader.loadInteger("acceptAltitude");
      atc.releaseAltitude = XmlLoader.loadInteger("releaseAltitude");
      atc.orderedAltitude = XmlLoader.loadInteger("orderedAltitude");
      atc.ctrAcceptDistance = XmlLoader.loadInteger("ctrAcceptDistance", null);
      atc.ctrNavaidAcceptDistance = XmlLoader.loadInteger("ctrNavaidAcceptDistance", null);
    }
  }

  private Atc.eType type;
  private String name;
  private double frequency;
  private int acceptAltitude;
  private int releaseAltitude;
  private int orderedAltitude;
  private Integer ctrAcceptDistance = null;
  private Integer ctrNavaidAcceptDistance = null;

  private Atc() {
  }

  public int getAcceptAltitude() {
    return acceptAltitude;
  }

  public Integer getCtrAcceptDistance() {
    return ctrAcceptDistance;
  }

  public Integer getCtrNavaidAcceptDistance() {
    return ctrNavaidAcceptDistance;
  }

  public double getFrequency() {
    return frequency;
  }

  public String getName() {
    return name;
  }

  public int getOrderedAltitude() {
    return orderedAltitude;
  }

  public int getReleaseAltitude() {
    return releaseAltitude;
  }

  public Atc.eType getType() {
    return type;
  }

}
