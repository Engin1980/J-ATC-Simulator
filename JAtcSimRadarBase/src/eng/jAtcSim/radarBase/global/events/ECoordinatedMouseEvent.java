///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jatcsim.JAtcSimRada.radarBase.global.events;
//
//import eng.jAtcSim.lib.coordinates.Coordinate;
//
///**
// *
// * @author Marek Vajgl
// */
//public class ECoordinatedMouseEvent extends EMouseEvent {
//
//
//  public final double lat;
//  public final double lng;
//  public final double dropLat;
//  public final double dropLng;
//
//  public ECoordinatedMouseEvent(double lat, double lng, double dropLat, double dropLng, int x, int y, int dropX, int dropY, int wheel, eButton button, EKeyboardModifier modifiers, eType type) {
//    super(x, y, dropX, dropY, wheel, button, modifiers, type);
//    this.lat = lat;
//    this.lng = lng;
//    this.dropLat = dropLat;
//    this.dropLng = dropLng;
//  }
//
//  public static ECoordinatedMouseEvent create(Coordinate coord, Coordinate dropCoord, EMouseEvent e) {
//    ECoordinatedMouseEvent ret = new ECoordinatedMouseEvent(
//      coord.getLatitude().getContent(), coord.getLongitude().getContent(),
//      dropCoord.getLatitude().getContent(), dropCoord.getLongitude().getContent(),
//      e.x, e.y, e.dropX, e.dropY, e.wheel, e.button, e.modifiers, e.type);
//    return ret;
//  }
//
//  public static ECoordinatedMouseEvent create(Coordinate coord, EMouseEvent e) {
//    ECoordinatedMouseEvent ret = new ECoordinatedMouseEvent(
//      coord.getLatitude().getContent(), coord.getLongitude().getContent(),
//      0,0,
//      e.x, e.y, e.dropX, e.dropY, e.wheel, e.button, e.modifiers, e.type);
//    return ret;
//  }
//
//  public Coordinate getCoordinate(){
//    return new Coordinate(lat, lng);
//  }
//
//  public Coordinate getDropCoordinate(){
//    return new Coordinate(dropLat, dropLng);
//  }
//
//}
