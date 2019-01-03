package eng.jAtcSim.app.extenders.swingFactory;

import javax.swing.filechooser.FileFilter;
import java.io.File;

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
