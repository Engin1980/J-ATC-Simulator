package eng.jAtcSim.lib.traffic.fleets;

import java.util.ArrayList;

public class Fleets extends ArrayList<CompanyFleet> {

  private static final String DEFAULT_AIRPLANE_TYPE_NAME = "A319";

  public CompanyFleet tryGetByIcao(String companyIcao) {
    
    CompanyFleet ret = null;
    for (CompanyFleet cf : this) {
      if (cf.icao.equals(companyIcao)) {
        ret = cf;
        break;
      }
    }
    return ret;
  }

  public static CompanyFleet getDefaultCompanyFleet() {
    CompanyFleet ret = new CompanyFleet();
    ret.icao = "(DEF)";
    FleetType ft = new FleetType();
    ft.name = DEFAULT_AIRPLANE_TYPE_NAME;
    ft.weight = 1;
    ret.add(ft);
    return ret;
  }
}
