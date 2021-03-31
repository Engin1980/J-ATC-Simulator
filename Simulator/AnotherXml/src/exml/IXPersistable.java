package exml;

import eng.eSystem.eXml.XElement;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

public interface IXPersistable {
  default void xLoad(XElement elm, XLoadContext ctx) {
    // intentionally blank, can be overridden/implemented
  }

  default void xSave(XElement elm, XSaveContext ctx) {
    // intentionally blank, can be overridden/implemented
  }

  default void xPostLoad(XLoadContext ctx) {
    // intentionally blank, can be overriden/implemented
  }
}
