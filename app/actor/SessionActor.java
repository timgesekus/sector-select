package actor;

import java.util.HashMap;
import java.util.Map;

import joinSessionView.JoinSessionWS.Event;
import play.Logger;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class SessionActor extends AbstractActor {
	private final Map<ActorRef, String> subscribers = new HashMap<>();
	private int sessionId;
	private ActorRef sessionChaTActorRef;
	private ActorRef sessionWorkspaceAssignement;

	public static Props props(int sessionId, int exerciseId, String ownerName) {
		return Props.create(new Creator<SessionActor>() {
			private static final long serialVersionUID = 1L;

			@Override
			public SessionActor create() throws Exception {
				return new SessionActor(sessionId, exerciseId, ownerName);
			}
		});
	}

	public SessionActor(int sessionId, int exerciseId, String ownerName) {
		this.sessionId = sessionId;
		configureMessageHandling();
		createChat();
		createWorkspaceAssignements();
	}

	private void configureMessageHandling() {
		receive(ReceiveBuilder
		  .match(Event.class, this::forwardToWorkspaceAssignementActor)
		  .match(Subscribe.class, this::handleSubscription)
		  .match(Unsubscribe.class, this::handleUnsubscribe)
		  .matchAny(this::unhandled)
		  .build());
	}

	private void createChat() {
		Props props = SessionChat.props();
		sessionChaTActorRef = getContext().actorOf(
		  props,
		  "sessionChatActor-" + sessionId);
	}

	private void createWorkspaceAssignements() {
		Props props = SessionWorkspaceAssignements.props();
		sessionWorkspaceAssignement = getContext().actorOf(
		  props,
		  "workspaceAssignement-" + sessionId);
	}

	private void forwardToWorkspaceAssignementActor(Event event) {
		sessionWorkspaceAssignement.tell(event, sender());
	}

	private void handleSubscription(Subscribe subscribe) {
		Logger.info("Received subscription {}:{}: ", sender(), subscribe.userName);
		subscribers.put(sender(), subscribe.userName);
		sendToChildren(subscribe);
	}

	private void handleUnsubscribe(Unsubscribe unsubscribe) {
		Logger.info("Received unsubscribe {}:{}: ", sender());
		subscribers.remove(sender());
		sendToChildren(unsubscribe);
	}

	private void sendToChildren(Object message) {
		sessionChaTActorRef.tell(message, sender());
		sessionWorkspaceAssignement.tell(message, sender());
	}
}
