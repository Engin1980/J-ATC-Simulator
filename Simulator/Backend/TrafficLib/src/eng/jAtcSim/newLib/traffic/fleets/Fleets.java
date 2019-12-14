package eng.jAtcSim.newLib.traffic.fleets;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ArrayUtils;

public class Fleets {

  public static Fleets load(XElement source) {

    IList<CompanyFleet> tmp = new EList<>();

    for (XElement child : source.getChildren("company")) {
      CompanyFleet cf = CompanyFleet.load(child);
      tmp.add(cf);
    }

    Fleets ret = new Fleets(tmp);
    return ret;
  }

  public static CompanyFleet getDefaultCompanyFleet() {
    CompanyFleet ret = CompanyFleet.getDefault();
    return ret;
  }

  private final IList<CompanyFleet> inner;

  private Fleets(IList<CompanyFleet> inner) {
    this.inner = inner;
  }

  public IList<CompanyFleet> getCompaniesByIcao(String[] companies) {
    return inner.where( q ->
        ArrayUtils.contains(companies, q.getIcao()));
  }

  public String[] getIcaos() {
    IList<String> lst = this.inner.select(q -> q.getIcao());
    String[] ret = lst.toArray(String.class);
    return ret;
  }

  public CompanyFleet tryGetByIcao(String companyIcao) {
    CompanyFleet ret = this.inner.tryGetFirst(q->q.getIcao().equals(companyIcao));
    return ret;
  }
}
