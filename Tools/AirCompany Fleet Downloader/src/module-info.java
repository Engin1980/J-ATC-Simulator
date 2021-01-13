module AirCompanyFleetDownloader {
  requires eng.eSystem;
  requires eng.eXmlSerialization;
  requires AnotherXml;
  opens eng.airCompanyFleetDownloader to eng.eXmlSerialization;
}
