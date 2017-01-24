package org.gbif.validation.processor;

import org.gbif.validation.api.DataFile;
import org.gbif.validation.util.FileBashUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Class responsible to handle the logic related to the file splitting strategy.
 */
class DataFileSplitter {

  /**
   * Split the provided {@link DataFile} into multiple {@link DataFile} if required.
   * If no split is required the returning list will contain the provided {@link DataFile}.
   *
   * @param dataFile expected to have at least the followings: rowType, filePath, numOfLines, hasHeaders
   * @param fileSplitSize
   * @param baseDir       Base directory where to store the results. A folder like "Occurrence_split" will be created.
   *
   * @return
   *
   * @throws IOException
   */
  static List<DataFile> splitDataFile(DataFile dataFile, Integer fileSplitSize, Path baseDir) throws IOException {
    Objects.requireNonNull(dataFile.getRowType(), "DataFile rowType shall be provided");
    Objects.requireNonNull(dataFile.getFilePath(), "DataFile filePath shall be provided");

    List<DataFile> splitDataFiles = new ArrayList<>();
    if (dataFile.getNumOfLines() <= fileSplitSize) {
      splitDataFiles.add(dataFile);
    } else {
      String splitFolder = baseDir.resolve(dataFile.getRowType().simpleName() + "_split").toAbsolutePath().toString();
      String[] splits = FileBashUtilities.splitFile(dataFile.getFilePath().toString(), fileSplitSize, splitFolder);

      boolean inputHasHeaders = dataFile.isHasHeaders();
      IntStream.range(0, splits.length)
              .forEach(idx -> splitDataFiles.add(newSplitDataFile(dataFile,
                      splitFolder,
                      splits[idx],
                      inputHasHeaders && (idx == 0),
                      Optional.of((idx * fileSplitSize) + (inputHasHeaders ? 1 : 0)))));
    }
    return splitDataFiles;
  }

  /**
   * Get a new {@link DataFile} instance representing a split of the provided {@link DataFile}.
   *
   * @param dataFile
   * @param baseDir
   * @param fileName
   * @param withHeader
   * @param offset
   *
   * @return new {@link DataFile} representing a portion of the provided dataFile.
   */
  private static DataFile newSplitDataFile(DataFile dataFile, String baseDir, String fileName,
                                           boolean withHeader, Optional<Integer> offset) {
    //Creates the file to be used
    File splitFile = new File(baseDir, fileName);
    splitFile.deleteOnExit();

    //use dataFile as parent dataFile
    DataFile dataInputSplitFile = DataFile.copyFromParent(dataFile);
    dataInputSplitFile.setHasHeaders(withHeader);
    dataInputSplitFile.setFilePath(Paths.get(splitFile.getAbsolutePath()));
    dataInputSplitFile.setFileLineOffset(offset);
    return dataInputSplitFile;
  }
}