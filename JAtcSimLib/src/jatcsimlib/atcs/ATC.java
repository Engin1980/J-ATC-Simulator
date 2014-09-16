/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

import jatcsimlib.airplanes.Airplane;

/**
 *
 * @author Marek
 */
public abstract class Atc {
  
  public enum eType{
    gnd,
    twr,
    app,
    ctr
  }
  
  private final eType type;
  
  private final String name;
  private final double frequency;
  protected final int acceptAltitude;
  protected final int releaseAltitude;
  protected final int orderedAltitude;

  public Atc(AtcTemplate template) {
    this.type = template.getType();
    this.name = template.getName();
    this.frequency = template.getFrequency();
    this.acceptAltitude = template.getAcceptAltitude();
    this.releaseAltitude = template.getReleaseAltitude();
    this.orderedAltitude = template.getOrderedAltitude();
  }
  
  public abstract boolean isHuman();

  public eType getType() {
    return type;
  }

  public String getName() {
    return name;
  }
  
  public abstract boolean isControllingAirplane(Airplane plane);
  
  public void registerNewPlane (Airplane plane){
    plane.visuallyResponsibleAtc = this;
    _registerNewPlane(plane);
  }
  protected abstract void _registerNewPlane(Airplane plane);

  public int getReleaseAltitude() {
    return releaseAltitude;
  }

  public double getFrequency() {
    return frequency;
  }

  public int getAcceptAltitude() {
    return acceptAltitude;
  }

  public int getOrderedAltitude() {
    return orderedAltitude;
  }

  @Override
  public String toString() {
    return this.name;
  }

  
}
