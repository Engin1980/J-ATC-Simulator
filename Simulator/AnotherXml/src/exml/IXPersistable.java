package exml;

import eng.eSystem.eXml.XElement;

public interface IXPersistable {
  default void load(XElement elm, XContext ctx) {
    // intentionally blank, can be overridden/implemented
  }

  default void save(XElement elm, XContext ctx) {
    // intentionally blank, can be overridden/implemented
  }

  default void postLoad(XContext ctx) {
    // intentionally blank, can be overriden/implemented
  }
}
