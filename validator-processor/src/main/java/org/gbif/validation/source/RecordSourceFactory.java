package org.gbif.validation.source;

import org.gbif.validation.api.DataFile;
import org.gbif.validation.api.RecordSource;
import org.gbif.validation.api.model.FileFormat;
import org.gbif.validation.util.FileBashUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static org.gbif.utils.file.tabular.TabularFiles.newTabularFileReader;

/**
 * Creates instances of RecordSource class.
 *
 * RecordSourceFactory contract is to provide a RecordSource from different source therefore
 * no validation on the structure of the source will be performed.
 */
public class RecordSourceFactory {

  /**
   * Private constructor.
   */
  private RecordSourceFactory() {
    //empty method
  }

  /**
   * Creates instances of RecordSource from character delimited files.
   */
  public static RecordSource fromDelimited(File sourceFile, char delimiterChar, boolean headerIncluded)
          throws IOException {
    return new TabularFileReader(sourceFile.toPath(), newTabularFileReader(new FileInputStream(sourceFile),
                                                                           delimiterChar, headerIncluded));
  }

  /**
   * Creates instances of RecordSource from a folder containing an extracted DarwinCore archive.
   */
  public static RecordSource fromDwcA(File sourceFolder) throws IOException {
    return new DwcReader(sourceFolder);
  }

  /**
   * Build a new RecordSource matching the {@link DataFile} file format.
   * @param dataFile
   * @return
   * @throws IOException
   */
  public static Optional<RecordSource> fromDataFile(DataFile dataFile) throws IOException {
    Objects.requireNonNull(dataFile.getFileFormat(), "fileFormat shall be provided");

    switch (dataFile.getFileFormat()) {
      case TABULAR:
        return
                Optional.of(fromDelimited(dataFile.getFilePath().toFile(), dataFile.getDelimiterChar(),
                        dataFile.isHasHeaders()));
      case SPREADSHEET:
        //we can only provide RecordSource if the spreadsheet was converted to CSV
        if(dataFile.isFileConverted()) {
          Optional.of(fromDelimited(dataFile.getFilePath().toFile(), dataFile.getDelimiterChar(),
                  dataFile.isHasHeaders()));
        }
        break;
      case DWCA:
        return Optional.of(fromDwcA(dataFile.getFilePath().toFile()));
    }
    return Optional.empty();
  }

  /**
   * Prepare the source for reading.
   * This step includes reading the headers from the file.
   * @param dataFile
   * @return
   * @throws IOException
   */
  public static DataFile prepareSource(DataFile dataFile) throws IOException {
    Objects.requireNonNull(dataFile.getFilePath(), "filePath shall be provided");
    Objects.requireNonNull(dataFile.getFileFormat(), "fileFormat shall be provided");

    try (RecordSource rs = fromDataFile(dataFile).orElse(null)) {
      if (rs != null) {
        dataFile.setNumOfLines(FileBashUtilities.countLines(rs.getFileSource().toAbsolutePath().toString()));
        dataFile.setColumns(rs.getHeaders());

        if (FileFormat.DWCA == dataFile.getFileFormat()) {
          dataFile.setRowType(((DwcReader) rs).getRowType());
          //change the current file path to point to the core
          dataFile.setFilePath(rs.getFileSource());
          dataFile.setSourceFileComponentName(rs.getFileSource().getFileName().toString());
        }
        else {
          dataFile.setSourceFileComponentName(dataFile.getSourceFileName());
        }
      }
    }
    return dataFile;
  }

}
