package eng.jAtcSim.newLib.fleet;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.validation.EAssert;

public class Fleets {

  public static Fleets create(IList<CompanyFleet> companyFleets) {
    return new Fleets(companyFleets);
  }

  private final IList<CompanyFleet> inner = new EDistinctList<>(q->q.getIcao(), EDistinctList.Behavior.exception);

  private Fleets(IList<CompanyFleet> companyFleets) {
    EAssert.Argument.isNotNull(companyFleets, "companyFleets");
    this.inner.add(companyFleets);
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
