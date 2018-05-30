/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import com.sun.org.apache.bcel.internal.generic.RET;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.lib.traffic.fleets.FleetType;
import eng.jAtcSim.lib.traffic.fleets.Fleets;

/**
 * @author Marek Vajgl
 */
public class GenericTraffic extends Traffic {

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

  private GenericTraffic() {
    this.probabilityOfDeparture = 0.5;
    this.probabilityOfNonCommercialFlight = 0;
    this.companies = new String[0];
    this.countryCodes = new String[0];
  }

  public GenericTraffic(String companies, String countryCodes, int movementsPerHour, double probabilityOfDeparture, double probabilityOfNonCommercialFlight,
                        int trafficCustomWeightTypeA, int trafficCustomWeightTypeB, int trafficCustomWeightTypeC, int trafficCustomWeightTypeD,
                        boolean useExtendedCallsigns) {

    if (movementsPerHour < 0) {
      throw new IllegalArgumentException("Argument \"movementsPerHour\" must be equal or greater than 0.");
    }

    if (NumberUtils.isBetweenOrEqual(0, probabilityOfDeparture, 1) == false) {
      throw new IllegalArgumentException("\"probabilityOfDeparture\" must be between 0 and 1.");
    }

    this.companies = companies.split(";");
    this.countryCodes = countryCodes.split(";");

    for (int i = 0; i < this.movementsPerHour.length; i++) {
      this.movementsPerHour[i] = movementsPerHour;
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

//
//  @Override
//  public void generateNewMovementsIfRequired() {
//    if (nextHourToGenerateTraffic != -1 && Acc.now().getHours() != nextHourToGenerateTraffic) {
//      return;
//    }
//
//    int currentHour = Acc.now().getHours();
//    int expMovs = movementsPerHour[currentHour];
//    for (int i = 0; i < expMovs; i++) {
//      Movement m = generateMovement(currentHour);
//      super.addScheduledMovement(m);
//    }
//    nextHourToGenerateTraffic = currentHour + 1;
//    if (nextHourToGenerateTraffic > 23) {
//      nextHourToGenerateTraffic = 0;
//    }
//  }

  private char getRandomCategory(String companyName) {
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
    String prefix;
    if (isNonCommercial)
      prefix = this.countryCodes[Acc.rnd().nextInt(this.countryCodes.length)];
    else
      prefix = this.companies[Acc.rnd().nextInt(this.companies.length)];
    Callsign cls = generateUnusedCallsign(prefix, isNonCommercial);

    int delayInMinutes = generateDelayMinutes();

    AirplaneType type;
    char category = getRandomCategory(prefix);
    if (isNonCommercial) {
      type = Acc.sim().getAirplaneTypes().getRandomFromCategory(category);
    } else {
      type = getTypeByCategoryAndCompany(prefix, category);
    }

    Movement ret = new Movement(cls, type, initTime, delayInMinutes, isDeparture);
    return ret;

  }

  private AirplaneType getTypeByCategoryAndCompany(String companyCode, char category) {
    CompanyFleet cf = Acc.fleets().tryGetByIcao(companyCode);
    if (cf == null) cf = Fleets.getDefaultCompanyFleet();

    FleetType available = cf.tryGetRandomByCategory(category);
    if (available == null){
      throw new EApplicationException("Unable to find any type for category " + category + " for company " + companyCode + " in loaded fleet. Check fleet?");
    }
    AirplaneType ret = available.getAirplaneType();
    if (ret == null)
      ret = cf.getRandom().getAirplaneType();

    return ret;
  }

}

