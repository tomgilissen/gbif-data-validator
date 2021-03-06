package org.gbif.validation.api;

import org.gbif.dwc.terms.Term;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Interface representing a source of records (file, map ...).
 */
public interface RecordSource extends Closeable {

  /**
   * Returns the headers of the source.
   * @return headers or null
   */
  @Nullable
  Term[] getHeaders();

  @Nullable
  List<String> read() throws IOException;

}
