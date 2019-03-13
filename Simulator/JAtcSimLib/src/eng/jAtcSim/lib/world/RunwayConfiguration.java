package eng.jAtcSim.lib.world;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.xmlSerialization.annotations.*;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.Headings;

import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Objects;

public class RunwayConfiguration {

  public static class RunwayThresholdConfiguration {
    private char[] categoriesArray;
    private String name;
    private ActiveRunwayThreshold threshold;
    private boolean primary = false;
    private boolean showRoutes = true;
    private boolean showApproach = true;

    public boolean isShowRoutes() {
      return showRoutes;
    }

    public boolean isShowApproach() {
      return showApproach;
    }

    public boolean isPrimary() {
      return primary;
    }

    public ActiveRunwayThreshold getThreshold() {
      return threshold;
    }

    public RunwayThresholdConfiguration(String name, String categories, boolean primary, boolean showRoutes, boolean showApproach) {
      this.categoriesArray = categories.toCharArray();
      this.name = name;
      this.primary = primary;
      this.showRoutes = showRoutes;
      this.showApproach = showApproach;
    }

    public RunwayThresholdConfiguration(ActiveRunwayThreshold threshold) {
      this.name = threshold.getName();
      this.threshold = threshold;
    }

    public boolean isForCategory(char category) {
      return ArrayUtils.contains(this.categoriesArray, category);
    }


    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RunwayThresholdConfiguration that = (RunwayThresholdConfiguration) o;
      return primary == that.primary &&
          showRoutes == that.showRoutes &&
          showApproach == that.showApproach &&
          Arrays.equals(categoriesArray, that.categoriesArray) &&
          Objects.equals(name, that.name) &&
          Objects.equals(threshold, that.threshold);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(name, threshold, primary, showRoutes, showApproach);
      result = 31 * result + Arrays.hashCode(categoriesArray);
      return result;
    }
  }

  private final int windFrom;
  private final int windTo;
  private final int windSpeedFrom;
  private final int windSpeedTo;
  private final IList<RunwayThresholdConfiguration> arrivals;
  private final IList<RunwayThresholdConfiguration> departures;
  private final IList<ISet<ActiveRunwayThreshold>> crossedThresholdSets;

  public RunwayConfiguration(int windFrom, int windTo, int windSpeedFrom, int windSpeedTo,
                             IList<RunwayThresholdConfiguration> arrivals, IList<RunwayThresholdConfiguration> departures,
                             IList<ISet<ActiveRunwayThreshold>> crossedThresholdSets) {
    this.windFrom = windFrom;
    this.windTo = windTo;
    this.windSpeedFrom = windSpeedFrom;
    this.windSpeedTo = windSpeedTo;
    this.arrivals = arrivals;
    this.departures = departures;
    this.crossedThresholdSets = crossedThresholdSets;
  }

  public static RunwayConfiguration createForThresholds(IList<ActiveRunwayThreshold> rts) {
    throw new UnsupportedOperationException("This must also includes somehow created crossedThresholdsSet, what is n ow in XmlModelBinder.");
//    IList<RunwayThresholdConfiguration> lst;
//    lst = rts.select(q -> new RunwayThresholdConfiguration(q));
//    RunwayConfiguration ret = new RunwayConfiguration(0, 359, 0, 999, lst, lst);
//    ret.bind();
//    return ret;
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

  public IReadOnlyList<RunwayThresholdConfiguration> getArrivals() {
    return arrivals;
  }

  public IReadOnlyList<RunwayThresholdConfiguration> getDepartures() {
    return departures;
  }

  public boolean accepts(int heading, int speed) {
    boolean ret =
        Headings.isBetween(this.windFrom, heading, this.windTo)
            &&
            NumberUtils.isBetweenOrEqual(this.windSpeedFrom, speed, this.windSpeedTo);
    return ret;
  }

  public String toInfoString(String departureArrivalSeparator) {
    EStringBuilder sb = new EStringBuilder();

    sb.append("Departures - ");
    sb.appendItems(
        departures.select(q -> new Tuple<>(q.name, new String(q.categoriesArray))),
        q -> q.getA() + " for " + q.getB(),
        ", ");
    sb.append(departureArrivalSeparator);
    sb.append("Arrivals - ");
    sb.appendItems(
        arrivals.select(q -> new Tuple<>(q.name, new String(q.categoriesArray))),
        q -> q.getA() + " for " + q.getB(),
        ", ");

    return sb.toString();
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

  public ISet<ActiveRunwayThreshold> getCrossedSetForThreshold(ActiveRunwayThreshold rt) {
    ISet<ActiveRunwayThreshold> ret = this.crossedThresholdSets.getFirst(q -> q.contains(rt));
    return ret;
  }
}
