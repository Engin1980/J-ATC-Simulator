package eng.jAtcSim.newLib.fleet;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;

public abstract class Fleets<T> {
  private final IList<T> inner = new EList<>();
  private final Selector<T, String> byIcaoSelector;
  private final T defaultItem;

  public Fleets(IReadOnlyList<T> items, Selector<T, String> byIcaoSelector, T defaultItem) {
    EAssert.Argument.isNotNull(items, "items");
    EAssert.Argument.isNotNull(byIcaoSelector, "byIcaoSelector");
    EAssert.Argument.isNotNull(defaultItem, "defaultItem");
    this.byIcaoSelector = byIcaoSelector;
    this.defaultItem = defaultItem;
  }

//  public IList<CompanyFleet> getCompaniesByIcao(String[] companies) {
//    return inner.where(q ->
//        ArrayUtils.contains(companies, q.getIcao()));
//  }

  public T getDefault() {
    return defaultItem;
  }

  public String[] getIcaos() {
    IList<String> lst = this.inner.select(q -> byIcaoSelector.select(q));
    String[] ret = lst.toArray(String.class);
    return ret;
  }

  public T getRandom() {
    return inner.getRandom();
  }

  public T tryGetByIcao(String icao) {
    T ret = this.inner.tryGetFirst(q -> byIcaoSelector.select(q).equals(icao));
    return ret;
  }

  public  T tryGetByIcaoOrDefault(String icao){
    T ret = tryGetByIcao(icao);
    if (ret == null)
      ret = getDefault();
    return ret;
  }
}
