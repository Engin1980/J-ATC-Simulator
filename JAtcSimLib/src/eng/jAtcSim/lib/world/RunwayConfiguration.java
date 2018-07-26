package eng.jAtcSim.lib.world;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.Headings;

import java.awt.geom.Line2D;

public class RunwayConfiguration {
  private int windFrom;
  private int windTo;
  private int windSpeedFrom;
  private int windSpeedTo;
  private IList<String> arrivals;
  private IList<String> departures;
  private IList<RunwayThreshold> arrivingThresholds;
  private IList<RunwayThreshold> departingThresholds;
  private IList<ISet<RunwayThreshold>> crossedThresholdSets = null;

  public RunwayConfiguration(int windFrom, int windTo, int windSpeedFrom, int windSpeedTo, IList<String> arrivals, IList<String> departures) {
    this.windFrom = windFrom;
    this.windTo = windTo;
    this.windSpeedFrom = windSpeedFrom;
    this.windSpeedTo = windSpeedTo;
    this.arrivals = arrivals;
    this.departures = departures;
  }

  public static RunwayConfiguration createForThresholds(IList<RunwayThreshold> rts) {
    IList<String> lst;
    lst = rts.select(q -> q.getName());
    RunwayConfiguration ret = new RunwayConfiguration(0, 359, 0, 999, lst, lst);
    ret.bind();
    return ret;
  }

  public int getWindFrom() {
    return windFrom;
  }

  public int getWindTo() {
    return windTo;
  }

  public int getWindSpeedFrom() {
    return windSpeedFrom;
  }

  public int getWindSpeedTo() {
    return windSpeedTo;
  }

  public IReadOnlyList<String> getArrivals() {
    return arrivals;
  }

  public IReadOnlyList<String> getDepartures() {
    return departures;
  }

  public IReadOnlyList<RunwayThreshold> getArrivingThresholds() {
    return arrivingThresholds;
  }

  public IReadOnlyList<RunwayThreshold> getDepartingThresholds() {
    return departingThresholds;
  }

  public void bind() {
    this.arrivingThresholds = new EList<>();
    for (String s : arrivals) {
      RunwayThreshold thr = Acc.airport().tryGetRunwayThreshold(s);
      if (thr == null)
        throw new EApplicationException("Unable to find threshold " + s + " for runway configuration.");
      arrivingThresholds.add(thr);
    }
    this.departingThresholds = new EList<>();
    for (String s : departures) {
      RunwayThreshold thr = Acc.airport().tryGetRunwayThreshold(s);
      if (thr == null)
        throw new EApplicationException("Unable to find threshold " + s + " for runway configuration.");
      departingThresholds.add(thr);
    }

    IList<Runway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(this.arrivingThresholds.select(q->q.getParent()).distinct());
    rwys.add(this.departingThresholds.select(q->q.getParent()).distinct());

    IList<ISet<Runway>> crossedRwys = new EList<>();
    while (rwys.isEmpty() == false){
      ISet<Runway> set = new ESet<>();
      Runway r = rwys.get(0);
      rwys.removeAt(0);
      set.add(r);
      set.add(
        rwys.where(q->isIntersectionBetweenRunways(r, q))
      );
      rwys.tryRemove(set);
      crossedRwys.add(set);
    }

    ISet<RunwayThreshold> set;
    this.crossedThresholdSets = new EList<>();
    for (ISet<Runway> crossedRwy : crossedRwys) {
      set = new ESet<>();
      for (Runway runway : crossedRwy) {
        set.add(runway.getThresholds());
      }
      this.crossedThresholdSets.add(set);
    }
  }

  private boolean isIntersectionBetweenRunways(Runway a, Runway b) {
    Line2D lineA = new Line2D.Float(
        (float) a.getThresholdA().getCoordinate().getLatitude().get(),
        (float) a.getThresholdA().getCoordinate().getLongitude().get(),
        (float) a.getThresholdB().getCoordinate().getLatitude().get(),
        (float) a.getThresholdA().getCoordinate().getLongitude().get());
    Line2D lineB = new Line2D.Float(
        (float) b.getThresholdA().getCoordinate().getLatitude().get(),
        (float) b.getThresholdA().getCoordinate().getLongitude().get(),
        (float) b.getThresholdB().getCoordinate().getLatitude().get(),
        (float) b.getThresholdA().getCoordinate().getLongitude().get());
    boolean ret = lineA.intersectsLine(lineB);
    return ret;
  }

  public boolean accepts(int heading, int speed) {
    boolean ret =
        Headings.isBetween(this.windFrom, heading, this.windTo)
            &&
            NumberUtils.isBetweenOrEqual(this.windSpeedFrom, speed, this.windSpeedTo);
    return ret;
  }

  public String toLineInfoString() {
    EStringBuilder sb = new EStringBuilder();
    IList<String> deps = getDepartingThresholds().select(q -> q.getName() + "(DEP)");
    IList<String> arrs = getDepartingThresholds().select(q -> q.getName() + "(ARR)");
    sb.appendItems(deps, ", ");
    sb.appendItems(arrs, ", ");
    return sb.toString();
  }

  public boolean isUsingTheSameRunwayConfiguration(RunwayConfiguration other) {
    boolean ret;
    if (this.arrivingThresholds.size() != other.arrivingThresholds.size())
      ret = false;
    else if (this.departingThresholds.size() != other.departingThresholds.size())
      ret = false;
    else if (this.arrivingThresholds.union(other.arrivingThresholds).size() != this.arrivingThresholds.size())
      ret = false;
    else if (this.arrivingThresholds.union(other.arrivingThresholds).size() != this.arrivingThresholds.size())
      ret = false;
    else
      ret = true;

    return ret;
  }

  public ISet<RunwayThreshold> getCrossedSetForThreshold(RunwayThreshold rt) {
    ISet<RunwayThreshold> ret = this.crossedThresholdSets.getFirst(q->q.contains(rt));
    return ret;
  }
}
