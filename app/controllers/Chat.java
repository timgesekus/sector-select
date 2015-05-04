package controllers;

import presenter.ChatPresenter;
import play.Logger;
import play.mvc.Controller;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eventBus.EventBus;

public class Chat extends Controller
{

  private final ActorRef sessionManager;
  private EventBus eventBus;

  @Inject
  public Chat(
    @Named("SessionService") ActorRef sessionManager,
    EventBus eventBus)
  {
    this.sessionManager = sessionManager;
    this.eventBus = eventBus;
  }

  @SubjectPresent
  public WebSocket<String> chatWS(String chatId)
  {
    Logger.info("Request for chat  with chatId " + chatId);
    String userName = session("userName");
    if (userName != null)
    {
      play.Logger.info("username " + userName);
      return WebSocket.withActor(out -> ChatPresenter.props(
        userName,
        chatId,
        eventBus,
        out));
    } else
    {
      return WebSocketUtils.notAuthorizedWebSocket();
    }
  }
}
