package org.gbif.validation.ws;

import org.gbif.occurrence.validation.api.DataFile;
import org.gbif.occurrence.validation.api.model.ValidationResult;
import org.gbif.occurrence.validation.tabular.OccurrenceDataFileProcessorFactory;
import org.gbif.occurrence.validation.util.FileBashUtilities;
import org.gbif.occurrence.validation.api.model.DataFileDescriptor;
import org.gbif.utils.HttpUtil;
import org.gbif.ws.server.provider.DataFileDescriptorProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/validate")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ValidationResource {

  private final ValidationConfiguration configuration;

  private final OccurrenceDataFileProcessorFactory dataFileProcessorFactory;

  private static final Logger LOG = LoggerFactory.getLogger(ValidationResource.class);

  private static final String FILE_PARAM = "file";

  private HttpUtil httpUtil;

  @Inject
  public ValidationResource(ValidationConfiguration configuration, HttpUtil httpUtil,
                            OccurrenceDataFileProcessorFactory dataFileProcessorFactory) {
    this.configuration = configuration;
    this.dataFileProcessorFactory = dataFileProcessorFactory;
    this.httpUtil = httpUtil;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/file")
  public ValidationResult validateFile(@FormDataParam(FILE_PARAM) final InputStream stream,
                                       @FormDataParam(FILE_PARAM) FormDataContentDisposition header,
                                       FormDataMultiPart formDataMultiPart) {
      DataFileDescriptor dataFileDescriptor = DataFileDescriptorProvider.getValue(formDataMultiPart, header);
      java.nio.file.Path dataFilePath = downloadFile(dataFileDescriptor, stream);
      ValidationResult result = processFile(dataFilePath, dataFileDescriptor);
      //deletes the downloaded file
      dataFilePath.toFile().delete();
      return result;
  }



  private  java.nio.file.Path downloadFile(DataFileDescriptor descriptor, final InputStream stream) {
    if(descriptor.getFile() != null) {
      try {
        URL fileUrl = new URL(descriptor.getFile());
        return (fileUrl.getProtocol().startsWith("http"))?  downloadHttpFile(fileUrl): copyDataFile(stream,descriptor);
      } catch(URISyntaxException | IOException  ex){
        throw new WebApplicationException(ex, Response.SC_BAD_REQUEST);
      }
    }
    throw new WebApplicationException(Response.SC_BAD_REQUEST);
  }

  /**
   * Downloads a file from a HTTP(s) endpoint.
   */
  private java.nio.file.Path downloadHttpFile(URL fileUrl) throws IOException,URISyntaxException {
    java.nio.file.Path destinyFilePath = downloadFilePath(Paths.get(fileUrl.toURI()).getFileName().toString());
    if (httpUtil.download(fileUrl,destinyFilePath.toFile()).getStatusCode() != Response.SC_OK) {
      throw new WebApplicationException(Response.SC_BAD_REQUEST);
    }
    return destinyFilePath;
  }

  /**
   * Creates a new random path to be used when copying files.
   */
  private java.nio.file.Path downloadFilePath(String fileName) {
    return Paths.get(configuration.getWorkingDir(), UUID.randomUUID().toString(),fileName);
  }


  /**
   * Copies the input stream into a temporary directory.
   */
  private java.nio.file.Path copyDataFile(final InputStream stream,
                                          DataFileDescriptor descriptor) throws IOException {
    java.nio.file.Path destinyFilePath = downloadFilePath(descriptor.getFile());
    LOG.info("Uploading data file into {}", descriptor.toString());
    Files.createDirectory(destinyFilePath.getParent());
    Files.copy(stream, destinyFilePath, StandardCopyOption.REPLACE_EXISTING);
    return destinyFilePath;
  }

  /**
   * Applies the validation routines to the input file.
   */
  private ValidationResult processFile(java.nio.file.Path dataFilePath, DataFileDescriptor dataFileDescriptor)  {
    try {
      DataFile dataFile = new DataFile();
      dataFile.setFileName(dataFilePath.toFile().getAbsolutePath());
      dataFile.setNumOfLines(FileBashUtilities.countLines(dataFilePath.toFile().getAbsolutePath()));
      dataFile.setDelimiterChar(dataFileDescriptor.getFieldsTerminatedBy());
      dataFile.setHasHeaders(dataFileDescriptor.isHasHeaders());
      dataFile.loadHeaders();
      return dataFileProcessorFactory.create(dataFile).process(dataFile);
    } catch (IOException ex) {
      //deletes the file in case of error
      dataFilePath.toFile().delete();
      throw new WebApplicationException(Response.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
