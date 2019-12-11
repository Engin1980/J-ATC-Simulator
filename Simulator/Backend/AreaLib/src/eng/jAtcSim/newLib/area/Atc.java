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

  public static Atc load(XElement source) {
    Atc ret = new Atc();
    ret.read(source);
    return ret;
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

  private void read(XElement source) {
    XmlLoader.setContext(source);
    this.type = XmlLoader.loadEnum("type", Atc.eType.class);
    this.name = XmlLoader.loadString("name");
    this.frequency = XmlLoader.loadDouble("frequency");
    this.acceptAltitude = XmlLoader.loadInteger("acceptAltitude");
    this.releaseAltitude = XmlLoader.loadInteger("releaseAltitude");
    this.orderedAltitude = XmlLoader.loadInteger("orderedAltitude");
    this.ctrAcceptDistance = XmlLoader.loadInteger("ctrAcceptDistance", null);
    this.ctrNavaidAcceptDistance = XmlLoader.loadInteger("ctrNavaidAcceptDistance", null);
  }
}
