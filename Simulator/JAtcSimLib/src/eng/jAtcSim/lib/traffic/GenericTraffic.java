package eng.jAtcSim.lib.traffic;

import com.sun.javafx.iio.common.ImageLoaderImpl;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.NumberUtils;

import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import sun.security.krb5.internal.ETypeInfo;

/**
 * @author Marek Vajgl
 */
public class GenericTraffic extends GeneratedTraffic {

  private final String[] companies;
  private final String[] countryCodes;

  private final double probabilityOfNonCommercialFlight;

  /**
   * Probability that generated plane is departure (arrival otherwise), range 0.0-1.0 .
   */
  private final double probabilityOfDeparture;

  /**
   * Specifies number of movements for each hour. This is 24 item array, where for each hour number movements is
   * defined.
   */
  private final int[] movementsPerHour = new int[24];

  /**
   * Specifies probability thresholds of each category. By indices 0, 1, 2, 3 for categories A, B, C, D.
   */
  private final double[] probabilityOfCategory = new double[4];

  @XmlIgnore
  private char[] orderedCategoriesByProbabilityDesc = null;

  @XmlConstructor
  private GenericTraffic() {
    this.probabilityOfDeparture = 0.5;
    this.probabilityOfNonCommercialFlight = 0;
    this.companies = new String[0];
    this.countryCodes = new String[0];
  }

  public GenericTraffic(String companies, String countryCodes, int[] movementsPerHour, double probabilityOfDeparture, double probabilityOfNonCommercialFlight,
                        int trafficCustomWeightTypeA, int trafficCustomWeightTypeB, int trafficCustomWeightTypeC, int trafficCustomWeightTypeD,
                        boolean useExtendedCallsigns) {

    if (movementsPerHour == null) {
      throw new IllegalArgumentException("Value of {movementsPerHour} cannot not be null.");
    }
    if (movementsPerHour.length != 24) {
      throw new IllegalArgumentException("Argument \"movementsPerHour\" must have length = 24.");
    }

    for (int i = 0; i < movementsPerHour.length; i++) {
      if (movementsPerHour[i] < 0)
        throw new IllegalArgumentException("Argument \"movementsPerHour\" must have all elements equal or greater than 0.");
    }

    if (NumberUtils.isBetweenOrEqual(0, probabilityOfDeparture, 1) == false) {
      throw new IllegalArgumentException("\"probabilityOfDeparture\" must be between 0 and 1.");
    }

    this.companies = companies.split(";");
    this.countryCodes = countryCodes.split(";");

    for (int i = 0; i < this.movementsPerHour.length; i++) {
      this.movementsPerHour[i] = movementsPerHour[i];
    }

    this.probabilityOfDeparture = probabilityOfDeparture;
    this.probabilityOfNonCommercialFlight = probabilityOfNonCommercialFlight;

    // category probabilities init
    {
      double sum = trafficCustomWeightTypeA + trafficCustomWeightTypeB + trafficCustomWeightTypeC + trafficCustomWeightTypeD;
      probabilityOfCategory[0] = trafficCustomWeightTypeA / sum;
      probabilityOfCategory[1] = trafficCustomWeightTypeB / sum;
      probabilityOfCategory[2] = trafficCustomWeightTypeC / sum;
      probabilityOfCategory[3] = trafficCustomWeightTypeD / sum;
    }

    super.setUseExtendedCallsigns(useExtendedCallsigns);
  }

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    IList<Movement> lst = new EList<>();

    int currentHour = Acc.now().getHours();
    int expMovs = movementsPerHour[currentHour];
    for (int i = 0; i < expMovs; i++) {
      Movement m = generateMovement(currentHour);
      lst.add(m);
    }

    // what will this to over midnight?
    ETime nextGenTime = Acc.now().getRoundedToNextHour();

    GeneratedMovementsResponse ret = new GeneratedMovementsResponse(
        nextGenTime, null, lst);
    return ret;
  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    IList<ExpectedMovement> ret = new EList<>();
    for (int i = 0; i < 24; i++) {
      for (int j = 0; j < movementsPerHour[i]; j++) {
        ETime time = new ETime(i, Acc.rnd().nextInt(0, 60), Acc.rnd().nextInt(0, 60));
        boolean isArrival = this.probabilityOfDeparture < Acc.rnd().nextDouble();
        boolean isCommercial = this.probabilityOfNonCommercialFlight > Acc.rnd().nextDouble();
        char category = getRandomCategory();
        ExpectedMovement em = new ExpectedMovement(time, isArrival, isCommercial, category);
        ret.add(em);
      }
    }
    return ret;
  }

  private char getRandomCategory() {
    char ret = 'A';
    double sum = 0;
    for (double v : probabilityOfCategory) {
      sum += v;
    }
    double tmp = Acc.rnd().nextDouble(sum);
    int index = -1;
    while (index < probabilityOfCategory.length) {
      index++;
      if (tmp < probabilityOfCategory[index]) {
        ret = (char) ((int) ret + index);
        break;
      } else {
        tmp -= probabilityOfCategory[index];
      }
    }
    return ret;
  }

  private Movement generateMovement(int hour) {

    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 60), Acc.rnd().nextInt(0, 60));
    boolean isDeparture = (Acc.rnd().nextDouble() <= this.probabilityOfDeparture);
    boolean isNonCommercial = Acc.rnd().nextDouble() < this.probabilityOfNonCommercialFlight;
    char category = getRandomCategory();

    String prefix;
    AirplaneType type;
    if (isNonCommercial) {
      prefix = this.countryCodes[Acc.rnd().nextInt(this.countryCodes.length)];
      type = Acc.sim().getAirplaneTypes().getRandomFromCategory(category);
    } else {
      Tuple<String, AirplaneType> tmp = getCompanyPrefixAndAirplaneType(category);
      prefix = tmp.getA();
      type = tmp.getB();
    }

    Callsign cls = generateUnusedCallsign(prefix, isNonCommercial);
    int delayInMinutes = generateDelayMinutes();
    int entryRadial = Acc.rnd().nextInt(360);
    Movement ret = new Movement(cls, type, initTime, delayInMinutes, isDeparture, entryRadial);
    return ret;
  }

  private Tuple<String, AirplaneType> getCompanyPrefixAndAirplaneType(char category) {
    CompanyFleet companyFleet = null;
    String icao = null;
    AirplaneType type = null;

    IList<CompanyFleet> flts = Acc.fleets().where(
        q ->
            ArrayUtils.contains(this.companies, q.icao));

    // this will try restrict to required category
    IList<CompanyFleet> tmp = flts.where(q -> q.getTypes().isAny(p -> p.getAirplaneType().category == category));
    if (tmp.isEmpty()) {
      if (this.orderedCategoriesByProbabilityDesc == null) fillOrderedCategories();
      for (char c : this.orderedCategoriesByProbabilityDesc) {
        tmp = flts.where(q -> q.getTypes().isAny(p -> p.getAirplaneType().category == c));
        if (!tmp.isEmpty()) {
          companyFleet = tmp.getRandom();
          icao = companyFleet.icao;
          type = companyFleet.getTypes().where(q -> q.getAirplaneType().category == c).getRandom().getAirplaneType();
          break;
        }
      }
      if (companyFleet == null) {
        companyFleet = flts.getRandom();
        icao = companyFleet.icao;
        type = companyFleet.getTypes().getRandom().getAirplaneType();
      }
      if (companyFleet == null)
        throw new EApplicationException("There is no plane type matching requested category and company.");
    } else {
      companyFleet = tmp.getRandom();
      icao = companyFleet.icao;
      type = companyFleet.getTypes().where(q -> q.getAirplaneType().category == category).getRandom().getAirplaneType();
    }

    //those should be set
    assert icao != null;
    assert type != null;

    Tuple<String, AirplaneType> ret = new Tuple<>(icao, type);
    return ret;
  }

  private void fillOrderedCategories() {
    IList<Tuple<Character, Double>> tmp = new EList<>();
    tmp.add(new Tuple('A', probabilityOfCategory[0]));
    tmp.add(new Tuple('B', probabilityOfCategory[1]));
    tmp.add(new Tuple('C', probabilityOfCategory[2]));
    tmp.add(new Tuple('D', probabilityOfCategory[3]));
    tmp.sort(q -> -q.getB());
    this.orderedCategoriesByProbabilityDesc = new char[4];
    for (int i = 0; i < tmp.size(); i++) {
      this.orderedCategoriesByProbabilityDesc[i] = tmp.get(i).getA();
    }
  }
}

