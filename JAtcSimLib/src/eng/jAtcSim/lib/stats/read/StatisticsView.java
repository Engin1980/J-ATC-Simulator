package eng.jAtcSim.lib.stats.read;

import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountView;
import eng.jAtcSim.lib.stats.read.specific.*;

public class StatisticsView {
  private ETime fromTime;
  private ETime toTime;
  private SecondStats secondStats;
  private PlanesCountStats planes;
  private MinMaxMeanCountView planesMood;
  private HoldingPointStats holdingPoint;
  private ErrorsStats errors;

  public StatisticsView(ETime fromTime, ETime toTime, SecondStats secondStats, PlanesCountStats planes, MinMaxMeanCountView planesMood, HoldingPointStats holdingPoint, ErrorsStats errors) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.secondStats = secondStats;
    this.planes = planes;
    this.planesMood = planesMood;
    this.holdingPoint = holdingPoint;
    this.errors = errors;
  }

  public ETime getFromTime() {
    return fromTime;
  }

  public ETime getToTime() {
    return toTime;
  }

  public SecondStats getSecondStats() {
    return secondStats;
  }

  public PlanesCountStats getPlanes() {
    return planes;
  }

  public MinMaxMeanCountView getPlanesMood() {
    return planesMood;
  }

  public HoldingPointStats getHoldingPoint() {
    return holdingPoint;
  }

  public ErrorsStats getErrors() {
    return errors;
  }
}
