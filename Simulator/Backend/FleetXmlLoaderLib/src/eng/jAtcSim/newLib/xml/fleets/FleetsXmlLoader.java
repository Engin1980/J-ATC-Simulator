package eng.jAtcSim.newLib.xml.fleets;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.CountryFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import exml.loading.XLoadContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FleetsXmlLoader {

  public static AirlinesFleets loadCompanyFleet(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new EApplicationException(sf("Failed to load airlines fleets from '{0}'.", fileName));
    }

    AirlinesFleets ret = loadAirlines(doc.getRoot());
    return ret;
  }

  public static GeneralAviationFleets loadGeneralAviationFleets(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new EApplicationException(sf("Failed to load G-A fleets from '{0}'.", fileName));
    }

    GeneralAviationFleets ret = loadGAs(doc.getRoot());
    return ret;
  }

  private static AirlinesFleets loadAirlines(XElement elm) {
    XLoadContext ctx = new XLoadContext();

    IList<CompanyFleet> lst = new EList<>();
    elm.getChildren("company").forEach(q ->
            lst.add(loadCompanyFleet(
                    q.getAttribute("icao"),
                    q.tryGetAttribute("name").orElse(""),
                    q.getChildren("type"),
                    ctx)));
    CompanyFleet def = loadCompanyFleet(
            elm.getChild("default").getAttribute("icao"),
            "-default-",
            elm.getChild("default").getChildren("type"),
            ctx);
    AirlinesFleets ret = AirlinesFleets.create(lst, def);
    return ret;
  }

  private static CompanyFleet loadCompanyFleet(String icao, String name, IReadOnlyList<XElement> types, XLoadContext ctx) {
    CompanyFleet ret;

    IList<TypeAndWeight> tws = loadTypesAndWeights(types, ctx);

    ret = new CompanyFleet(icao, name, tws);
    return ret;
  }

  private static IList<TypeAndWeight> loadTypesAndWeights(IReadOnlyList<XElement> types, XLoadContext ctx) {
    IList<TypeAndWeight> ret = new EList<>();
    types.forEach(q -> {
      TypeAndWeight tw = TypeAndWeight.create(
              q.getAttribute("name"),
              Integer.parseInt(q.getAttribute("weight"))
      );
      ret.add(tw);
    });
    return ret;
  }

  private static GeneralAviationFleets loadGAs(XElement elm) {
    XLoadContext ctx = new XLoadContext();

    IList<CountryFleet> lst = new EList<>();
    elm.getChildren("country").forEach(q -> lst.add(loadCountryFleet(
            q.getAttribute("code"),
            q.getAttribute("aircraftPrefix"),
            q.tryGetAttribute("name").orElse(""),
            q.getChildren("type"),
            ctx)));
    CountryFleet def = loadCountryFleet(
            "DEF",
            elm.getChild("default").getAttribute("aircraftPrefix"),
            "-default-",
            elm.getChild("default").getChildren("type"),
            ctx);
    GeneralAviationFleets ret = GeneralAviationFleets.create(lst, def);
    return ret;
  }

  private static CountryFleet loadCountryFleet(String countryCode, String aircraftPrefix, String name,
                                               IReadOnlyList<XElement> types, XLoadContext ctx) {
    CountryFleet ret;
    IList<TypeAndWeight> tws = loadTypesAndWeights(types, ctx);

    ret = new CountryFleet(countryCode, aircraftPrefix, name, tws);
    return ret;
  }
}
