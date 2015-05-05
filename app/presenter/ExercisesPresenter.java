package presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import play.Logger;
import chat.command.ChatCommand.UserLeaveChat;

import com.googlecode.protobuf.format.JsonFormat;

import services.SessionService;
import session.command.SessionComand.RestoreSessions;
import session.event.SessionEvent.RestoreSessionsCompleted;
import session.event.SessionEvent.SessionStarted;
import session.event.SessionEvent.SessionStopped;
import eventBus.EventBus;
import eventBus.Topic;
import exercises.messages.Exercises;
import exercises.messages.Exercises.ExercisesResponse.Builder;
import exercises.messages.Exercises.ExercisesResponse.Exercise;
import exercises.messages.Exercises.ExercisesResponse;
import exercises.messages.Exercises.ExercisesResponse.Session;
import exercises.messages.Exercises.RequestExercises;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class ExercisesPresenter extends AbstractActor
{

  public static Props props(final String userName, final EventBus eventBus, final ActorRef exerciseService,
      final ActorRef sessionService, final ActorRef out)
  {
    return Props.create(new Creator< ExercisesPresenter >()
    {
      private static final long serialVersionUID = 1398731621836926808L;


      @Override
      public ExercisesPresenter create() throws Exception
      {
        Logger.info("Exercise Presenter props called");
        return new ExercisesPresenter(userName, eventBus, exerciseService, sessionService, out);
      }
    });
  }


  private final String userName;
  private final EventBus eventBus;
  private final ActorRef exerciseService;
  private final ActorRef sessionService;
  private final ActorRef out;
  Optional< ExercisesResponse > exercisesOption;
  private final Map< String, Set< String >> exerciseSessions;
  private boolean exercisesReceived;
  private boolean isSessionsRestored;
  private boolean isInitComplete;
  final Logger.ALogger logger = Logger.of(this.getClass());


  public ExercisesPresenter(String userName, EventBus eventBus, ActorRef exerciseService,
      ActorRef sessionService, ActorRef out)
  {
    this.userName = userName;
    this.eventBus = eventBus;
    this.exerciseService = exerciseService;
    this.sessionService = sessionService;
    this.out = out;
    exercisesReceived = false;
    isSessionsRestored = false;
    isInitComplete = false;

    exercisesOption = Optional.empty();
    exerciseSessions = new HashMap<>();
    configureMessageHandling();
    requestSessions();
    requestExercises();

    eventBus.subscribe(self(), Topic.SESSION_SERVICE_EVENT);
    logger.info("Exercise Presenter created: {}", self());
  }


  private void configureMessageHandling()
  {
    receive(ReceiveBuilder.match(ExercisesResponse.class, this::handleExercisesResponse)
        .match(SessionStarted.class, this::sessionStarted)
        .match(SessionStopped.class, this::sessionStopped)
        .match(RestoreSessionsCompleted.class, this::restoreSessionsCompleted)
        .build());
  }


  private void requestSessions()
  {
    RestoreSessions restoreSessions = RestoreSessions.newBuilder()
        .build();
    sessionService.tell(restoreSessions, self());
  }


  private void requestExercises()
  {
    RequestExercises requestExercises = RequestExercises.newBuilder()
        .setUserName(userName)
        .build();
    exerciseService.tell(requestExercises, self());
  }


  private void handleExercisesResponse(ExercisesResponse exercises)
  {
    exercisesOption = Optional.of(exercises);
    exercisesReceived = true;
    checkIAndSetfInitializationIsComplete();
    sendViewModel();
  }


  private void checkIAndSetfInitializationIsComplete()
  {
    if (exercisesReceived && isSessionsRestored)
    {
      isInitComplete = true;
    }
  }


  private void sessionStarted(SessionStarted sessionStarted)
  {
    logger.info("Adding a session");
    String sessionId = sessionStarted.getSessionId();
    String exerciseId = sessionStarted.getExerciseId();

    Set< String > sessionIds = getSessionIds(exerciseId);
    sessionIds.add(sessionId);
    exerciseSessions.put(exerciseId, sessionIds);
    sendViewModel();
  }


  private void sessionStopped(SessionStopped sessionStopped)
  {
    logger.info("Removing a session");
    String sessionId = sessionStopped.getSessionId();
    Optional< String > exerciseIdOptional = exerciseSessions.keySet()
        .stream()
        .filter(exerciseId -> exerciseSessions.get(exerciseId)
            .contains(sessionId))
        .findFirst();
    if (exerciseIdOptional.isPresent())
    {
      String exerciseId = exerciseIdOptional.get();
      exerciseSessions.get(exerciseId)
          .remove(sessionId);
    }
    else
    {
      Logger.error("SessionId {} missing ", sessionId);
    }
    sendViewModel();
  }


  private void restoreSessionsCompleted(RestoreSessionsCompleted restoreSessionsCompleted)
  {
    isSessionsRestored = true;
    checkIAndSetfInitializationIsComplete();
    sendViewModel();
  }


  private void sendViewModel()
  {
    if (isInitComplete)
    {
      Builder exercisesResponseBuilder = ExercisesResponse.newBuilder();
      exercises.messages.Exercises.ExercisesResponse.Session.Builder sessionBuilder = ExercisesResponse.Session.newBuilder();
      ExercisesResponse exercises = exercisesOption.get();
      for (Exercise exercise: exercises.getExercisesList())
      {
        ExercisesResponse.Exercise.Builder exerciseBuilder = createExerciseBuilder();
        String exerciseId = exercise.getExerciseId();
        String exerciseName = exercise.getExerciseName();
        exerciseBuilder.setExerciseId(exerciseId);
        Set< String > sessionIds = getSessionIds(exerciseId);
        logger.info("Number of sessions: {}", sessionIds.size());
        List< Session > sessions = new ArrayList<>();
        for (String sessionId: sessionIds)
        {
          Session session = sessionBuilder.setSessionId(sessionId)
              .build();
          logger.info("SessionId: {}", sessionId);
          sessions.add(session);
        }
        Exercise newExercise = exerciseBuilder.setExerciseId(exerciseId)
            .setExerciseName(exerciseName)
            .addAllSessions(sessions)
            .build();

        exercisesResponseBuilder.addExercises(newExercise);
      }
      String jsonMessage = JsonFormat.printToString(exercisesResponseBuilder.build());
      logger.info("Sending message: {}", jsonMessage);
      out.tell(jsonMessage, self());
    }
  }


  private exercises.messages.Exercises.ExercisesResponse.Exercise.Builder createExerciseBuilder()
  {
    exercises.messages.Exercises.ExercisesResponse.Exercise.Builder exerciseBuilder = ExercisesResponse.Exercise.newBuilder();
    return exerciseBuilder;
  }


  private Set< String > getSessionIds(String exerciseId)
  {
    Set< String > sessionIds = exerciseSessions.getOrDefault(exerciseId, new HashSet< String >());
    return sessionIds;
  }


  @Override
  public void postStop() throws Exception
  {
    eventBus.unsubscribe(self());
  }
}
