package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.traffic.contextLocal.Context;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.GenericCommercialMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GenericGeneralAviationMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public class SimpleGenericTrafficModel implements ITrafficModel {

  public static class MovementsForHour {
    public static MovementsForHour create(int count, double generalAviationProbability, double departureProbability) {
      return new MovementsForHour(count, generalAviationProbability, departureProbability);
    }

    public final int count;
    public final Double departureProbability;
    public final Double generalAviationProbability;

    public MovementsForHour() {
      this.count = 0;
      this.departureProbability = 0.5;
      this.generalAviationProbability = 0.2;
    }

    public MovementsForHour(int count, Double generalAviationProbability, Double departureProbability) {
      EAssert.Argument.isTrue(count >= 0);
      EAssert.Argument.isTrue(generalAviationProbability == null || NumberUtils.isBetweenOrEqual(0, generalAviationProbability, 1),
          "General-aviation probability must be empty or between 0 and 1.");
      EAssert.Argument.isTrue(departureProbability == null || NumberUtils.isBetweenOrEqual(0, departureProbability, 1),
          "Departure probability must be empty or between 0 and 1.");
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
      double globalGeneralAviationProbability,
      double globalDepartureProbability,
      MovementsForHour[] movementsForHours,
      IList<ValueAndWeight> companies,
      IList<ValueAndWeight> countries) {
    return new SimpleGenericTrafficModel(globalGeneralAviationProbability,
        globalDepartureProbability, movementsForHours, companies, countries);
  }

  public static SimpleGenericTrafficModel create(
      double globalGeneralAviationProbability,
      double globalDepartureProbability,
      MovementsForHour[] movementsForHours,
      IMap<String, Integer> companies,
      IMap<String, Integer> countries) {
    IList<ValueAndWeight> lstCompanies = companies.getEntries().select(q -> new ValueAndWeight(q.getKey(), q.getValue())).toList();
    IList<ValueAndWeight> lstCountries = countries.getEntries().select(q -> new ValueAndWeight(q.getKey(), q.getValue())).toList();
    return new SimpleGenericTrafficModel(globalGeneralAviationProbability,
        globalDepartureProbability, movementsForHours, lstCompanies, lstCountries);
  }

  private final IList<ValueAndWeight> companies;
  private final IList<ValueAndWeight> countries;
  private final MovementsForHour[] movementsForHours;
  private final ERandom rnd = Context.getShared().getRnd();

  private SimpleGenericTrafficModel(
      double globalGeneralAviationProbability,
      double globalDepartureProbability,
      MovementsForHour[] movementsForHours,
      IList<ValueAndWeight> companies,
      IList<ValueAndWeight> countries) {
    EAssert.Argument.isNotNull(movementsForHours, "movementsForHours");
    EAssert.Argument.isTrue(movementsForHours.length == 24);
    EAssert.Argument.isNotNull(companies, "companies");
    EAssert.Argument.isTrue(companies.isEmpty() == false);
    EAssert.Argument.isNotNull(countries, "countries");
    EAssert.Argument.isTrue(countries.isEmpty() == false);

    this.movementsForHours = new MovementsForHour[24];
    for (int i = 0; i < this.movementsForHours.length; i++) {
      this.movementsForHours[i] = new MovementsForHour(
          movementsForHours[i].count,
          movementsForHours[i].departureProbability == null ?
              globalDepartureProbability : movementsForHours[i].departureProbability,
          movementsForHours[i].generalAviationProbability == null ?
              globalGeneralAviationProbability : movementsForHours[i].generalAviationProbability);

      EAssert.Argument.isTrue(
          NumberUtils.isBetweenOrEqual(0, this.movementsForHours[i].generalAviationProbability, 1));
      EAssert.Argument.isTrue(
          NumberUtils.isBetweenOrEqual(0, this.movementsForHours[i].departureProbability, 1));
    }

    this.companies = companies;
    this.countries = countries;
  }

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

    MovementTemplate ret;
    if (isNonCommercial) {
      String countryIcao = this.countries.getRandomByWeights(q -> (double) q.weight, rnd).value;
      ret = new GenericGeneralAviationMovementTemplate(kind, initTime, countryIcao,
          new EntryExitInfo(radial));
    } else {
      String companyIcao = this.companies.getRandomByWeights(q -> (double) q.weight, rnd).value;
      ret = new GenericCommercialMovementTemplate(companyIcao, null,
          kind, initTime, new EntryExitInfo(radial));
    }

    return ret;
  }
}
