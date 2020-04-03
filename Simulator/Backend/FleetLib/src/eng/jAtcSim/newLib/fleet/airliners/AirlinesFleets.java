package eng.jAtcSim.newLib.fleet.airliners;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.validation.EAssert;

public class AirlinesFleets {

  public static AirlinesFleets create(IList<CompanyFleet> companyFleets, CompanyFleet defaultFleet) {
    return new AirlinesFleets(companyFleets, defaultFleet);
  }

  private final IList<CompanyFleet> inner = new EDistinctList<>(q->q.getIcao(), EDistinctList.Behavior.exception);
  private final CompanyFleet defaultFleet;

  private AirlinesFleets(IList<CompanyFleet> companyFleets, CompanyFleet defaultFleet) {
    EAssert.Argument.isNotNull(companyFleets, "companyFleets");
    EAssert.Argument.isNotNull(defaultFleet, "defaultFleet");
    EAssert.Argument.isTrue(defaultFleet.getTypes().isEmpty() == false, "Default fleet for airliens contains no types.");
    this.inner.add(companyFleets);
    this.defaultFleet = defaultFleet;
  }

  public IList<CompanyFleet> getCompaniesByIcao(String[] companies) {
    return inner.where(q ->
        ArrayUtils.contains(companies, q.getIcao()));
  }

  public String[] getIcaos() {
    IList<String> lst = this.inner.select(q -> q.getIcao());
    String[] ret = lst.toArray(String.class);
    return ret;
  }

  public CompanyFleet tryGetByIcao(String companyIcao) {
    CompanyFleet ret = this.inner.tryGetFirst(q -> q.getIcao().equals(companyIcao));
    return ret;
  }
}
