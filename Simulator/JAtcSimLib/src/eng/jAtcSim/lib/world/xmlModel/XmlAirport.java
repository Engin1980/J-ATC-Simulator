package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.xml.RunwayConfigurationParser;

public class XmlAirport {
  public static class SharedRoutesGroup {
    public String groupName;
    @XmlOptional
    @XmlItemElement(elementName = "route", type = Route.class)
    public IList<Route> routes = new EList<>();
  }

  public static class SharedIafRoutesGroup {
    public String groupName;
    @XmlOptional
    @XmlItemElement(elementName = "route", type = IafRoute.class)
    public IList<IafRoute> iafRoutes = new EList<>();
  }

  public final XmlInitialPosition initialPosition = new InitialPosition();
  @XmlItemElement(elementName = "runway", type = ActiveRunway.class)
  public final IList<XmlActiveRunway> runways = new EList<>();
  @XmlOptional
  @XmlItemElement(elementName = "runway", type = InactiveRunway.class)
  public final IList<XmlInactiveRunway> inactiveRunways = new EList<>();
  @XmlItemElement(elementName = "atcTemplate", type = AtcTemplate.class)
  public final IList<XmlAtcTemplate> atcTemplates = new EList<>();
  @XmlItemElement(elementName = "hold", type = PublishedHold.class)
  public final IList<XmlPublishedHold> holds = new EList<>();
  @XmlOptional
  @XmlItemElement(elementName = "entryExitPoint", type = EntryExitPoint.class)
  public final IList<XmlEntryExitPoint> entryExitPoints = new EList<>();
  public String icao;
  public String name;
  public int altitude;
  public int transitionAltitude;
  @XmlOptional
  public int vfrAltitude = -1;
  public String mainAirportNavaidName;
  public double declination;
  @XmlIgnore
  public Area parent;
  public int coveredDistance;
  @XmlOptional
  @XmlItemElement(elementName = "configuration", type = RunwayConfiguration.class, parser = RunwayConfigurationParser.class)
  public IList<XmlRunwayConfiguration> runwayConfigurations = new EList<>();
  @XmlOptional
  @XmlItemElement(elementName = "sharedRoutesGroup", type= eng.jAtcSim.lib.world.Airport.SharedRoutesGroup.class)
  public IList<eng.jAtcSim.lib.world.Airport.SharedRoutesGroup> sharedRoutesGroups = new EList<>();
  @XmlOptional
  @XmlItemElement(elementName = "sharedIafRoutesGroup", type= eng.jAtcSim.lib.world.Airport.SharedIafRoutesGroup.class)
  public IList<eng.jAtcSim.lib.world.Airport.SharedIafRoutesGroup> sharedIafRoutesGroups = new EList<>();
  @XmlIgnore
  public IList<XmlRoute> routes;

  public IReadOnlyList<eng.jAtcSim.lib.world.Airport.SharedRoutesGroup> getSharedRoutesGroups() {
    return sharedRoutesGroups;
  }

  public IReadOnlyList<eng.jAtcSim.lib.world.Airport.SharedIafRoutesGroup> getSharedIafRoutesGroups() {
    return sharedIafRoutesGroups;
  }

  public IReadOnlyList<EntryExitPoint> getEntryExitPoints() {
    return entryExitPoints;
  }

  public int getCoveredDistance() {
    return coveredDistance;
  }

  public double getDeclination() {
    return declination;
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

  public IReadOnlyList<ActiveRunway> getRunways() {
    return runways;
  }

  public IReadOnlyList<InactiveRunway> getInactiveRunways() {
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

  public IReadOnlyList<PublishedHold> getHolds() {
    return holds;
  }

  public ActiveRunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    for (ActiveRunway r : runways) {
      for (ActiveRunwayThreshold t : r.getThresholds()) {
        if (t.getName().equals(runwayThresholdName)) {
          return t;
        }
      }
    }
    return null;
  }

  public IReadOnlyList<AtcTemplate> getAtcTemplates() {
    return atcTemplates;
  }

  public int getVfrAltitude() {
    return vfrAltitude;
  }

  public XmlNavaid getMainAirportNavaid() {
    if (this._mainAirportNavaid == null) {
      try {
        this._mainAirportNavaid = this.getParent().getNavaids().get(this.mainAirportNavaidName);
      } catch (ERuntimeException ex) {
        throw new EApplicationException("Failed to find main navaid named " + this.mainAirportNavaidName + " for aiport " + this.name + ". Invalid area file?", ex);
      }
    }

    return this._mainAirportNavaid;
  }

  public IReadOnlyList<ActiveRunwayThreshold> getAllThresholds() {
    IList<ActiveRunwayThreshold> ret = new EList<>();
    for (ActiveRunway runway : this.getRunways()) {
      for (ActiveRunwayThreshold threshold : runway.getThresholds()) {
        ret.add(threshold);
      }
    }
    return ret;
  }

  public IReadOnlyList<RunwayConfiguration> getRunwayConfigurations() {
    return runwayConfigurations;
  }

  public InitialPosition getInitialPosition() {
    return this.initialPosition;
  }

  public IReadOnlyList<Route> getRoutes() {
    return routes;
  }

  public void bind() {
    eng.jAtcSim.lib.world.Airport a = this;

    for (PublishedHold h : a.getHolds()) {
      h.bind();
    }

    // fill routes list
    this.routes = new EDistinctList<>(EDistinctList.Behavior.exception);
    this.sharedRoutesGroups.forEach(q -> this.routes.add(q.routes)); // adds shared routes
    this.getAllThresholds().forEach(q -> this.routes.add(q.getRoutes())); // adds threshold specific routes

    for (Route o : a.getRoutes()) {
      o.bind();
    }
    a.bindEntryExitPointsByRoutes();

    for (ActiveRunway r : a.getRunways()) {
      for (ActiveRunwayThreshold t : r.getThresholds()) {
        t.bind();

        for (Approach p : t.getApproaches()) {
          p.bind();
        }
      }

      for (EntryExitPoint eep : a.getEntryExitPoints()) {
        eep.bind();
      }
    }

    for (RunwayConfiguration runwayConfiguration : a.getRunwayConfigurations()) {
      runwayConfiguration.bind();
    }

  }

  public void bindEntryExitPointsByRoutes() {
    for (Route route : this.routes) {
      EntryExitPoint eep = new EntryExitPoint(
          route.getMainNavaid(),
          route.getType() == Route.eType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
          route.getMaxMrvaAltitude());

      mergeEntryExitPoints(eep);
    }
  }

  public void mergeEntryExitPoints(EntryExitPoint eep) {
    EntryExitPoint tmp = this.getEntryExitPoints().tryGetFirst(q -> q.getName().equals(eep.getNavaid().getName()));
    if (tmp == null)
      this.entryExitPoints.add(eep);
    else {
      tmp.adjustBy(eep);
    }
  }
}
