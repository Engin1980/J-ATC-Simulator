/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.traffic.Traffic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marek
 */
public class Airport implements KeyItem<String> {

  private final InitialPosition initialPosition = new InitialPosition();
  private final KeyList<Runway, String> runways = new KeyList();
  @XmlOptional
  private final KeyList<InactiveRunway, String> inactiveRunways = new KeyList();
  private final KeyList<AtcTemplate, Atc.eType> atcTemplates = new KeyList();
  private final KeyList<PublishedHold, Navaid> holds = new KeyList();
  @XmlOptional
  private final IList<Route> sharedRoutes = new EList<>();
  private String icao;
  private String name;
  private int altitude;
  private int transitionAltitude;
  private int vfrAltitude;
  private String mainAirportNavaidName;
  private Navaid _mainAirportNavaid;
  private List<Traffic> trafficDefinitions = new ArrayList<>();
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

  public KeyList<InactiveRunway, String> getInactiveRunways() {
    return inactiveRunways;
  }

  public int getAltitude() {
    return altitude;
  }

  public Area getParent() {
    return parent;
  }

  public void setParent(Area parent) {
    this.parent = parent;
  }

  public KeyList<PublishedHold, Navaid> getHolds() {
    return holds;
  }

  public List<Traffic> getTrafficDefinitions() {
    return trafficDefinitions;
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

  public int getVfrAltitude() {
    return vfrAltitude;
  }

  public Navaid getMainAirportNavaid() {
    if (this._mainAirportNavaid == null) {
      try {
        this._mainAirportNavaid = this.getParent().getNavaids().get(this.mainAirportNavaidName);
      } catch (ERuntimeException ex) {
        throw new EApplicationException("Failed to find main navaid named " + this.mainAirportNavaidName + " for aiport " + this.name + ". Invalid area file?", ex);
      }
    }

    return this._mainAirportNavaid;
  }

  public InitialPosition getInitialPosition() {
    return this.initialPosition;
  }

  public IList<Route> getSharedRoutes() {
    return sharedRoutes;
  }
}
