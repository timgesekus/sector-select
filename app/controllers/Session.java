package controllers;

import com.google.inject.name.Named;

import play.Logger;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import actor.SessionWebsocketHandler;
import actor.SessionWebsocketHandler.PropCreater;
import actor.SessionActor;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectNotPresent;

public class Session extends Controller {

	private final ActorRef sessionManager;

	public Session(@Named("SessionManager") ActorRef sessionManager) {
		this.sessionManager = sessionManager;
	}

	public WebSocket<String> sectors(int sessionId) {
		Logger.info("sectors with sessionId " + sessionId);
		String userName = session("userName");
		if (userName != null) {
			play.Logger.info("username " + userName);
			PropCreater propCreater = new SessionWebsocketHandler.PropCreater(
					sessionId, userName, sessionManager);
			return WebSocket.withActor(propCreater::props);
		} else {
			return WebSocketUtils.notAuthorizedWebSocket();
		}
	}
}
