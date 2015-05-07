/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.AtcTemplate;
import jatcsimlib.coordinates.RadarRange;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.KeyList;
import jatcsimlib.global.TrafficCategories;

/**
 *
 * @author Marek
 */
public class Airport implements KeyItem<String> {

  private String icao;
  private String name;
  private int altitude;
  private int transitionAltitude;
  private final RadarRange radarRange = new RadarRange();
  private final KeyList<Runway, String> runways = new KeyList();
  private final KeyList<AtcTemplate, Atc.eType> atcTemplates = new KeyList();
  private final KeyList<PublishedHold, Navaid> holds = new KeyList();
  private TrafficCategories trafficCategories;
  
  private Area parent;

  @Override
  public String getKey() {
    return icao;
  }

  public String getIcao() {
    return icao;
  }

  public String getName() {
    return name;
  }

  public int getTransitionAltitude() {
    return transitionAltitude;
  }

  public Coordinate getLocation() {
    return runways.get(0).getThresholdA().getCoordinate();
  }

  public KeyList<Runway, String> getRunways() {
    return runways;
  }

  public int getAltitude() {
    return altitude;
  }

  public RadarRange getRadarRange() {
    return radarRange;
  }

  public Area getParent() {
    return parent;
  }

  public KeyList<PublishedHold, Navaid> getHolds() {
    return holds;
  }

  public void setParent(Area parent) {
    this.parent = parent;
  }

  public RunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    for (Runway r : runways) {
      for (RunwayThreshold t : r.getThresholds()) {
        if (t.getName().equals(runwayThresholdName)) {
          return t;
        }
      }
    }
    return null;
  }

  public KeyList<AtcTemplate, Atc.eType> getAtcTemplates() {
    return atcTemplates;
  }
  
  public TrafficCategories getTrafficCategories(){
    return this.trafficCategories;
  }

}
