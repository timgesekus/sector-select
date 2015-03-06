package actor;

import java.io.IOException;

import play.Logger;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;
import actor.SessionManager.GetSessionActor;
import actor.SessionManager.GetSessionActorReply;
import actor.messages.Sectors;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JoinSessionWS extends AbstractActor {

	private final ObjectMapper objectMapper;

	public static Props props(ActorRef out, ActorRef sessionManager) {
		return Props.create(JoinSessionWS.class, out, sessionManager);
	}

	private final ActorRef out;
	private final ActorRef sessionManager;
	private final String userName;
	private final int sessionId;
	private final PartialFunction<Object, BoxedUnit> waitForSessionActor;
	private final PartialFunction<Object, BoxedUnit> normalState;
	private ActorRef sessionActor;

	public JoinSessionWS(
	  ActorRef out,
	  String userName,
	  int sessionId,
	  ActorRef sessionManager) throws JsonProcessingException {
		waitForSessionActor = ReceiveBuilder
		  .match(GetSessionActorReply.class, this::userSessionActor)
		  .match(Sectors.class, this::receiveSectors)
		  .build();
		normalState = ReceiveBuilder
		  .match(String.class, this::receiveJsonFromSocket)
		  .match(Sectors.class, this::receiveSectors)
		  .build();

		this.out = out;
		this.userName = userName;
		this.sessionManager = sessionManager;
		this.sessionId = sessionId;
		objectMapper = new ObjectMapper();

		startWaitingForSessionActor();
		requestSessionActor();

	}

	@Override
	public void postStop() throws Exception {
		Logger.info("Websocket for {} with id {} closed", self(), userName);
		if (sessionActor != null) {
			Unsubscribe unsubscribe = new Unsubscribe();
			sessionActor.tell(unsubscribe, self());
		}
	}

	private void startWaitingForSessionActor() {
		receive(waitForSessionActor);
	}

	private void requestSessionActor() {
		this.sessionManager.tell(new GetSessionActor(this.sessionId), self());
	}

	private void subscribeToSession() {
		sessionActor.tell(new Subscribe(userName), self());
	}

	public void userSessionActor(GetSessionActorReply getSessionActorReply) {
		setSessionActor(getSessionActorReply);
		startNormalOperation();
		subscribeToSession();
	}

	private void setSessionActor(GetSessionActorReply getSessionActorReply) {
		this.sessionActor = getSessionActorReply.sessionActor;
	}

	private void startNormalOperation() {
		getContext().become(normalState);
	}

	public void receiveJsonFromSocket(String json)
	  throws JsonParseException,
	  JsonMappingException,
	  IOException {
		Logger.info("Received a message:" + json);
		Event readValue;
		readValue = objectMapper.readValue(json, Event.class);
		sessionActor.tell(readValue, self());
	}

	public void receiveSectors(Sectors sectors) throws JsonProcessingException {
		Logger.info("Received a sectors message " + sectors.sectors.size());
		String sectorsAsJson = objectMapper.writeValueAsString(sectors);
		out.tell(sectorsAsJson, self());
	}

	public static class PropCreater {
		private final int sessionId;
		private final String userName;
		private final ActorRef sessionManager;

		public PropCreater(int sessionId, String userName, ActorRef sessionManagers) {
			this.sessionId = sessionId;
			this.userName = userName;
			sessionManager = sessionManagers;
		}

		public Props props(ActorRef out) {
			return Props.create(
			  JoinSessionWS.class,
			  out,
			  userName,
			  sessionId,
			  sessionManager);
		}

	}

	public static class Event {
		public String topic;
		public String sector;

		public Event() {
			topic = "event";
		}
	}

}
