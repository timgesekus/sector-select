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

public class SessionManager extends AbstractActor {
	static int nextSessionId = 0;
	private final Map<Integer, ActorRef> sessions;

	public static Props props() {
		return Props.create(new Creator<SessionManager>() {

			/**
			 *
			 */
			private static final long serialVersionUID = -3975356938966488960L;

			@Override
			public SessionManager create() throws Exception {
				return new SessionManager();
			}

		});
	}

	public SessionManager() {
		Logger.info("Starting session Manager");
		sessions = new HashMap<>();
		receive(ReceiveBuilder.match(StartExercise.class, this::startExercise)
				.match(GetSessionActor.class, this::getSessionActor).build());
	}

	public void startExercise(StartExercise startExercise) {
		int newSessionId = getNextSessionId();
		Props sessionActorProps = SessionActor.props(newSessionId,
				startExercise.exerciseId, startExercise.ownerName);
		ActorRef sessionActorRef = getContext().actorOf(sessionActorProps);
		sessions.put(newSessionId, sessionActorRef);
		JoinSession joinSession = new JoinSession();
		joinSession.sessionid = newSessionId;
		Logger.info("Session started id : {}", newSessionId);
		sender().tell(joinSession, self());
	}

	public void getSessionActor(GetSessionActor getSessionActor) {
		int sessionId = getSessionActor.sessionId;
		if (sessions.containsKey(sessionId)) {
			ActorRef sessionActorRef = sessions.get(sessionId);
			GetSessionActorAnswer getSessionActorAnswer = new GetSessionActorAnswer();
			getSessionActorAnswer.sessionId = sessionId;
			getSessionActorAnswer.sessionActor = sessionActorRef;
			sender().tell(getSessionActorAnswer, self());
		} else {
			NoSessionFound noSessionFound = new NoSessionFound(sessionId);
			sender().tell(noSessionFound, self());

		}
	}

	public int getNextSessionId() {
		int sessionId = nextSessionId;
		sessionId++;
		return sessionId;
	}

	public static class GetSessionActor {
		int sessionId;

		public GetSessionActor(int sessionId) {
			this.sessionId = sessionId;
		}
	}

	public static class GetSessionActorAnswer {
		int sessionId;
		ActorRef sessionActor;
	}

	public static class NoSessionFound {
		private final int sessionId;

		public NoSessionFound(int sessionId) {
			this.sessionId = sessionId;
		}
	}

}
