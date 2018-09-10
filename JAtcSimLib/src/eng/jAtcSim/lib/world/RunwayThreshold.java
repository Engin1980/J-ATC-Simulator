/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.approaches.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marek
 */
public class RunwayThreshold {

  @XmlItemElement(elementName = "ilsApproach", type = IlsApproach.class)
  @XmlItemElement(elementName = "gnssApproach", type = GnssApproach.class)
  @XmlItemElement(elementName = "unpreciseApproach", type = UnpreciseApproach.class)
  private final List<Approach> approaches = new ArrayList<>();
  @XmlOptional
  @XmlItemElement(elementName = "route", type = Route.class)
  private final IList<Route> routes = new EList<>();
  @XmlOptional
  String includeRoutesGroups = null;
  private String name;
  private Coordinate coordinate;
  @XmlIgnore
  private Runway parent;
  @XmlIgnore
  private double _course;
  private int initialDepartureAltitude;
  @XmlIgnore
  private RunwayThreshold _other;
  @XmlOptional // as inactive runway do not have this
  private boolean preferred = false;
  @XmlIgnore
  private Coordinate estimatedFafPoint;

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

  public List<Approach> getApproaches() {
    return approaches;
  }

  public IList<Route> getRoutes() {
    return routes;
  }

  public Runway getParent() {
    return parent;
  }

  public void setParent(Runway parent) {
    this.parent = parent;
  }

  public double getCourse() {
    return this._course;
  }


  public CurrentApproachInfo tryGetCurrentApproachInfo(Approach.ApproachType type, char category, Coordinate planePosition) {

    CurrentApproachInfo ret;
    if (type == Approach.ApproachType.visual)
      ret = Approach.createVisualApproachInfo(this, type, planePosition);
    else
      ret =
          Approach.tryGetCurrentApproachInfo(this.approaches, category, type, planePosition);
    return ret;
  }

  public Approach getHighestApproach() {
    Approach ret;

    ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof IlsApproach);
    if (ret == null)
      ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof GnssApproach);
    if (ret == null)
      ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof UnpreciseApproach);
    //TODO original idea was that visual approaches will be generated and put into list of approaches
    //now it is not done such way, so this method returns null
//    if (ret == null)
//      ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof VisualApproach);

//    assert ret != null;

    return ret;
  }

  public IList<RunwayThreshold> getParallelGroup() {
    IList<RunwayThreshold> ret = new EList<>();

    double crs = this.getCourse();
    for (Runway runway : this.getParent().getParent().getRunways()) {
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

  public Coordinate getEstimatedFafPoint() {
    return estimatedFafPoint;
  }

  @Override
  public String toString() {
    return this.getName() + "{rwyThr}";
  }

  public void bind() {
    this._other
        = this.getParent().getThresholdA().equals(this)
        ? this.getParent().getThresholdB()
        : this.getParent().getThresholdA();
    this._course
        = Coordinates.getBearing(this.coordinate, _other.coordinate);

    this.estimatedFafPoint = Coordinates.getCoordinate(
        this.coordinate,
        Headings.getOpposite(this._course),
        9);

    if (this.includeRoutesGroups != null) {
      String[] groupNames = this.includeRoutesGroups.split(";");
      for (String groupName : groupNames) {
        Airport.SharedRoutesGroup group = this.getParent().getParent().getSharedRoutesGroups().tryGetFirst(q -> q.groupName.equals(groupName));

        if (group == null)
          throw new EApplicationException("Unable to find route group named " + groupName + " in airport "
              + this.getParent().getParent().getIcao() + " required for runway threshold " + this.getName());

        this.routes.add(group.routes);
      }
    }
  }
}
