package eng.jAtcSim.lib.traffic;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.CollectionUtils;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DensityBasedTraffic extends GeneratedTraffic {

  public static class CodeWeight {
    public String code;
    public double weight;

    @Override
    public String toString() {
      return String.format("%s -> %.3f", code, weight);
    }
  }

  public static class CodeWeightList extends EList<CodeWeight> {
    @XmlIgnore
    private double weightSum = -1;

    public CodeWeight getRandomCode() {
      if (weightSum < 0)
        weightSum = CollectionUtils.sum(this, o -> o.weight);
      double rnd = Acc.rnd().nextDouble(0, weightSum);
      int index = 0;
      CodeWeight ret = null;
      while (rnd > 0) {
        CodeWeight cw = this.get(index);
        if (rnd < cw.weight) {
          ret = cw;
          break;
        } else {
          rnd -= cw.weight;
          index++;
        }
      }
      assert ret != null;

      return ret;
    }
  }

  public static class DirectionWeight {
    public int heading;
    public double weight;

    @Override
    public String toString() {
      return String.format("%03d -> %.3f", heading, weight);
    }
  }

  public static class HourBlockMovements {
    public int hour;
    public int arrivals;
    public int departures;

    @Override
    public String toString() {
      return String.format("%s:00 -> %d / %d", hour, departures, arrivals);
    }
  }

  @XmlItemElement(elementName = "company", type = CodeWeight.class)
  private CodeWeightList companies = null; // XML
  @XmlItemElement(elementName = "country", type = CodeWeight.class)
  private CodeWeightList countries = null; // XML
  @XmlItemElement(elementName = "item", type = HourBlockMovements.class)
  private IList<HourBlockMovements> density = null; //XMl
  @XmlItemElement(elementName = "direction", type = DirectionWeight.class)
  private IList<DirectionWeight> directions = new EList<>(); // XML
  private double nonCommercialFlightProbability = 0; // XML

  @XmlIgnore
  private boolean initialized = false;

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    Tuple<Integer, IReadOnlyList<Movement>> tmp = null;
    GeneratedMovementsResponse ret;

    Integer lastGeneratedHour = (Integer) syncObject;
    if (!initialized || lastGeneratedHour == Acc.now().getHours()) {
      tmp = generateNewMovements(lastGeneratedHour);

      ret = new GeneratedMovementsResponse(
          Acc.now().getRoundedToNextHour(),
          tmp.getA(),
          tmp.getB());
    } else
      ret = new GeneratedMovementsResponse(
          Acc.now().getRoundedToNextHour(),
          syncObject,
          new EList<>());

    return ret;
  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    IList<ExpectedMovement> ret = new EList<>();

    for (int i = 0; i < 24; i++) {
      int l = i;
      IList<HourBlockMovements> hbms = density.where(q -> q.hour == l);
      for (int j = 0; j < hbms.size(); j++) {
        HourBlockMovements hbm = hbms.get(j);
        boolean isCommercial = Acc.rnd().nextDouble() > nonCommercialFlightProbability;
        char category = '-';
        for (int k = 0; k < hbm.arrivals; k++) {
          ETime time = new ETime(i, Acc.rnd().nextInt(0, 60), 30);
          ExpectedMovement em = new ExpectedMovement(time, true, isCommercial, category);
          ret.add(em);
        }
        for (int k = 0; k < hbm.departures; k++) {
          ETime time = new ETime(i, Acc.rnd().nextInt(0, 60), 30);
          ExpectedMovement em = new ExpectedMovement(time, false, isCommercial, category);
          ret.add(em);
        }
      }
    }

    return ret;
  }

  private Tuple<Integer, IReadOnlyList<Movement>> generateNewMovements(Integer lastGeneratedHour) {
    IList<Movement> ret = new EList<>();

    if (!initialized) {
      if (density.size() == 0)
        throw new EApplicationException("Unable to use generic traffic without density specified.");
      // init things
      density.sort(q -> q.hour);
      IReadOnlyList<Movement> tmp = generateTrafficForHour(Acc.now().getHours());
      ret.add(tmp);
      initialized = true;
      lastGeneratedHour = Acc.now().getHours();
    }
    lastGeneratedHour++;
    if (lastGeneratedHour > 23) lastGeneratedHour = 0;
    ret.add(
        generateTrafficForHour(lastGeneratedHour));

    return new Tuple<>(lastGeneratedHour, ret);
  }

  private IReadOnlyList<Movement> generateTrafficForHour(int hour) {
    IList<Movement> ret = new EList<>();

    HourBlockMovements hbm = density.get(0);
    int index = 0;
    while (index < density.size()) {
      if (density.get(index).hour <= hour)
        hbm = density.get(index);
      else
        break;
      index++;
    }
    assert hbm != null;
    Movement m;

    for (int i = 0; i < hbm.arrivals; i++) {
      m = generateNewScheduledMovement(hour, false);
      ret.add(m);
    }
    for (int i = 0; i < hbm.departures; i++) {
      m = generateNewScheduledMovement(hour, true);
      ret.add(m);
    }

    return ret;
  }

  private Movement generateNewScheduledMovement(int hour, boolean isDeparture) {
    String prefix;
    boolean isNonCommercial = Acc.rnd().nextDouble() < nonCommercialFlightProbability;
    if (isNonCommercial)
      prefix = this.countries.getRandomCode().code;
    else
      prefix = this.companies.getRandomCode().code;

    Callsign cls = super.generateUnusedCallsign(prefix, isNonCommercial);
    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 59), Acc.rnd().nextInt(0, 59));
    int delay = super.generateDelayMinutes();

    AirplaneType type;
    if (isNonCommercial)
      //TODO here should be some like category probability
      type = Acc.types().getRandom();
    else {
      CompanyFleet cf = Acc.fleets().tryGetByIcao(prefix);
      if (cf == null) cf = Acc.fleets().getDefaultCompanyFleet();
      type = cf.getRandom().getAirplaneType();
    }

    int entryRadial = getRandomEntryRadial();

    Movement m = new Movement(cls, type, initTime, delay, isDeparture, entryRadial);
    return m;
  }

  private int getRandomEntryRadial() {
    int ret;
    if (directions.isEmpty())
      ret = Acc.rnd().nextInt(360);
    else {
      DirectionWeight dw;
      dw = directions.getRandomByWeights(q -> q.weight, Acc.rnd());
      ret = dw.heading;
    }
    return ret;
  }
}
