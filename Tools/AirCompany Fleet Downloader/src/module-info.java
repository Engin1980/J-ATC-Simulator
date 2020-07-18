module eng.jAtcSim.tools.AirCompanyFleetDownloader {
  requires eng.eXmlSerialization;
  requires eng.eSystem;
  opens eng.airCompanyFleetDownloader to eng.eXmlSerialization;
}