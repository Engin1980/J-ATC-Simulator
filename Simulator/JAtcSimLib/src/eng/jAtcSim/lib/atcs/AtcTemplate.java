/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.atcs;

/**
 * @author Marek
 */
public class AtcTemplate {
  private Atc.eType type;
  private String name;
  private double frequency;
  private int acceptAltitude;
  private int releaseAltitude;
  private int orderedAltitude;
  private Integer ctrAcceptDistance = null;
  private Integer ctrNavaidAcceptDistance = null;

  public AtcTemplate(Atc.eType type, String name, double frequency, int acceptAltitude, int releaseAltitude, int orderedAltitude, Integer ctrAcceptDistance, Integer ctrNavaidAcceptDistance) {
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
