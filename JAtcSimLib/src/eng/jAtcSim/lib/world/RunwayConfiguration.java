package eng.jAtcSim.lib.world;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.Headings;

import java.awt.geom.Line2D;

public class RunwayConfiguration {

  public static class RunwayThresholdConfiguration {
    @XmlOptional
    private String categories = "ABCD";
    @XmlIgnore
    private char[] categoriesArray;
    private String name;
    @XmlIgnore
    private RunwayThreshold threshold;
    @XmlOptional
    private boolean primary = false;
    @XmlOptional
    private boolean showRoutes = true;
    @XmlOptional
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

    public String getCategories() {
      return categories;
    }

    public RunwayThreshold getThreshold() {
      return threshold;
    }

    public RunwayThresholdConfiguration(String name, String categories, boolean primary, boolean showRoutes, boolean showApproach) {
      this.categories = categories;
      this.name = name;
      this.primary = primary;
      this.showRoutes = showRoutes;
      this.showApproach = showApproach;
    }

    public RunwayThresholdConfiguration(RunwayThreshold threshold) {
      this.name = threshold.getName();
      this.threshold = threshold;
    }

    public void bind() {
      this.threshold = Acc.airport().tryGetRunwayThreshold(this.name);
      if (this.threshold == null)
        throw new EApplicationException("Unable to find threshold " + this.name + " for runway configuration.");
      categoriesArray = categories.toCharArray();
    }

    public boolean isForCategory(char category) {
      return ArrayUtils.contains(this.categoriesArray, category);
    }
  }

  private int windFrom;
  private int windTo;
  private int windSpeedFrom;
  private int windSpeedTo;
  @XmlItemElement(elementName = "arrival", type=RunwayThresholdConfiguration.class)
  private IList<RunwayThresholdConfiguration> arrivals;
  @XmlItemElement(elementName = "departure", type=RunwayThresholdConfiguration.class)
  private IList<RunwayThresholdConfiguration> departures;
  @XmlIgnore
  private IList<ISet<RunwayThreshold>> crossedThresholdSets = null;

  @XmlConstructor
  private RunwayConfiguration() {

  }

  public RunwayConfiguration(int windFrom, int windTo, int windSpeedFrom, int windSpeedTo,
                             IList<RunwayThresholdConfiguration> arrivals, IList<RunwayThresholdConfiguration> departures) {
    this.windFrom = windFrom;
    this.windTo = windTo;
    this.windSpeedFrom = windSpeedFrom;
    this.windSpeedTo = windSpeedTo;
    this.arrivals = arrivals;
    this.departures = departures;
  }

  public static RunwayConfiguration createForThresholds(IList<RunwayThreshold> rts) {
    IList<RunwayThresholdConfiguration> lst;
    lst = rts.select(q -> new RunwayThresholdConfiguration(q));
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

  public IReadOnlyList<RunwayThresholdConfiguration> getArrivals() {
    return arrivals;
  }

  public IReadOnlyList<RunwayThresholdConfiguration> getDepartures() {
    return departures;
  }

  public void bind() {
    arrivals.forEach(q -> q.bind());
    departures.forEach(q -> q.bind());

    IList<Runway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(this.arrivals.select(q -> q.threshold.getParent()).distinct());
    rwys.add(this.departures.select(q -> q.threshold.getParent()).distinct());

    // check if all categories are applied
    for (char i = 'A'; i <= 'D'; i++) {
      char c = i;
      if (!arrivals.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find arrival threshold for category " + c);
      if (!departures.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find departure threshold for category " + c);
    }

    // detection and saving the crossed runways
    IList<ISet<Runway>> crossedRwys = new EList<>();
    while (rwys.isEmpty() == false) {
      ISet<Runway> set = new ESet<>();
      Runway r = rwys.get(0);
      rwys.removeAt(0);
      set.add(r);
      set.add(
          rwys.where(q -> isIntersectionBetweenRunways(r, q))
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
        (float) a.getThresholdB().getCoordinate().getLongitude().get());
    Line2D lineB = new Line2D.Float(
        (float) b.getThresholdA().getCoordinate().getLatitude().get(),
        (float) b.getThresholdA().getCoordinate().getLongitude().get(),
        (float) b.getThresholdB().getCoordinate().getLatitude().get(),
        (float) b.getThresholdB().getCoordinate().getLongitude().get());
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

  public IList<String> toInfoString() {
    EList<String> lines = new EList<>();
    EStringBuilder sb;

    sb = new EStringBuilder();
    sb.append("Arrivals: ");
    sb.appendItems(
        arrivals.select(q -> new Tuple<>(q.name, q.categories)),
        q -> q.getA() + "/" + q.getB(),
        ", ");
    lines.add(sb.toString());

    sb = new EStringBuilder();
    sb.append("Departures: ");
    sb.appendItems(
        departures.select(q -> new Tuple<>(q.name, q.categories)),
        q -> q.getA() + "/" + q.getB(),
        ", ");
    lines.add(sb.toString());

    return lines;
  }

  public boolean isUsingTheSameRunwayConfiguration(RunwayConfiguration other) {
    boolean ret;
    if (this.arrivals.size() != other.arrivals.size())
      ret = false;
    else if (this.departures.size() != other.departures.size())
      ret = false;
    else if (this.arrivals.union(other.arrivals).size() != this.arrivals.size())
      ret = false;
    else if (this.arrivals.union(other.arrivals).size() != this.arrivals.size())
      ret = false;
    else
      ret = true;

    return ret;
  }

  public ISet<RunwayThreshold> getCrossedSetForThreshold(RunwayThreshold rt) {
    ISet<RunwayThreshold> ret = this.crossedThresholdSets.getFirst(q -> q.contains(rt));
    return ret;
  }
}
