/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.validation.EAssert;

/**
 * @author Marek
 */
public class Atc {

  public enum eType {
    gnd,
    twr,
    app,
    ctr
  }

  public static Atc create(String name, eType type, double frequency, int acceptAltitude, int releaseAltitude, int orderedAltitude, Integer ctrAcceptDistance, Integer ctrNavaidAcceptDistance) {
    Atc ret = new Atc(name, type, frequency, acceptAltitude, releaseAltitude, orderedAltitude,
        ctrAcceptDistance, ctrNavaidAcceptDistance);
    return ret;
  }

  private Atc.eType type;
  private String name;
  private double frequency;
  private int acceptAltitude;
  private int releaseAltitude;
  private int orderedAltitude;
  private Integer ctrAcceptDistance;
  private Integer ctrNavaidAcceptDistance;

  private Atc(String name, eType type, double frequency, int acceptAltitude, int releaseAltitude, int orderedAltitude, Integer ctrAcceptDistance, Integer ctrNavaidAcceptDistance) {
    EAssert.Argument.isNotNull(name);
    EAssert.Argument.isTrue(frequency > 100 && frequency < 150);
    EAssert.Argument.isTrue(acceptAltitude > 0);
    EAssert.Argument.isTrue(releaseAltitude > 0);
    EAssert.Argument.isTrue(acceptAltitude > releaseAltitude);
    EAssert.Argument.isTrue(orderedAltitude > 0);
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

  public Atc.eType getType() {
    return type;
  }

}
