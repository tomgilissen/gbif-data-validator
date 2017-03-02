package org.gbif.validation.source;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests related to {@link DwcaMetadataExtractor}.
 */
public class DwcaMetadataExtractorTest {

  private static final String TEST_FILE_TAXON_LOCATION = "dwca/dwca-taxon";
  private static final String TEST_FILE_ID_WITH_TERM_LOCATION = "dwca/dwca-id-with-term";

  @Test
  public void testTaxon() {
    testDwcaMetadataExtractor(TEST_FILE_TAXON_LOCATION, DwcTerm.Taxon, 12, "1559060", 12);
  }

  @Test
  public void testIdAsTerm() {
    testDwcaMetadataExtractor(TEST_FILE_ID_WITH_TERM_LOCATION, DwcTerm.Occurrence, 5,
            "10d1bde8870c424ba9065de6964a269d", 11);
  }

  private void testDwcaMetadataExtractor(String testFile, Term expectedRowType, int expectedNumberOfHeaders,
                              String expectedLine1Column1Value, int expectedLine1Length) {
    File testFolder = FileUtils.getClasspathFile(testFile);

    try {
      DwcaMetadataExtractor dwcReader = new DwcaMetadataExtractor(testFolder);
      assertEquals(expectedRowType, dwcReader.getRowType());
      assertEquals(expectedNumberOfHeaders, dwcReader.getHeaders().length);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

}
