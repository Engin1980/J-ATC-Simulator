package exml;

import eng.eSystem.eXml.XElement;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

public interface IXPersistable {
  default void load(XElement elm, XLoadContext ctx) {
    // intentionally blank, can be overridden/implemented
  }

  default void save(XElement elm, XSaveContext ctx) {
    // intentionally blank, can be overridden/implemented
  }

  default void postLoad(XLoadContext ctx) {
    // intentionally blank, can be overriden/implemented
  }
}
