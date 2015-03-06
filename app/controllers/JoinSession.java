package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import views.html.joinSession;
import actor.JoinSessionWS;
import actor.JoinSessionWS.PropCreater;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.google.inject.name.Named;

public class JoinSession extends Controller {

	private final ActorRef sessionManager;

	public JoinSession(@Named("SessionManager") ActorRef sessionManager) {
		this.sessionManager = sessionManager;
	}

	
	@SubjectPresent
	public static Result joinSession(int sessionId) {
		return ok(joinSession.render(sessionId));
	}
	
	public WebSocket<String> joinSessionWS(int sessionId) {
		Logger.info("sectors with sessionId " + sessionId);
		String userName = session("userName");
		if (userName != null) {
			play.Logger.info("username " + userName);
			PropCreater propCreater = new JoinSessionWS.PropCreater(
					sessionId, userName, sessionManager);
			return WebSocket.withActor(propCreater::props);
		} else {
			return WebSocketUtils.notAuthorizedWebSocket();
		}
	}
}
