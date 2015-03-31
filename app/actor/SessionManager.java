package actor;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import viewmodels.exerciseselect.JoinSession;
import viewmodels.exerciseselect.StartExercise;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

/**
 * Manages simulation sessions.
 */
public class SessionManager extends AbstractActor {
	static int nextSessionId = 0;
	private final Map<Integer, ActorRef> sessions;

	/**
	 * Create props for {@link SessionManager}
	 * 
	 * @return props for creating a {@link SessionManager}
	 */
	public static Props props() {
		return Props.create(new Creator<SessionManager>() {
			private static final long serialVersionUID = 1L;

			@Override
			public SessionManager create() throws Exception {
				return new SessionManager();
			}
		});
	}

	public SessionManager() {
		Logger.info("Starting session Manager");
		sessions = new HashMap<>();
		configureMessageHandling();
	}

	private void configureMessageHandling() {
		receive(ReceiveBuilder
		  .match(StartExercise.class, this::startExercise)
		  .match(GetSessionActor.class, this::getSessionActor)
		  .build());
	}

	private void startExercise(StartExercise startExercise) {
		int newSessionId = getNextSessionId();
		int exerciseId = startExercise.exerciseId;
		String sessionOwner = startExercise.ownerName;
		ActorRef sessionActorRef = createNewSessionActor(
		  newSessionId,
		  exerciseId,
		  sessionOwner);
		registerSessionActor(newSessionId, sessionActorRef);
		sendJoinSessionToSender(newSessionId);
	}

	public void getSessionActor(GetSessionActor getSessionActor) {
		int sessionId = getSessionActor.sessionId;
		if (sessions.containsKey(sessionId)) {
			sendSessionActorToSender(sessionId);
		} else {
			sendNoSessionFoundToSender(sessionId);
		}
	}

	private void sendJoinSessionToSender(int newSessionId) {
		JoinSession joinSession = new JoinSession();
		joinSession.sessionid = newSessionId;
		Logger.info("JoinSession started id : {}", newSessionId);
		sender().tell(joinSession, self());
	}

	private void registerSessionActor(int newSessionId, ActorRef sessionActorRef) {
		sessions.put(newSessionId, sessionActorRef);
	}

	private ActorRef createNewSessionActor(
	  int newSessionId,
	  int exerciseId,
	  String sessionOwner) {
		Props sessionActorProps = SessionActor.props(
		  newSessionId,
		  exerciseId,
		  sessionOwner);
		ActorRef sessionActorRef = getContext().actorOf(
		  sessionActorProps,
		  "sessionActor-" + newSessionId);
		return sessionActorRef;
	}

	private void sendNoSessionFoundToSender(int sessionId) {
		NoSessionFound noSessionFound = new NoSessionFound(sessionId);
		sender().tell(noSessionFound, self());
	}

	private void sendSessionActorToSender(int sessionId) {
		ActorRef sessionActorRef = sessions.get(sessionId);
		GetSessionActorReply getSessionActorReply = new GetSessionActorReply(
		  sessionId,
		  sessionActorRef);
		sender().tell(getSessionActorReply, self());
	}

	public int getNextSessionId() {
		int sessionId = nextSessionId;
		nextSessionId++;
		return sessionId;
	}

	/**
	 * Send to retrieve the {@link SessionActor} that handles a session identified
	 * by its id
	 */
	public static class GetSessionActor {
		int sessionId;

		public GetSessionActor(int sessionId) {
			this.sessionId = sessionId;
		}
	}

	/**
	 * Reply to a {@link GetSessionActor}
	 */
	public static class GetSessionActorReply {
		public int sessionId;
		public ActorRef sessionActor;

		public GetSessionActorReply(int sessionId, ActorRef sessionActor) {
			this.sessionId = sessionId;
			this.sessionActor = sessionActor;
		}
	}

	public static class NoSessionFound {
		private final int sessionId;

		public NoSessionFound(int sessionId) {
			this.sessionId = sessionId;
		}
	}

}
