package eng.jAtcSim.newLib.area.traffic;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.Selector;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Callsign;
import eng.jAtcSim.newLib.area.airplanes.AirplaneType;
import eng.jAtcSim.newLib.area.airplanes.Callsign;
import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.newLib.world.xml.XmlLoader;

public class DensityBasedTraffic extends GeneratedTraffic {

  public static class CodeWeight {
    public final String code;
    public final double weight;

    public CodeWeight(String code, double weight) {
      this.code = code;
      this.weight = weight;
    }

    @Override
    public String toString() {
      return String.format("%s -> %.3f", code, weight);
    }
  }

  public static class CodeWeightList {
    public static CodeWeightList load(IReadOnlyList<XElement> sources) {
      return load(sources, "code", "weight");
    }

    public static CodeWeightList load(IReadOnlyList<XElement> sources, String codeXmlName, String weightXmlName) {
      IList<CodeWeight> tmp = new EList<>();

      for (XElement source : sources) {
        String code = XmlLoader.loadString(source, codeXmlName);
        double weight = XmlLoader.loadDouble(source, weightXmlName);
        CodeWeight cw = new CodeWeight(code, weight);
        tmp.add(cw);
      }

      CodeWeightList ret = new CodeWeightList(tmp);
      return ret;
    }

    private final IList<CodeWeight> inner;
    private final double weightSum;

    public CodeWeightList(IList<CodeWeight> items) {
      this.inner = items;
      this.weightSum = inner.sumDouble(q -> q.weight);
    }

    public CodeWeight getRandomCode() {
      double rnd = Acc.rnd().nextDouble(0, weightSum);
      int index = 0;
      CodeWeight ret = null;
      while (rnd > 0) {
        CodeWeight cw = this.inner.get(index);
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

    public <T> IList<T> select(Selector<CodeWeight, T> selector) {
      return inner.select(selector);
    }
  }

  public static class DirectionWeight {
    public final int heading;
    public final double weight;

    public DirectionWeight(int heading, double weight) {
      this.heading = heading;
      this.weight = weight;
    }

    @Override
    public String toString() {
      return String.format("%03d -> %.3f", heading, weight);
    }
  }

  public static class HourBlockMovements {
    public static IList<HourBlockMovements> loadList(IReadOnlyList<XElement> children) {
      IList<HourBlockMovements> ret = new EList<>();

      for (XElement child : children) {
        int hour = XmlLoader.loadInteger(child, "hour");
        int arrs = XmlLoader.loadInteger(child, "arrivals");
        int deps = XmlLoader.loadInteger(child, "departures");

        HourBlockMovements hbm = new HourBlockMovements(hour, arrs, deps);
        ret.add(hbm);
      }

      return ret;
    }

    public final int hour;
    public final int arrivals;
    public final int departures;

    public HourBlockMovements(int hour, int arrivals, int departures) {
      this.hour = hour;
      this.arrivals = arrivals;
      this.departures = departures;
    }

    @Override
    public String toString() {
      return String.format("%s:00 -> %d / %d", hour, departures, arrivals);
    }
  }

  public static DensityBasedTraffic load(XElement source) {
    XmlLoader.setContext(source);
    double delayProbability = XmlLoader.loadDouble("delayProbability");
    int maxDelayInMinutesPerStep = XmlLoader.loadInteger("maxDelayInMinutesPerStep");
    double nonCommercialFlightProbability = XmlLoader.loadDouble("nonCommercialFlightProbability");
    boolean useExtendedCallsigns = XmlLoader.loadBoolean("useExtendedCallsigns");

    //TODO isFullDayTraffic not implemented
    boolean isFullDayTraffic = XmlLoader.loadBoolean(source.getChild("companies"), "isFullDayTraffic");
    CodeWeightList companies = CodeWeightList.load(source.getChild("companies").getChildren("company"));
    CodeWeightList countries = CodeWeightList.load(source.getChild("countries").getChildren("country"));
    IList<HourBlockMovements> density = HourBlockMovements.loadList(source.getChild("density").getChildren("item"));
    CodeWeightList directionsCW = CodeWeightList.load(source.getChild("directions").getChildren("direction"),
        "heading", "weight");
    IList<DirectionWeight> directions = directionsCW.select(q -> new DirectionWeight(
        Integer.parseInt(q.code), q.weight));

    DensityBasedTraffic ret = new DensityBasedTraffic(
        delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns, companies, countries,
        density, directions, nonCommercialFlightProbability
    );
    return ret;
  }

  private final CodeWeightList companies;
  private final CodeWeightList countries;
  private final IList<HourBlockMovements> density;
  private final IList<DirectionWeight> directions;
  private final double nonCommercialFlightProbability;
  private boolean initialized = false;

  private DensityBasedTraffic(double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns,
                              CodeWeightList companies, CodeWeightList countries,
                              IList<HourBlockMovements> density, IList<DirectionWeight> directions,
                              double nonCommercialFlightProbability) {
    super(delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);
    this.companies = companies;
    this.countries = countries;
    this.density = density;
    this.directions = directions;
    this.nonCommercialFlightProbability = nonCommercialFlightProbability;
  }

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
