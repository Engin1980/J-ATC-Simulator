package eng.jAtcSim.newLib.xml.fleets.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.CountryFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class GeneralAviationFleetsXmlLoader {
  public GeneralAviationFleets load(XElement root) {
    EAssert.isTrue(root.getName().equals("airlinersFleets"), "Incorrect loading xml element");

    IList<CountryFleet> tmp = new EDistinctList<>(q->q.getCountryCode(), EDistinctList.Behavior.exception);
    XmlLoaderUtils.loadList(
        root.getChildren("company"),
        tmp,
        q-> loadCountry(q));

    CountryFleet defaultFleet = loadDefault(root.getChild("default"));

    GeneralAviationFleets ret = GeneralAviationFleets.create(tmp, defaultFleet);
    return ret;
  }

  private CountryFleet loadDefault(XElement source) {
    String prefix = XmlLoaderUtils.loadString(source, "aircraftPrefix");
    IList<TypeAndWeight> types = XmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CountryFleet ret = new CountryFleet("-default-", prefix, "-default-", types);
    return ret;
  }

  private CountryFleet loadCountry(XElement source) {
    String code = XmlLoaderUtils.loadString(source, "code");
    String prefix = XmlLoaderUtils.loadString(source, "aircraftPrefix");
    String name = XmlLoaderUtils.loadString(source, "name", "(N/A)");
    IList<TypeAndWeight> types = XmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CountryFleet ret = new CountryFleet(code, prefix, name, types);
    return ret;
  }

  private TypeAndWeight loadFleet(XElement source){
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    int weight = XmlLoaderUtils.loadInteger("weight");

    TypeAndWeight ret = TypeAndWeight.create(name, weight);
    return ret;
  }
}
