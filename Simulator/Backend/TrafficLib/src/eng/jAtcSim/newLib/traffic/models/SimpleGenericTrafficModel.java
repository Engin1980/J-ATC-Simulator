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

import java.util.Arrays;

public class SimpleGenericTrafficModel implements ITrafficModel {

  public static class MovementsForHour {
    public static MovementsForHour create(int count, double generalAviationProbability, double departureProbability) {
      return new MovementsForHour(count, generalAviationProbability, departureProbability);
    }

    public final int count;
    public final double departureProbability;
    public final double generalAviationProbability;

    public MovementsForHour(int count, double generalAviationProbability, double departureProbability) {
      EAssert.Argument.isTrue(count >= 0);
      EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, generalAviationProbability, 1),
          "General-aviation probability must be between 0 and 1.");
      EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, departureProbability, 1),
          "Departure probability must be between 0 and 1.");
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

  //TODEL some of the following methods is probably not used, delete them if able
  public static SimpleGenericTrafficModel create(
      MovementsForHour[] movementsForHours,
      IList<ValueAndWeight> companies,
      IList<ValueAndWeight> countries) {
    return new SimpleGenericTrafficModel(movementsForHours, companies, countries);
  }

  public static SimpleGenericTrafficModel create(
      MovementsForHour[] movementsForHours,
      IMap<String, Integer> companies,
      IMap<String, Integer> countries) {
    IList<ValueAndWeight> lstCompanies = companies.getEntries().select(q -> new ValueAndWeight(q.getKey(), q.getValue())).toList();
    IList<ValueAndWeight> lstCountries = countries.getEntries().select(q -> new ValueAndWeight(q.getKey(), q.getValue())).toList();
    return new SimpleGenericTrafficModel(movementsForHours, lstCompanies, lstCountries);
  }

  private final IList<ValueAndWeight> companies;
  private final IList<ValueAndWeight> countries;
  private final MovementsForHour[] movementsForHours;
  private final ERandom rnd = Context.getApp().getRnd();

  private SimpleGenericTrafficModel(
      MovementsForHour[] movementsForHours,
      IList<ValueAndWeight> companies,
      IList<ValueAndWeight> countries) {
    EAssert.Argument.isNotNull(movementsForHours, "movementsForHours");
    EAssert.Argument.isTrue(movementsForHours.length == 24);
    EAssert.Argument.isNotNull(companies, "companies");
    EAssert.Argument.isTrue(companies.isEmpty() == false);
    EAssert.Argument.isNotNull(countries, "countries");
    EAssert.Argument.isTrue(countries.isEmpty() == false);

    this.movementsForHours = Arrays.copyOf(movementsForHours, movementsForHours.length);
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
