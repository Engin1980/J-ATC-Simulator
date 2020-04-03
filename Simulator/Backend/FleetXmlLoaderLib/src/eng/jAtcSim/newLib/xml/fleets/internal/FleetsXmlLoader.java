package eng.jAtcSim.newLib.xml.fleets.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.CompanyFleet;
import eng.jAtcSim.newLib.fleet.FleetType;
import eng.jAtcSim.newLib.fleet.Fleets;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class FleetsXmlLoader {
  public Fleets load(XElement root) {
    EAssert.isTrue(root.getName().equals("fleets"), "Incorrect loading xml element");

    IList<CompanyFleet> tmp = new EDistinctList<>(q->q.getIcao(), EDistinctList.Behavior.exception);
    XmlLoaderUtils.loadList(
        root.getChildren("company"),
        tmp,
        q->loadCompany(q));

    Fleets ret = Fleets.create(tmp);
    return ret;
  }

  private CompanyFleet loadCompany(XElement source) {
    String icao = XmlLoaderUtils.loadString(source, "icao");
    String name = XmlLoaderUtils.loadString(source, "name", "(N/A)");
    IList<FleetType> types = XmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CompanyFleet ret = new CompanyFleet(icao, name, types);
    return ret;
  }

  private FleetType loadFleet(XElement source){
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    int weight = XmlLoaderUtils.loadInteger("weight");

    FleetType ret = FleetType.create(name, weight);
    return ret;
  }


}
