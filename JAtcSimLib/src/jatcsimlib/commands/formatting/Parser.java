/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandList;

/**
 *
 * @author Marek Vajgl
 */
public interface Parser {
  public Command parseOne(String text);
  public CommandList parseMulti(String text);
  public String getHelp();
  public String getHelp(String commandPrefix);
}
