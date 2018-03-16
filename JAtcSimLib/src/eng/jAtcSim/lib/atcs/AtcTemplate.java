/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.atcs;

import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.global.KeyItem;

/**
 *
 * @author Marek
 */
public class AtcTemplate implements KeyItem<Atc.eType> {
  private Atc.eType type;
  private String name;
  private double frequency;
  private int acceptAltitude;
  private int releaseAltitude;
  private int orderedAltitude;
  @XmlOptional
  private Integer ctrAcceptDistance = null;
  @XmlOptional
  private Integer ctrNavaidAcceptDistance = null;

  @Override
  public Atc.eType getKey() {
    return type;
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
