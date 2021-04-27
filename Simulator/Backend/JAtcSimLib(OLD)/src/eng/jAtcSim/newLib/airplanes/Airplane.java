//package eng.jAtcSim.newLib.area.airplanes;
//
//import eng.eSystem.collections.IList;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.exceptions.ApplicationException;
//import eng.eSystem.exceptions.EEnumValueUnsupportedException;
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.geo.Coordinates;
//import eng.eSystem.utilites.EnumUtils;
//import eng.eSystem.utilites.NumberUtils;
//import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
//import eng.jAtcSim.newLib.Acc;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplane4Atc;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplane4Mrva;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteAdvanced;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
//import eng.jAtcSim.newLib.area.airplanes.moods.Mood;
//import eng.jAtcSim.newLib.area.airplanes.moods.MoodResult;
//import eng.jAtcSim.newLib.area.airplanes.navigators.HeadingNavigator;
//import eng.jAtcSim.newLib.area.airplanes.navigators.INavigator;
//import eng.jAtcSim.newLib.area.airplanes.navigators.ToCoordinateNavigator;
//import eng.jAtcSim.newLib.area.airplanes.behaviors.*;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.*;
//import eng.jAtcSim.newLib.area.airplanes.modules.*;
//import eng.jAtcSim.newLib.area.atcs.Atc;
//import eng.jAtcSim.newLib.area.exceptions.ToDoException;
//import eng.jAtcSim.newLib.messaging.IMessageContent;
//import eng.jAtcSim.newLib.messaging.Message;
//import eng.jAtcSim.newLib.area.speaking.IFromAtc;
//import eng.jAtcSim.newLib.area.speaking.ISpeech;
//import eng.jAtcSim.newLib.area.speaking.SpeechList;
//import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.DivertingNotification;
//import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.GoingAroundNotification;
//import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ChangeAltitudeCommand;
//import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ChangeHeadingCommand;
//import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ProceedDirectCommand;
//import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ThenCommand;
//import eng.jAtcSim.newLib.global.ETime;
//import eng.jAtcSim.newLib.global.Headings;
//import eng.jAtcSim.newLib.global.Restriction;
//import eng.jAtcSim.newLib.global.UnitProvider;
//import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;
//import eng.jAtcSim.newLib.world.Navaid;
//import eng.jAtcSim.newLib.world.DARoute;
//import eng.jAtcSim.newLib.world.approaches.NewApproachInfo;
//
//public class Airplane  {
//
//
//}
