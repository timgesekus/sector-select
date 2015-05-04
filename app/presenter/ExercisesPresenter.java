package presenter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.googlecode.protobuf.format.JsonFormat;

import services.SessionService;
import session.command.SessionComand.RestoreSessions;
import session.event.SessionEvent.RestoreSessionsCompleted;
import session.event.SessionEvent.SessionStarted;
import session.event.SessionEvent.SessionStopped;
import eventBus.EventBus;
import exercises.messages.Exercises;
import exercises.messages.Exercises.ExercisesResponse;
import exercises.messages.Exercises.RequestExercises;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class ExercisesPresenter extends AbstractActor
{

  public static Props props(
    final String userName,
    final EventBus eventBus,
    final ActorRef exerciseService,
    final ActorRef sessionService,
    final ActorRef out)
  {
    return Props.create(new Creator<ExercisesPresenter>()
    {
      private static final long serialVersionUID = 1398731621836926808L;

      @Override
      public ExercisesPresenter create() throws Exception
      {
        return new ExercisesPresenter(
          userName,
          eventBus,
          exerciseService,
          sessionService,
          out);
      }
    });
  }

  private final String userName;
  private final EventBus eventBus;
  private final ActorRef exerciseService;
  private final ActorRef sessionService;
  private final ActorRef out;
  Optional<ExercisesResponse> exercisesOption;
  private final Map<String, Set<String>> exerciseSessions;

  public ExercisesPresenter(
    String userName,
    EventBus eventBus,
    ActorRef exerciseService,
    ActorRef sessionService,
    ActorRef out)
  {
    this.userName = userName;
    this.eventBus = eventBus;
    this.exerciseService = exerciseService;
    this.sessionService = sessionService;
    this.out = out;

    exercisesOption = Optional.empty();
    exerciseSessions = new HashMap<>();
    configureMessageHandling();
    requestSessions();
    requestExercises();
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(ExercisesResponse.class, this::handleExercisesResponse)
      .match(SessionStarted.class, this::sessionStarted)
      .match(SessionStopped.class, this::sessionStopped)
      .match(RestoreSessionsCompleted.class, this::restoreSessionsCompleted)
      .build());
  }

  private void requestSessions()
  {
    RestoreSessions restoreSessions = RestoreSessions.newBuilder().build();
    sessionService.tell(restoreSessions, self());
  }

  private void requestExercises()
  {
    RequestExercises requestExercises = RequestExercises
      .newBuilder()
      .setUserName(userName)
      .build();
    exerciseService.tell(requestExercises, self());
  }

  private void handleExercisesResponse(ExercisesResponse exercises)
  {
    exercisesOption = Optional.of(exercises);
    sendViewModel();
  }

  private void sessionStarted(SessionStarted sessionStarted)
  {

  }

  private void sessionStopped(SessionStopped sessionStopped)
  {

  }

  private void restoreSessionsCompleted(
    RestoreSessionsCompleted restoreSessionsCompleted)
  {

  }

  private void sendViewModel()
  {
    if (exercisesOption.isPresent())
    {
      ExercisesResponse exercises = exercisesOption.get();
      String jsonMessage = JsonFormat.printToString(exercises);
      out.tell(jsonMessage, self());
    }
  }
}
