module AirCompanyFleetDownloader {
  requires eng.eSystem;
  requires eng.eXmlSerialization;
  opens eng.airCompanyFleetDownloader to eng.eXmlSerialization;
}