/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AtcType;

/**
 * @author Marek
 */
public class Atc {

  public static Atc create(String name, AtcType type, double frequency, int acceptAltitude, int releaseAltitude, int orderedAltitude, Integer ctrAcceptDistance, Integer ctrNavaidAcceptDistance) {
    Atc ret = new Atc(name, type, frequency, acceptAltitude, releaseAltitude, orderedAltitude,
        ctrAcceptDistance, ctrNavaidAcceptDistance);
    return ret;
  }

  private final AtcType type;
  private final String name;
  private final double frequency;
  private final int acceptAltitude;
  private final int releaseAltitude;
  private final int orderedAltitude;
  private final Integer ctrAcceptDistance;
  private final Integer ctrNavaidAcceptDistance;

  private Atc(String name, AtcType type, double frequency, int acceptAltitude, int releaseAltitude, int orderedAltitude, Integer ctrAcceptDistance, Integer ctrNavaidAcceptDistance) {
    EAssert.Argument.isNotNull(name);
    EAssert.Argument.isTrue(frequency > 100 && frequency < 150);
    //TODO do the check in some different way, like minimal/maximal allowed altitude or something like that
    if (type != AtcType.app) EAssert.Argument.isTrue(acceptAltitude > 0);
    if (type != AtcType.app) EAssert.Argument.isTrue(releaseAltitude > 0);
    if (type != AtcType.app) EAssert.Argument.isTrue(orderedAltitude > 0);
    this.type = type;
    this.name = name;
    this.frequency = frequency;
    this.acceptAltitude = acceptAltitude;
    this.releaseAltitude = releaseAltitude;
    this.orderedAltitude = orderedAltitude;
    this.ctrAcceptDistance = ctrAcceptDistance;
    this.ctrNavaidAcceptDistance = ctrNavaidAcceptDistance;
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

  public AtcType getType() {
    return type;
  }

}
