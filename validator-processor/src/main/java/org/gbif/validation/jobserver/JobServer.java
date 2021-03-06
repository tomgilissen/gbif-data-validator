package org.gbif.validation.jobserver;

import org.gbif.validation.api.DataFile;
import org.gbif.validation.api.model.JobDataOutput;
import org.gbif.validation.api.model.JobStatusResponse;
import org.gbif.validation.api.model.JobStatusResponse.JobStatus;
import org.gbif.validation.api.result.ValidationDataOutput;
import org.gbif.validation.jobserver.messages.DataJob;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import akka.actor.Props;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.validation.jobserver.util.ActorSelectionUtil.getJobServerChildren;
import static org.gbif.validation.jobserver.util.ActorSelectionUtil.getRunningActor;

/**
 * Manages the job submission and status retrieval.
 * @param <T> type of elements that this job servers handles
 */
public class JobServer<T> {

  private static final Logger LOG = LoggerFactory.getLogger(JobServer.class);

  private final ActorSystem system;
  private final AtomicLong jobIdSeed;
  private final JobStorage jobStorage;
  private final ActorRef jobMonitor;

  // only used to keep track of the DataJob while the job is running
  private final Cache<Long, DataJob<DataFile>> jobIdToDataJob = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterAccess(1, TimeUnit.DAYS)
          .build();

  /**
   * Creates a JobServer instance that will use the jobStore instance to store and retrieve job's data.
   * @param jobStorage
   * @param propsSupplier
   * @param completionCallback callback function to call on completion (doesn't imply success, only completion)
   */
  public JobServer(JobStorage jobStorage, Supplier<Props> propsSupplier, Consumer<UUID> completionCallback) {
    system = ActorSystem.create("JobServerSystem");
    jobIdSeed = new AtomicLong(new Date().getTime());
    this.jobStorage = jobStorage;
    jobMonitor = system.actorOf(Props.create(JobMonitor.class, propsSupplier, jobStorage, completionCallback), "JobMonitor");
    LOG.info("New jobServer instance created");
  }

  /**
   * Process the submission of a data validation job.
   * If the job is accepted the response contains the new jobId ACCEPTED as the job status.
   */
  public JobStatusResponse<?> submit(DataFile dataFile) {
    long newJobId = jobIdSeed.getAndIncrement();
    long startTimestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();

    LOG.info("Running actors:" + getJobServerChildren(system).stream().collect(Collectors.joining(",")));
    DataJob<DataFile> dataJob = new DataJob<>(newJobId, startTimestamp, dataFile);
    jobIdToDataJob.put(newJobId, dataJob);
    jobMonitor.tell(dataJob, jobMonitor);
    return JobStatusResponse.ofAccepted(newJobId, startTimestamp, dataFile.getKey());
  }

  /**
   * Gets the status of a job. If the job is not found ValidationJobResponse.NOT_FOUND_RESPONSE is returned.
   */
  public JobStatusResponse<?> status(long jobId) {
    //the job storage is checked first
    Optional<JobStatusResponse<?>> result = Optional.empty();
    try {
      result = jobStorage.getStatus(jobId);
    } catch (IOException ioEx) {
      // log and continue as if it was not found
      LOG.warn("Exception while getting job from the jobStorage " + jobId, ioEx);
    }

    if (result.isPresent()) {
      return result.get();
    }
    //if the job data is not in the storage it might be still running
    return getJobStatus(jobId);
  }

  public Optional<JobDataOutput> getDataOutput(long jobId, ValidationDataOutput.Type type) {
    Objects.requireNonNull(type, "JobDataOutput.Type shall be provided");
    Optional<JobDataOutput> outputData = Optional.empty();
    try {
      outputData = jobStorage.getDataOutput(jobId, type);
    } catch (IOException ioEx) {
      // log and continue as if it was not found
      LOG.warn("Exception while getting job output data from the jobStorage " + jobId, ioEx);
    }
    return outputData;
  }

  /**
   * Tries to kill a jobId.
   */
  public JobStatusResponse<?> kill(long jobId) {
    Optional<ActorRef> actorOpt = getRunningActor(jobId, system);
    if (actorOpt.isPresent()) {
      Optional<DataJob<DataFile>> possibleDataJob = Optional.ofNullable(jobIdToDataJob.getIfPresent(jobId));

      JobStatusResponse<?> response = JobStatusResponse.ofKilled(jobId,
              possibleDataJob.map(DataJob::getStartTimeStamp).orElse(null),
              possibleDataJob.map(DataJob::getJobData).map(DataFile::getKey).orElse(null));
      ActorRef actorRef = actorOpt.get();
      actorRef.tell(Kill.getInstance(), jobMonitor);
      system.stop(actorRef);
      jobStorage.put(response);   //stores a job result
      return response;
    }
    return JobStatusResponse.ofNotFound(jobId);
  }

  /**
   * Only used when a job can not be started in an asynchronous context.
   * @param jobStatus
   */
  public void pushFailedJobStatus(JobStatusResponse<?> jobStatus) {
    Preconditions.checkArgument(JobStatus.FAILED == jobStatus.getStatus());
    jobStorage.put(jobStatus);
  }

  /**
   * Stops the jobs server and all the underlying actors.
   */
  public void stop() {
    if (!system.isTerminated()) {
      system.shutdown();
    }
  }

  /**
   * Tries to gets the status from the running instances.
   */
  private JobStatusResponse<?> getJobStatus(long jobId) {

    try {
      //there's a running actor with that jobId name?
      if (getRunningActor(jobId, system).isPresent()) {
        Optional<DataJob<DataFile>> possibleDataJob = Optional.ofNullable(jobIdToDataJob.getIfPresent(jobId));
        return JobStatusResponse.ofRunning(jobId,
                possibleDataJob.map(DataJob::getStartTimeStamp).orElse(null),
                possibleDataJob.map(DataJob::getJobData).map(DataFile::getKey).orElse(null));
      }
    } catch (Exception ex) {
      LOG.error("Error  retrieving JobId {} data", jobId, ex);
    }
    return JobStatusResponse.ofNotFound(jobId);
  }

}
