/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.global.MustBeBinded;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marek
 */
public class RunwayThreshold extends MustBeBinded implements KeyItem<String> {

  @XmlOptional // as inactive runway do not have this
  private final KeyList<Approach, Approach.eType> approaches = new KeyList();
  @XmlOptional // as inactive runway do not have this
  private final KeyList<Route, String> routes = new KeyList();
  private String name;
  private Coordinate coordinate;
  private Runway parent;
  private double _course;
  @XmlOptional // as inactive runway do not have this
  private int initialDepartureAltitude;
  private RunwayThreshold _other;
  @XmlOptional // as inactive runway do not have this
  private boolean preferred = false;
  @XmlOptional // as inactive runway do not have this
  private Coordinate fafCross;

  public int getInitialDepartureAltitude() {
    return initialDepartureAltitude;
  }

  public boolean isPreferred() {
    return preferred;
  }

  public String getName() {
    return name;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public Coordinate getFafCross() {
    return fafCross;
  }

  public KeyList<Approach, Approach.eType> getApproaches() {
    return approaches;
  }

  public KeyList<Route, String> getRoutes() {
    return routes;
  }

  @Override
  public String getKey() {
    return getName();
  }

  public Runway getParent() {
    return parent;
  }

  public void setParent(Runway parent) {
    this.parent = parent;
  }

  public double getCourse() {
    checkBinded();

    return this._course;
  }

  public Approach tryGetApproachByTypeWithILSDerived(Approach.eType type) {
    Approach ret = getApproaches().tryGet(type);
    if (ret == null) {
      switch (type) {
        case ILS_I:
          ret = tryGetApproachByTypeWithILSDerived(Approach.eType.ILS_II);
          break;
        case ILS_II:
          ret = tryGetApproachByTypeWithILSDerived(Approach.eType.ILS_III);
      }
    }
    return ret;
  }

  public Approach getHighestApproach() {
    Approach ret;

    ret = tryGetApproachByTypeWithILSDerived(Approach.eType.ILS_I);
    if (ret == null) {
      ret = approaches.tryGet(Approach.eType.GNSS);
    }
    if (ret == null) {
      ret = approaches.tryGet(Approach.eType.VORDME);
    }
    if (ret == null) {
      ret = approaches.tryGet(Approach.eType.NDB);
    }

    return ret;
  }

  public List<RunwayThreshold> getParallelGroup() {
    List<RunwayThreshold> ret = new ArrayList<>();

    double crs = this.getCourse();
    for (Runway runway : this.getParent().getParent().getRunways()) {
      if (runway.isActive() == false) continue;
      for (RunwayThreshold threshold : runway.getThresholds()) {
        //TODO this may fail for
        if (Headings.isBetween(crs - 1, threshold.getCourse(), crs + 1)) {
          ret.add(threshold);
          break;
        }
      }
    }

    assert ret.contains(this) : "Parallel thresholds group should contains the invoking one.";

    return ret;
  }

  public RunwayThreshold getOtherThreshold() {
    return _other;
  }

  @Override
  protected void _bind() {
    this._other
        = this.getParent().getThresholdA().equals(this)
        ? this.getParent().getThresholdB()
        : this.getParent().getThresholdA();
    this._course
        = Coordinates.getBearing(this.coordinate, _other.coordinate);
  }

  @Override
  public String toString() {
    return this.getName() + "{rwyThr}";
  }
}
