package org.gbif.validation.source;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.utils.file.FileUtils;
import org.gbif.validation.api.DataFile;
import org.gbif.validation.api.DwcDataFile;
import org.gbif.validation.api.RecordSource;
import org.gbif.validation.api.RowTypeKey;
import org.gbif.validation.api.TabularDataFile;
import org.gbif.validation.api.vocabulary.FileFormat;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link }RecordSourceFactory}
 */
public class RecordSourceFactoryTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private static final String TEST_TSV_FILE_LOCATION = "validator_test_file_all_issues.tsv";
  private static final String TEST_DWC_FILE_LOCATION = "dwca/dwca-taxon";

  @Test
  public void testPrepareDwcA() throws IOException, UnsupportedDataFileException {

    File testFile = FileUtils.getClasspathFile(TEST_DWC_FILE_LOCATION);
    DataFile dataFile = new DataFile(UUID.randomUUID(), testFile.toPath(), "dwca-taxon", FileFormat.DWCA, "", "");

    DwcDataFile preparedDwcDataFile = DataFileFactory.prepareDataFile(dataFile, folder.newFolder().toPath());

    TabularDataFile taxonDataFile = preparedDwcDataFile.getByRowTypeKey(RowTypeKey.forCore(DwcTerm.Taxon));
    try (RecordSource rs = RecordSourceFactory.fromTabularDataFile(taxonDataFile)) {
      assertEquals("1559060", rs.read().get(0));
    }
  }

  @Test
  public void testPrepareTabular() throws IOException, UnsupportedDataFileException {

    File testFile = FileUtils.getClasspathFile(TEST_TSV_FILE_LOCATION);
    DataFile dataFile = new DataFile(UUID.randomUUID(), testFile.toPath(), "validator_test_file_all_issues.tsv",
            FileFormat.TABULAR, "", "");

    DwcDataFile preparedTabularDataFile = DataFileFactory.prepareDataFile(dataFile, folder.newFolder().toPath());

    assertEquals(1, preparedTabularDataFile.getTabularDataFiles().size());
    TabularDataFile tabularDataFile = preparedTabularDataFile.getTabularDataFiles().get(0);
    assertEquals('\t', tabularDataFile.getDelimiterChar().charValue());
    assertEquals(DwcTerm.Occurrence, tabularDataFile.getRowTypeKey().getRowType());

    try(RecordSource recordSource = RecordSourceFactory.fromTabularDataFile(tabularDataFile)) {
      assertEquals("http://coldb.mnhn.fr/catalognumber/mnhn/p/p00501568", recordSource.read().get(0));
    }
  }

}
