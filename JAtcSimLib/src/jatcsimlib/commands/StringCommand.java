/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

/**
 *
 * @author Marek Vajgl
 */
public class StringCommand extends Command {
  private final String text;

  public StringCommand(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
}
