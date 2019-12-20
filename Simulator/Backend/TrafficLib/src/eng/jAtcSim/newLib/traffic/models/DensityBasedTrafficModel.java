package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.Selector;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.SharedFactory;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.traffic.models.base.DayGeneratedTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public class DensityBasedTrafficModel extends DayGeneratedTrafficModel {
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
      double rnd = SharedFactory.getRnd().nextDouble(0, weightSum);
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

  public static DensityBasedTrafficModel load(XElement source) {
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

    DensityBasedTrafficModel ret = new DensityBasedTrafficModel(
        delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns, companies, countries,
        density, directions, nonCommercialFlightProbability
    );
    return ret;
  }

  private CodeWeightList companies;
  private CodeWeightList countries;
  private IList<HourBlockMovements> density;
  private IList<DirectionWeight> directions;
  private double nonCommercialFlightProbability;
  private ERandom rnd = SharedFactory.getRnd();

  private DensityBasedTrafficModel(double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns,
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
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < 24; i++) {
      int hour = i;
      IReadOnlyList<MovementTemplate> tmp;
      HourBlockMovements hbm = density.getFirst(q -> q.hour == hour);
      tmp = generateMovementsForHour(hbm.departures, MovementTemplate.eKind.departure, hour);
      ret.add(tmp);
      tmp = generateMovementsForHour(hbm.arrivals, MovementTemplate.eKind.arrival, hour);
      ret.add(tmp);
    }

    return ret;
  }

  private IReadOnlyList<MovementTemplate> generateMovementsForHour(int count, MovementTemplate.eKind kind, int hour) {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < count; i++) {
      MovementTemplate mt = generateNewMovement(hour, kind);
      ret.add(mt);
    }

    return ret;
  }

  private MovementTemplate generateNewMovement(int hour, MovementTemplate.eKind kind) {
    String prefix;
    boolean isCommercial = !(rnd.nextDouble() < nonCommercialFlightProbability);
    if (isCommercial == false)
      prefix = this.countries.getRandomCode().code;
    else
      prefix = this.companies.getRandomCode().code;

    Callsign cls = super.generateRandomCallsign(prefix, isCommercial);
    ETimeStamp time = new ETimeStamp(hour, rnd.nextInt(0, 59), rnd.nextInt(0, 59));
    int delay = super.generateDelayMinutes();

    String typeName;
    if (isCommercial == false)
      //TODO here should be some like category probability
      ???
    else {
      ???
    }

    int radial = getRandomEntryRadial();

    FlightMovementTemplate ret = new FlightMovementTemplate(
        cls, typeName, kind, time, delay, new EntryExitInfo(radial));
    return ret;
  }

  private int getRandomEntryRadial() {
    int ret;
    if (directions.isEmpty())
      ret = rnd.nextInt(360);
    else {
      DirectionWeight dw;
      dw = directions.getRandomByWeights(q -> q.weight, rnd);
      ret = dw.heading;
    }
    return ret;
  }
}
