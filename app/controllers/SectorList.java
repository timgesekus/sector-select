package controllers;

import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import actor.SectorListActor;
import actor.SectorListActor.PropCreater;
import actor.SessionActor;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectNotPresent;

public class SectorList extends Controller {
	private static ActorRef sessionActor = Akka.system().actorOf(
			SessionActor.props());

	@SubjectNotPresent(content = "String")
	public static WebSocket<String> sectors() {
		String userName = session("userName");
		if (userName != null) {
			play.Logger.info("username " + userName);
			PropCreater propCreater = new SectorListActor.PropCreater(
					sessionActor, userName);
			return WebSocket.withActor(propCreater::props);
		} else {
			return WebSocketUtils.notAuthorizedWebSocket();
		}
	}

	
}
