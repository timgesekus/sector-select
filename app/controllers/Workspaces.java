package controllers;

import joinSessionView.ChatPresenter;
import joinSessionView.WorkspacesPresenter;
import play.Logger;
import play.mvc.Controller;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eventBus.EventBus;

public class Workspaces extends Controller
{

  private final ActorRef sessionManager;
  private EventBus eventBus;

  @Inject
  public Workspaces(
    @Named("SessionService") ActorRef sessionManager,
    EventBus eventBus)
  {
    this.sessionManager = sessionManager;
    this.eventBus = eventBus;
  }

  @SubjectPresent
  public WebSocket<String> workspacesWS(String workspacesId)
  {
    Logger.info("Request for workspaces  with workspacesId " + workspacesId);
    String userName = session("userName");
    if (userName != null)
    {
      play.Logger.info("username " + userName);
      return WebSocket.withActor(out -> WorkspacesPresenter.props(
        userName,
        workspacesId,
        eventBus,
        out));
    } else
    {
      return WebSocketUtils.notAuthorizedWebSocket();
    }
  }
}
