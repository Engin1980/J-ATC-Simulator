package eng.jAtcSim.startup.extenders;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SwingFactory {

  public enum FileDialogType {
    area,
    fleets,
    types,
    traffic,
    game,
    startupSettings,
    folder
  }

  public static JScrollBar createHorizontalBar(int minimum, int maximum, int value) {
    JScrollBar ret = new JScrollBar(JScrollBar.HORIZONTAL);
    ret.getModel().setRangeProperties(value, 0, minimum, maximum, true);
    return ret;
  }

  public static JFileChooser createFileDialog(FileDialogType type, String defaultFile) {
    JFileChooser ret = new JFileChooser();

    ret.setAcceptAllFileFilterUsed(false);
    switch (type) {
      case area:
        ret.addChoosableFileFilter(new FileTypeFilter(".ar.xml", "area files"));
        break;
      case fleets:
        ret.addChoosableFileFilter(new FileTypeFilter(".fl.xml", "Fleet files"));
        break;
      case startupSettings:
        ret.addChoosableFileFilter(new FileTypeFilter(".ss.xml", "Startup settings"));
        break;
      case game:
        ret.addChoosableFileFilter(new FileTypeFilter(".sm.xml", "Saved simulation"));
        break;
      case traffic:
        ret.addChoosableFileFilter(new FileTypeFilter(".tr.xml", "Traffic files"));
        break;
      case types:
        ret.addChoosableFileFilter(new FileTypeFilter(".tp.xml", "Airplane types files"));
        break;
      case folder:
        ret.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }
    ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
    ret.addChoosableFileFilter(new FileTypeFilter("", "All files"));

    if (defaultFile == null)
      ret.setCurrentDirectory(new File(System.getProperty("user.home")));
    else {
      Path p = Paths.get(defaultFile);
      ret.setSelectedFile(p.toFile());
      p = p.getParent();
      ret.setCurrentDirectory(p.toFile());
    }

    return ret;
  }

  public static void show(JPanel pnl, String title){
    JFrame frm = new JFrame();
    frm.getContentPane().add(pnl);
    frm.pack();
    frm.setVisible(true);
    frm.setTitle(title);
  }

  public static void showDialog(JPanel pnl, String title, JFrame owner){
    JDialog dlg = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
    dlg.getContentPane().add(pnl);
    dlg.pack();
    dlg.setLocationRelativeTo(null);
    dlg.setTitle(title);
    dlg.setVisible(true);
  }

  public static JButton createButton(String title, ActionListener action){
    JButton ret = new JButton(title);
    ret.addActionListener(action);
    return ret;
  }
}


class FileTypeFilter extends FileFilter {
  private String extension;
  private String description;

  public FileTypeFilter(String extension, String description) {
    this.extension = extension;
    this.description = description;
  }

  public boolean accept(File file) {
    if (file.isDirectory()) {
      return true;
    }
    return file.getName().endsWith(extension);
  }

  public String getDescription() {
    return description + String.format(" (*%s)", extension);
  }
}
