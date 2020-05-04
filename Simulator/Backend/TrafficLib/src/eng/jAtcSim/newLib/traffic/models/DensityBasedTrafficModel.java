package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.GenericGeneralAviationMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GenericCommercialMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class DensityBasedTrafficModel implements ITrafficModel {
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

  public static class DirectionWeight {
    public static DirectionWeight load(XElement source) {
      int heading = XmlLoaderUtils.loadInteger(source, "heading");
      double weight = XmlLoaderUtils.loadDouble(source, "weight");
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

    public final int arrivals;
    public final int departures;
    public final double generalAviationProbability;

    public HourBlockMovements(int arrivals, int departures, double generalAviationProbability) {
      this.arrivals = arrivals;
      this.departures = departures;
      this.generalAviationProbability = generalAviationProbability;
    }
  }

  public static DensityBasedTrafficModel create(
      HourBlockMovements[] perHourMovements,
      IList<Company> companies,
      IList<Company> countries,
      IList<DirectionWeight> directions
  ) {
    return new DensityBasedTrafficModel(perHourMovements, companies, countries, directions);
  }

  private final IList<Company> companies;
  private final IList<Company> countries;
  private final HourBlockMovements[] perHourMovements;
  private final IList<DirectionWeight> directions;
  private final ERandom rnd = SharedAcc.getRnd();

  private DensityBasedTrafficModel(
      HourBlockMovements[] perHourMovements,
      IList<Company> companies,
      IList<Company> countries,
      IList<DirectionWeight> directions) {
    EAssert.Argument.isNotNull(perHourMovements, "perHourMovements");
    EAssert.Argument.isTrue(perHourMovements.length == 24);
    EAssert.Argument.isTrue(EList.of(perHourMovements).isAll(q -> q != null));
    EAssert.Argument.isNotNull(companies, "companies");
    EAssert.Argument.isNotNull(countries, "countries");
    EAssert.Argument.isNotNull(directions, "directions");
    this.companies = companies;
    this.countries = countries;
    this.perHourMovements = perHourMovements;
    this.directions = directions;
  }

  @Override
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < 24; i++) {
      IReadOnlyList<MovementTemplate> tmp;
      HourBlockMovements hbm = perHourMovements[i];
      tmp = generateMovementsForHour(hbm.departures, hbm.generalAviationProbability, MovementTemplate.eKind.departure, i);
      ret.add(tmp);
      tmp = generateMovementsForHour(hbm.arrivals, hbm.generalAviationProbability, MovementTemplate.eKind.arrival, i);
      ret.add(tmp);
    }

    return ret;
  }

  private IReadOnlyList<MovementTemplate> generateMovementsForHour(
      int count, double gaProb, MovementTemplate.eKind kind, int hour) {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < count; i++) {
      MovementTemplate mt = generateNewMovement(hour, rnd.nextDouble() < gaProb, kind);
      ret.add(mt);
    }

    return ret;
  }

  private MovementTemplate generateNewMovement(int hour, boolean isGA, MovementTemplate.eKind kind) {

    ETimeStamp time = new ETimeStamp(hour, rnd.nextInt(0, 59), rnd.nextInt(0, 59));
    int radial = getRandomEntryRadial();

    MovementTemplate ret;
    if (isGA == false) {
      Company company = this.companies.getRandomByWeights(q -> q.weight, rnd);
      Character category = company.category;
      String companyIcao = company.icao;
      ret = new GenericCommercialMovementTemplate(
          companyIcao, category, kind, time, new EntryExitInfo(radial));
    } else {
      Company country = this.countries.getRandomByWeights(q -> q.weight, rnd);
      String countryPrefix = country.icao;
      ret = new GenericGeneralAviationMovementTemplate(kind, time, countryPrefix, new EntryExitInfo(radial));
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
