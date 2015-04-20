package controllers;

import joinSessionView.JoinSessionPresenter;
import joinSessionView.JoinSessionWS;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.concurrent.Future;
import utils.WebSocketUtils;
import viewmodels.exerciseselect.StartExercise;
import akka.actor.ActorRef;
import akka.actor.Props;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import static akka.pattern.Patterns.ask;

import com.google.inject.name.Named;

import eventBus.EventBus;

public class JoinSession extends Controller
{

  private final ActorRef sessionManager;
  private EventBus eventBus;

  public JoinSession(
    @Named("SessionManager") ActorRef sessionManager,
    EventBus eventBus)
  {
    this.sessionManager = sessionManager;
    this.eventBus = eventBus;
  }

  @SubjectPresent
  public Promise<Result> createSession(int exerciseId)
  {

    String userName = session("userName");
    StartExercise startExercise = new StartExercise(userName, exerciseId);

    // return ok(views.html.joinSession.render(sessionId));
    Future<Object> startExerciseAnswer = ask(
      sessionManager,
      startExercise,
      1000);
    return Promise.wrap(startExerciseAnswer).map(
      this::handleStartExerciseAnswer);
  }

  @SubjectPresent
  public static Result joinSession(int sessionId)
  {
    return ok(views.html.joinSession.render(sessionId));
  }

  public WebSocket<String> joinSessionWS(int sessionId)
  {
    Logger.info("sectors with sessionId " + sessionId);
    String userName = session("userName");
    if (userName != null)
    {
      play.Logger.info("username " + userName);
      Props joinSessionPresenterProps = JoinSessionPresenter.props(
        userName,
        sessionId,
        sessionManager);
      ActorRef joinSessionPresenter = Akka.system().actorOf(
        joinSessionPresenterProps);
      return WebSocket.withActor(out -> JoinSessionWS.props(
        out,
        userName,
        joinSessionPresenter,
        eventBus,
        sessionId));
    } else
    {
      return WebSocketUtils.notAuthorizedWebSocket();
    }
  }

  private Result handleStartExerciseAnswer(Object startExerciseAnswer)
  {
    if (startExerciseAnswer instanceof viewmodels.exerciseselect.JoinSession)
    {
      viewmodels.exerciseselect.JoinSession joinSession = (viewmodels.exerciseselect.JoinSession) startExerciseAnswer;
      return joinSession(joinSession.sessionid);
    }
    return internalServerError("Failed to create session: "
        + startExerciseAnswer.toString());
  }
}
