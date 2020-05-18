package eng.jAtcSim.newLib.xml.fleets.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

public class AirlinesFleetsXmlLoader {
  public AirlinesFleets load(XElement root) {
    EAssert.isTrue(root.getName().equals("airlinersFleets"), "Incorrect loading xml element");

    IList<CompanyFleet> tmp = new EDistinctList<>(q->q.getIcao(), EDistinctList.Behavior.exception);
    SmartXmlLoaderUtils.loadList(
        root.getChildren("company"),
        tmp,
        q->loadCompany(q));

    CompanyFleet defaultFleet = loadDefault(root.getChild("default"));

    AirlinesFleets ret = AirlinesFleets.create(tmp, defaultFleet);
    return ret;
  }

  private CompanyFleet loadDefault(XElement source) {
    IList<TypeAndWeight> types = SmartXmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CompanyFleet ret = new CompanyFleet("-default-", "-default-", types);
    return ret;
  }

  private CompanyFleet loadCompany(XElement source) {
    String icao = SmartXmlLoaderUtils.loadString(source, "icao");
    String name = SmartXmlLoaderUtils.loadString(source, "name", "(N/A)");
    IList<TypeAndWeight> types = SmartXmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CompanyFleet ret = new CompanyFleet(icao, name, types);
    return ret;
  }

  private TypeAndWeight loadFleet(XElement source){
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    int weight = SmartXmlLoaderUtils.loadInteger("weight");

    TypeAndWeight ret = TypeAndWeight.create(name, weight);
    return ret;
  }
}
