package exml;

import eng.eSystem.eXml.XElement;

public interface ISimPersistable {

  void save(XElement elm, XmlContext ctx);

  void load(XElement elm, XmlContext ctx);
}
