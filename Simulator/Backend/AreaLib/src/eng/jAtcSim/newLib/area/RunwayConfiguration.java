package eng.jAtcSim.newLib.area;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.geo.Headings;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;

public class RunwayConfiguration implements IXPersistable {

  public static RunwayConfiguration createForThresholds(IReadOnlyList<ActiveRunwayThreshold> rts) {
    IList<RunwayThresholdConfiguration> lst;
    lst = rts.select(q -> new RunwayThresholdConfiguration(
            PlaneCategoryDefinitions.getAll(),
            q,
            false,
            true,
            true));
    RunwayConfiguration ret = new RunwayConfiguration(0, 359, 0, 999, lst, lst);
    ret.bind();
    return ret;
  }

  private static boolean isIntersectionBetweenRunways(ActiveRunway a, ActiveRunway b) {
//    Line2D lineA = new Line2D.Float(
//        (float) a.getThresholdA().getCoordinate().getLatitude().get(),
//        (float) a.getThresholdA().getCoordinate().getLongitude().get(),
//        (float) a.getThresholdB().getCoordinate().getLatitude().get(),
//        (float) a.getThresholdB().getCoordinate().getLongitude().get());
//    Line2D lineB = new Line2D.Float(
//        (float) b.getThresholdA().getCoordinate().getLatitude().get(),
//        (float) b.getThresholdA().getCoordinate().getLongitude().get(),
//        (float) b.getThresholdB().getCoordinate().getLatitude().get(),
//        (float) b.getThresholdB().getCoordinate().getLongitude().get());
//    boolean ret = lineA.intersectsLine(lineB);

    boolean ret = LineUtils.linesIntersect(
            //l1-from
            (float) a.getThresholdA().getCoordinate().getLatitude().get(),
            (float) a.getThresholdA().getCoordinate().getLongitude().get(),
            //l1-to
            (float) a.getThresholdB().getCoordinate().getLatitude().get(),
            (float) a.getThresholdB().getCoordinate().getLongitude().get(),
            //l2-from
            (float) b.getThresholdA().getCoordinate().getLatitude().get(),
            (float) b.getThresholdA().getCoordinate().getLongitude().get(),
            //l2-to
            (float) b.getThresholdB().getCoordinate().getLatitude().get(),
            (float) b.getThresholdB().getCoordinate().getLongitude().get());
    return ret;
  }
  private final IList<RunwayThresholdConfiguration> arrivals;
  private IList<ISet<ActiveRunwayThreshold>> crossedThresholdSets = null;
  private final IList<RunwayThresholdConfiguration> departures;
  private final int windFrom;
  private final int windSpeedFrom;
  private final int windSpeedTo;
  private final int windTo;

  @XmlConstructor
  public RunwayConfiguration() {
    arrivals = null;
    departures = null;
    windFrom = 0;
    windSpeedFrom = 0;
    windSpeedTo = 0;
    windTo = 0;
  }

  public RunwayConfiguration(int windFrom, int windTo, int windSpeedFrom, int windSpeedTo,
                             IList<RunwayThresholdConfiguration> arrivals,
                             IList<RunwayThresholdConfiguration> departures) {
    this.windFrom = windFrom;
    this.windTo = windTo;
    this.windSpeedFrom = windSpeedFrom;
    this.windSpeedTo = windSpeedTo;
    this.arrivals = arrivals;
    this.departures = departures;
  }

  public boolean accepts(int heading, int speed) {
    boolean ret =
            Headings.isBetween(this.windFrom, heading, this.windTo)
                    &&
                    NumberUtils.isBetweenOrEqual(this.windSpeedFrom, speed, this.windSpeedTo);
    return ret;
  }

  public void bind() {
    this.crossedThresholdSets = buildCrossedThresholdsSet();
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

  @Deprecated
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

  private IList<ISet<ActiveRunwayThreshold>> buildCrossedThresholdsSet() {
    // detection and saving the crossed runways
    IList<ISet<ActiveRunwayThreshold>> ret = new EList<>();

    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.addMany(this.arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.addMany(this.departures.select(q -> q.getThreshold().getParent()).distinct());
    IList<ISet<ActiveRunway>> crossedRwys = new EList<>();
    while (rwys.isEmpty() == false) {
      ISet<ActiveRunway> set = new ESet<>();
      ActiveRunway r = rwys.get(0);
      rwys.removeAt(0);
      set.add(r);
      set.add(
              rwys.where(q -> isIntersectionBetweenRunways(r, q))
      );
      rwys.tryRemoveMany(set);
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


}
