package eng.jAtcSim.newLib.xml.fleets.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;
import eng.jAtcSim.newLib.fleet.generalAviation.CountryFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

public class GeneralAviationFleetsXmlLoader {
  //TODO resolve this later
  private static final boolean ERROR_ON_DUPLICIT_KEYS = false;
  public GeneralAviationFleets load(XElement root) {
    EAssert.isTrue(root.getName().equals("generalAviationFleets"), "Incorrect loading xml element");

    IList<CountryFleet> tmp =  ERROR_ON_DUPLICIT_KEYS ?
        new EDistinctList<>(q->q.getCountryCode(), EDistinctList.Behavior.exception) :
        new EDistinctList<>(q->q.getCountryCode(), EDistinctList.Behavior.skip);
    SmartXmlLoaderUtils.loadList(
        root.getChildren("country"),
        tmp,
        q-> loadCountry(q));

    CountryFleet defaultFleet = loadDefault(root.getChild("default"));

    GeneralAviationFleets ret = GeneralAviationFleets.create(tmp, defaultFleet);
    return ret;
  }

  private CountryFleet loadDefault(XElement source) {
    String prefix = SmartXmlLoaderUtils.loadString(source, "aircraftPrefix");
    IList<TypeAndWeight> types = SmartXmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CountryFleet ret = new CountryFleet("-default-", prefix, "-default-", types);
    return ret;
  }

  private CountryFleet loadCountry(XElement source) {
    String code = SmartXmlLoaderUtils.loadString(source, "code");
    String prefix = SmartXmlLoaderUtils.loadString(source, "aircraftPrefix");
    String name = SmartXmlLoaderUtils.loadString(source, "name", "(N/A)");
    IList<TypeAndWeight> types = SmartXmlLoaderUtils.loadList(
        source.getChildren("type"),
        q -> loadFleet(q)
    );

    CountryFleet ret = new CountryFleet(code, prefix, name, types);
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
