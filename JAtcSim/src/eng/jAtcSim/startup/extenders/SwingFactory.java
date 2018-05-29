package eng.jAtcSim.startup.extenders;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SwingFactory {

  public static JScrollBar createHorizontalBar(int minimum, int maximum, int value){
    JScrollBar ret = new JScrollBar(JScrollBar.HORIZONTAL);
    ret.setMinimum(minimum);
    ret.setMaximum(maximum);
    ret.setValue(value);
    return ret;
  }

  public static JFileChooser createFileDialog() {
    JFileChooser ret = new JFileChooser();

    ret.setAcceptAllFileFilterUsed(false);
    ret.addChoosableFileFilter(new FileTypeFilter(".ss.xml", "Startup settings"));
    ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
    ret.addChoosableFileFilter(new FileTypeFilter("", "All files"));

    ret.setCurrentDirectory(new File(System.getProperty("user.home")));
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
