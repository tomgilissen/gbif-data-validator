package org.gbif.validation.jobserver.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * Utility class to select/find actor in the System of actors.
 */
public class ActorSelectionUtil {

  static final String JOB_MONITOR_ACTOR_NAME = "JobMonitor";

  //Path used to find actors in the system of actors
  private static final String ACTOR_SELECTION_PATH = "/user/JobMonitor/";

  //waiting time to identify actors
  private static final Timeout WAIT_TO = new Timeout(5, TimeUnit.SECONDS);

  private static final Logger LOG = LoggerFactory.getLogger(ActorSelectionUtil.class);

  /**
   * Private constructor.
   */
  private ActorSelectionUtil() {
    //empty constructor
  }

  /**
   * Tries to get the reference of a running Actor in the system.
   */
  public static Optional<ActorRef> getRunningActor(long jobId, final ActorSystem system) {
    try {
      ActorSelection sel = system.actorSelection(ACTOR_SELECTION_PATH + jobId);
      Future<ActorRef> fut = sel.resolveOne(WAIT_TO);
      ActorRef ref = Await.result(fut, WAIT_TO.duration());
      return Optional.ofNullable(ref);
    } catch (Exception ex) {
      LOG.warn("Actor not found {}", jobId);
      return Optional.empty();
    }
  }

  /**
   *
   * @param system
   * @return
   */
  public static List<String> getJobServerChildren(final ActorSystem system) {
    try {
      return JavaConversions.seqAsJavaList(system.child(JOB_MONITOR_ACTOR_NAME).elements().toSeq());
    } catch (Exception ex) {
      LOG.warn("getRunningActor failed", ex);
      return Collections.emptyList();
    }
  }

}
