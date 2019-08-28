/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.atcs;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.world.xml.XmlLoader;

/**
 * @author Marek
 */
public class AtcTemplate {

  public static IList<AtcTemplate> loadList(IReadOnlyList<XElement> sources){
    IList<AtcTemplate> ret = new EList<>();

    for (XElement source : sources) {
      AtcTemplate atcTemplate = AtcTemplate.load(source);
      ret.add(atcTemplate);
    }

    return ret;
  }

  public static AtcTemplate load(XElement source){
    XmlLoader.setContext(source);
    Atc.eType type = XmlLoader.loadEnum("type", Atc.eType.class);
    String name = XmlLoader.loadString("name");
    double frequency = XmlLoader.loadDouble("frequency");
    int acceptAltitude = XmlLoader.loadInteger("acceptAltitude");
    int releaseAltitude = XmlLoader.loadInteger("releaseAltitude");
    int orderedAltitude = XmlLoader.loadInteger("orderedAltitude");
    Integer ctrAcceptDistance = XmlLoader.loadInteger("ctrAcceptDistance", null);
    Integer ctrNavaidAcceptDistance = XmlLoader.loadInteger("ctrNavaidAcceptDistance", null);

    AtcTemplate ret = new AtcTemplate(type, name, frequency,acceptAltitude,
        releaseAltitude,orderedAltitude,ctrAcceptDistance, ctrNavaidAcceptDistance);
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

  private AtcTemplate(Atc.eType type, String name, double frequency, int acceptAltitude, int releaseAltitude, int orderedAltitude, Integer ctrAcceptDistance, Integer ctrNavaidAcceptDistance) {
    this.type = type;
    this.name = name;
    this.frequency = frequency;
    this.acceptAltitude = acceptAltitude;
    this.releaseAltitude = releaseAltitude;
    this.orderedAltitude = orderedAltitude;
    this.ctrAcceptDistance = ctrAcceptDistance;
    this.ctrNavaidAcceptDistance = ctrNavaidAcceptDistance;
  }

  public Atc.eType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public double getFrequency() {
    return frequency;
  }

  public int getAcceptAltitude() {
    return acceptAltitude;
  }

  public int getReleaseAltitude() {
    return releaseAltitude;
  }

  public int getOrderedAltitude() {
    return orderedAltitude;
  }

  public Integer getCtrAcceptDistance() {
    return ctrAcceptDistance;
  }

  public Integer getCtrNavaidAcceptDistance() {
    return ctrNavaidAcceptDistance;
  }
}
