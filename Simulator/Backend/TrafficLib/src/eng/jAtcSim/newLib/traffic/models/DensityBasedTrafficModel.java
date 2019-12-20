package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.Selector;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.SharedFactory;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.traffic.models.base.DayGeneratedTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.GeneralAviationMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GeneralCommercialMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class DensityBasedTrafficModel extends DayGeneratedTrafficModel {
  public static class Company {
    public final String icao;
    public final double weight;
    public final Character category;

    public Company(String icao, double weight) {
      this(icao, null, weight);
    }

    public Company(String icao, Character category, double weight) {
      EAssert.isNonemptyString(icao);
      EAssert.isTrue(weight >= 0);
      this.icao = icao;
      this.weight = weight;
      this.category = category;
    }

    @Override
    public String toString() {
      return sf("%s -> %.3f", icao, weight);
    }
  }

  public static class CompanyList {
    public static CompanyList load(IReadOnlyList<XElement> sources) {
      IList<Company> tmp = new EList<>();

      for (XElement source : sources) {
        String code = XmlLoader.loadString(source, "icao");
        double weight = XmlLoader.loadDouble(source, "weight");
        Character category = XmlLoader.loadChar(source, "category", null);
        Company cw = new Company(code, category, weight);
        tmp.add(cw);
      }

      CompanyList ret = new CompanyList(tmp);
      return ret;
    }

    private final IList<Company> inner;
    private final double weightSum;

    public CompanyList(IList<Company> items) {
      this.inner = items;
      this.weightSum = inner.sumDouble(q -> q.weight);
    }

    public Company getRandom() {
      double rnd = SharedFactory.getRnd().nextDouble(0, weightSum);
      int index = 0;
      Company ret = null;
      while (rnd > 0) {
        Company cw = this.inner.get(index);
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

    public <T> IList<T> select(Selector<Company, T> selector) {
      return inner.select(selector);
    }
  }

  public static class DirectionWeight {
    public static DirectionWeight load(XElement source) {
      int heading = XmlLoader.loadInteger(source, "heading");
      double weight = XmlLoader.loadDouble(source, "weight");
      return new DirectionWeight(heading, weight);
    }

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
    CompanyList companies = CompanyList.load(source.getChild("companies").getChildren("company"));
    IList<HourBlockMovements> density = HourBlockMovements.loadList(source.getChild("density").getChildren("item"));

    IList<DirectionWeight> directions = new EList<>();
    XmlLoader.loadList(source.getChild("directions").getChildren("direction"),
        directions,
        q -> DirectionWeight.load(q));

    DensityBasedTrafficModel ret = new DensityBasedTrafficModel(
        delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns, companies,
        density, directions, nonCommercialFlightProbability
    );
    return ret;
  }

  private final CompanyList companies;
  private final IList<HourBlockMovements> density;
  private final IList<DirectionWeight> directions;
  private final double nonCommercialFlightProbability;
  private final ERandom rnd = SharedFactory.getRnd();

  private DensityBasedTrafficModel(double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns,
                                   CompanyList companies,
                                   IList<HourBlockMovements> density, IList<DirectionWeight> directions,
                                   double nonCommercialFlightProbability) {
    super(delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);
    this.companies = companies;
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
    String icao;
    boolean isCommercial = !(rnd.nextDouble() < nonCommercialFlightProbability);

    Company company = this.companies.getRandom();
    Character category = company.category;
    String companyIcao = company.icao;
    ETimeStamp time = new ETimeStamp(hour, rnd.nextInt(0, 59), rnd.nextInt(0, 59));
    int delay = super.generateDelayMinutes();
    int radial = getRandomEntryRadial();

    MovementTemplate ret;
    if (isCommercial == false)
      //TODO here should be some like category probability
      ret = new GeneralCommercialMovementTemplate(
          companyIcao, category, kind, time, delay, new EntryExitInfo(radial));
    else {
      ret = new GeneralAviationMovementTemplate(kind, time, delay, new EntryExitInfo(radial));
    }
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
