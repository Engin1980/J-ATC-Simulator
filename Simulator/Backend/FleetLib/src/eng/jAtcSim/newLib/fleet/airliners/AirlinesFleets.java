package eng.jAtcSim.newLib.fleet.airliners;

import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.Fleets;

public class AirlinesFleets extends Fleets<CompanyFleet> {

  public static AirlinesFleets create(IList<CompanyFleet> companyFleets, CompanyFleet defaultFleet) {
    return new AirlinesFleets(companyFleets, defaultFleet);
  }

  private AirlinesFleets(IList<CompanyFleet> companyFleets, CompanyFleet defaultFleet) {
    super(companyFleets, q -> q.getIcao(), defaultFleet);
    EAssert.Argument.isTrue(defaultFleet.getTypes().isEmpty() == false, "Default fleet for airliners contains no types.");
  }
}
