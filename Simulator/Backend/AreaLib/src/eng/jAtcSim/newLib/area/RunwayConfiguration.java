package eng.jAtcSim.newLib.area;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Headings;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

import java.awt.geom.Line2D;

public class RunwayConfiguration {

  public static RunwayConfiguration createForThresholds(IList<ActiveRunwayThreshold> rts) {
    throw new UnsupportedOperationException("This must also includes somehow created crossedThresholdsSet, what is n ow in XmlModelBinder.");
//    IList<RunwayThresholdConfiguration> lst;
//    lst = rts.select(q -> new RunwayThresholdConfiguration(q));
//    RunwayConfiguration ret = new RunwayConfiguration(0, 359, 0, 999, lst, lst);
//    ret.bind();
//    return ret;
  }

  public static RunwayConfiguration load(XElement source, Airport airport) {
    RunwayConfiguration ret = new RunwayConfiguration();
    ret.read(source, airport);
    return ret;
  }

  private static IList<ISet<ActiveRunwayThreshold>> buildCrossedThresholdsSet(
      IList<RunwayThresholdConfiguration> departures,
      IList<RunwayThresholdConfiguration> arrivals) {
    // detection and saving the crossed runways
    IList<ISet<ActiveRunwayThreshold>> ret = new EList<>();

    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.add(departures.select(q -> q.getThreshold().getParent()).distinct());
    IList<ISet<ActiveRunway>> crossedRwys = new EList<>();
    while (rwys.isEmpty() == false) {
      ISet<ActiveRunway> set = new ESet<>();
      ActiveRunway r = rwys.get(0);
      rwys.removeAt(0);
      set.add(r);
      set.add(
          rwys.where(q -> isIntersectionBetweenRunways(r, q))
      );
      rwys.tryRemove(set);
      crossedRwys.add(set);
    }

    ISet<ActiveRunwayThreshold> set;
    for (ISet<ActiveRunway> crossedRwy : crossedRwys) {
      set = new ESet<>();
      for (ActiveRunway runway : crossedRwy) {
        set.add(runway.getThresholds());
      }
      ret.add(set);
    }

    return ret;
  }

  private static boolean isIntersectionBetweenRunways(ActiveRunway a, ActiveRunway b) {
    Line2D lineA = new Line2D.Float(
        (float) a.getThresholdA().getCoordinate().getLatitude().get(),
        (float) a.getThresholdA().getCoordinate().getLongitude().get(),
        (float) a.getThresholdB().getCoordinate().getLatitude().get(),
        (float) a.getThresholdB().getCoordinate().getLongitude().get());
    Line2D lineB = new Line2D.Float(
        (float) b.getThresholdA().getCoordinate().getLatitude().get(),
        (float) b.getThresholdA().getCoordinate().getLongitude().get(),
        (float) b.getThresholdB().getCoordinate().getLatitude().get(),
        (float) b.getThresholdB().getCoordinate().getLongitude().get());
    boolean ret = lineA.intersectsLine(lineB);
    return ret;
  }

  private static void checkAllCategoriesAreApplied(
      IList<RunwayThresholdConfiguration> departures,
      IList<RunwayThresholdConfiguration> arrivals) {
    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.add(departures.select(q -> q.getThreshold().getParent()).distinct());

    // check if all categories are applied
    for (char i = 'A'; i <= 'D'; i++) {
      char c = i;
      if (!arrivals.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find arrival threshold for category " + c);
      if (!departures.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find departure threshold for category " + c);
    }
  }

  private int windFrom;
  private int windTo;
  private int windSpeedFrom;
  private int windSpeedTo;
  private IList<RunwayThresholdConfiguration> arrivals;
  private IList<RunwayThresholdConfiguration> departures;
  private IList<ISet<ActiveRunwayThreshold>> crossedThresholdSets;

  private RunwayConfiguration() {
  }

  public boolean accepts(int heading, int speed) {
    boolean ret =
        Headings.isBetween(this.windFrom, heading, this.windTo)
            &&
            NumberUtils.isBetweenOrEqual(this.windSpeedFrom, speed, this.windSpeedTo);
    return ret;
  }

  public IReadOnlyList<RunwayThresholdConfiguration> getArrivals() {
    return arrivals;
  }

  public ISet<ActiveRunwayThreshold> getCrossedSetForThreshold(ActiveRunwayThreshold rt) {
    ISet<ActiveRunwayThreshold> ret = this.crossedThresholdSets.getFirst(q -> q.contains(rt));
    return ret;
  }

  public IReadOnlyList<RunwayThresholdConfiguration> getDepartures() {
    return departures;
  }

  public int getWindFrom() {
    return windFrom;
  }

  public int getWindSpeedFrom() {
    return windSpeedFrom;
  }

  public int getWindSpeedTo() {
    return windSpeedTo;
  }

  public int getWindTo() {
    return windTo;
  }

  public boolean isUsingTheSameRunwayConfiguration(RunwayConfiguration other) {
    boolean ret;
    if (this.arrivals.size() != other.arrivals.size())
      ret = false;
    else if (this.departures.size() != other.departures.size())
      ret = false;
    else if (this.arrivals.union(other.arrivals).distinct().size() != this.arrivals.size())
      ret = false;
    else if (this.arrivals.union(other.arrivals).distinct().size() != this.arrivals.size())
      ret = false;
    else
      ret = true;

    return ret;
  }

  public String toInfoString(String departureArrivalSeparator) {
    EStringBuilder sb = new EStringBuilder();

    sb.append("Departures - ");
    sb.appendItems(
        departures.select(q -> new Tuple<>(q.getThreshold().getName(), q.getCategories().toString())),
        q -> q.getA() + " for " + q.getB(),
        ", ");
    sb.append(departureArrivalSeparator);
    sb.append("Arrivals - ");
    sb.appendItems(
        arrivals.select(q -> new Tuple<>(q.getThreshold().getName(), q.getCategories().toString())),
        q -> q.getA() + " for " + q.getB(),
        ", ");

    return sb.toString();
  }

  private void read(XElement source, Airport airport) {
    XmlLoader.setContext(source);
    this.windFrom = XmlLoader.loadInteger("windFrom", 0);
    this.windTo = XmlLoader.loadInteger("windTo", 359);
    this.windSpeedFrom = XmlLoader.loadInteger("windSpeedFrom", 0);
    this.windSpeedTo = XmlLoader.loadInteger("windSpeedTo", 999);

    IList<RunwayThresholdConfiguration> deps = new EList<>();
    IList<RunwayThresholdConfiguration> arrs = new EList<>();

    for (XElement child : source.getChildren()) {
      RunwayThresholdConfiguration rtc = RunwayThresholdConfiguration.load(child, airport);
      switch (child.getName()) {
        case "departure":
          deps.add(rtc);
          break;
        case "arrivals":
          arrs.add(rtc);
          break;
        default:
          throw new EEnumValueUnsupportedException(child.getName());
      }
    }

    checkAllCategoriesAreApplied(deps, arrs);
    IList<ISet<ActiveRunwayThreshold>> cts = buildCrossedThresholdsSet(deps, arrs);

    RunwayConfiguration ret = new RunwayConfiguration();
    ret.windFrom = windFrom;
    ret.windTo = windTo;
    ret.windSpeedFrom = windSpeedFrom;
    ret.windSpeedTo = windSpeedTo;
    ret.arrivals = arrs;
    ret.departures = deps;
    ret.crossedThresholdSets = cts;
  }
}