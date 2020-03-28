/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

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
  
  static class XmlLoader {
    public static Atc load(XElement source) {
      Atc ret = new Atc();
      read(source,ret);
      return ret;
    }

    private static void read(XElement source, Atc atc) {
      XmlLoaderUtils.setContext(source);
      atc.type = XmlLoaderUtils.loadEnum("type", Atc.eType.class);
      atc.name = XmlLoaderUtils.loadString("name");
      atc.frequency = XmlLoaderUtils.loadDouble("frequency");
      atc.acceptAltitude = XmlLoaderUtils.loadInteger("acceptAltitude");
      atc.releaseAltitude = XmlLoaderUtils.loadInteger("releaseAltitude");
      atc.orderedAltitude = XmlLoaderUtils.loadInteger("orderedAltitude");
      atc.ctrAcceptDistance = XmlLoaderUtils.loadInteger("ctrAcceptDistance", null);
      atc.ctrNavaidAcceptDistance = XmlLoaderUtils.loadInteger("ctrNavaidAcceptDistance", null);
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
