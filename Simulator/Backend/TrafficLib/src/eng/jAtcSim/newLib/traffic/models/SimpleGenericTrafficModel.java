package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.models.base.DayGeneratedTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.GeneralAviationMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GeneralCommercialMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public class SimpleGenericTrafficModel extends DayGeneratedTrafficModel {

  public static class MovementsForHour {
    public static MovementsForHour create(int count, double generalAviationProbability, double departureProbability) {
      return new MovementsForHour(count, generalAviationProbability, departureProbability);
    }

    private final int count;
    private final double generalAviationProbability;
    private final double departureProbability;

    public MovementsForHour(int count, double generalAviationProbability, double departureProbability) {
      EAssert.Argument.isTrue(count >= 0);
      EAssert.Argument.isTrue(generalAviationProbability >= 0 && generalAviationProbability <= 1);
      EAssert.Argument.isTrue(departureProbability > 0 && departureProbability <= 1);
      this.count = count;
      this.generalAviationProbability = generalAviationProbability;
      this.departureProbability = departureProbability;
    }
  }

  public static class ValueAndWeight {
    public static ValueAndWeight create(String value, int weight) {
      return new ValueAndWeight(value, weight);
    }

    private final String value;
    private final int weight;

    private ValueAndWeight(String value, int weight) {
      EAssert.Argument.isNonemptyString(value, "value");
      EAssert.Argument.isTrue(weight >= 0);
      this.value = value;
      this.weight = weight;
    }

    public String getValue() {
      return value;
    }

    public int getWeight() {
      return weight;
    }
  }

  public static SimpleGenericTrafficModel create(
      MovementsForHour[] movementsForHours,
      IList<ValueAndWeight> companies,
      IList<ValueAndWeight> countries,
      double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns) {
    return new SimpleGenericTrafficModel(
        movementsForHours, companies, countries, delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);
  }

  private final double delayProbability;
  private final int maxDelayInMinutesPerStep;
  private final boolean useExtendedCallsigns;
  private final MovementsForHour[] movementsForHours;
  private final IList<ValueAndWeight> companies;
  private final IList<ValueAndWeight> countries;
  private final ERandom rnd = new ERandom();

  private SimpleGenericTrafficModel(MovementsForHour[] movementsForHours, IList<ValueAndWeight> companies, IList<ValueAndWeight> countries, double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns) {
    EAssert.Argument.isNotNull(movementsForHours, "movementsForHours");
    EAssert.Argument.isTrue(movementsForHours.length == 24);
    EAssert.Argument.isNotNull(companies, "companies");
    EAssert.Argument.isTrue(companies.isEmpty() == false);
    EAssert.Argument.isNotNull(countries, "countries");
    EAssert.Argument.isTrue(countries.isEmpty() == false);
    EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, delayProbability, 1));
    EAssert.Argument.isTrue(maxDelayInMinutesPerStep >= 0);
    this.delayProbability = delayProbability;
    this.maxDelayInMinutesPerStep = maxDelayInMinutesPerStep;
    this.useExtendedCallsigns = useExtendedCallsigns;
    this.movementsForHours = movementsForHours;
    this.companies = companies;
    this.countries = countries;
  }

  //  private final double probabilityOfNonCommercialFlight;
//  private final double probabilityOfDeparture; // 0-1
//  private final int[] movementsPerHour; // int[24]
//  private ERandom rnd = SharedInstanceProvider.getRnd();

//  public SimpleGenericTrafficModel(int[] movementsPerHour,
//                                   double probabilityOfDeparture, double probabilityOfNonCommercialFlight) {
//    EAssert.isNotNull(movementsPerHour);
//    EAssert.isTrue(movementsPerHour.length == 24);
//    for (int i : movementsPerHour) {
//      EAssert.isTrue(i >= 0, new IllegalArgumentException("Argument \"movementsPerHour\" must have all elements equal or greater than 0."));
//    }
//    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, probabilityOfDeparture, 1));
//
//    this.movementsPerHour = Arrays.copyOf(movementsPerHour, 24);
//    this.probabilityOfDeparture = probabilityOfDeparture;
//    this.probabilityOfNonCommercialFlight = probabilityOfNonCommercialFlight;
//  }

  @Override
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < 24; i++) {
      MovementsForHour mvm = movementsForHours[i];
      for (int c = 0; c < mvm.count; c++) {
        MovementTemplate tmp = generateMovement(i, mvm);
        ret.add(tmp);
      }
    }

    return ret;
  }

  private MovementTemplate generateMovement(int hour, MovementsForHour mvm) {
    ETimeStamp initTime = new ETimeStamp(hour, rnd.nextInt(0, 60), rnd.nextInt(0, 60));
    MovementTemplate.eKind kind = (rnd.nextDouble() <= mvm.departureProbability) ?
        MovementTemplate.eKind.departure : MovementTemplate.eKind.arrival;
    boolean isNonCommercial = rnd.nextDouble() < mvm.generalAviationProbability;
    int radial = rnd.nextInt(360);
    int delay = 0;


    while (rnd.nextDouble() < delayProbability) {
      delay += rnd.nextInt(maxDelayInMinutesPerStep);
    }

    MovementTemplate ret;
    if (isNonCommercial) {
      String countryIcao = this.countries.getRandomByWeights(q -> (double) q.weight, rnd).value;
      ret = new GeneralAviationMovementTemplate(kind, initTime, countryIcao,
          new EntryExitInfo(radial),
          delay);
    } else {
      String companyIcao = this.companies.getRandomByWeights(q -> (double) q.weight, rnd).value;
      ret = new GeneralCommercialMovementTemplate(companyIcao, null,
          kind, initTime, new EntryExitInfo(radial),
          delay);

    }

    return ret;
  }
}
