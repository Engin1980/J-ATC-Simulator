package exml;

import eng.eSystem.eXml.XElement;

public interface ISimPersistable {

  void save(XElement elm, XContext ctx);

  void load(XElement elm, XContext ctx);
}
