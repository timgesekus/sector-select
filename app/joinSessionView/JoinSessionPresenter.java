package joinSessionView;

import java.util.ArrayList;
import java.util.List;

import joinSession.viewmodel.ChatViewModel;
import play.Logger;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;
import actor.SessionChat.ChatLine;
import actor.SessionManager.GetSessionActor;
import actor.SessionManager.GetSessionActorReply;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import actor.util.Subscriptions;
import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

/**
 * Presents the sector model to the join session page
 */
public class JoinSessionPresenter extends AbstractActorWithStash {
	private final Subscriptions subscriptions;
	private final PartialFunction<Object, BoxedUnit> waitForSessionActor;
	private final PartialFunction<Object, BoxedUnit> normalState;
	private ActorRef sessionManager;
	private int sessionId;
	private ActorRef sessionActor;
	private String userName;
	private final List<String> chatLines;

	final Logger.ALogger logger = Logger.of(this.getClass());

	public static Props props(
	  String userName,
	  int sessionId,
	  ActorRef sessionManager) {
		return Props.create(new Creator<JoinSessionPresenter>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3019314840688970341L;

			@Override
			public JoinSessionPresenter create() throws Exception {
				return new JoinSessionPresenter(userName, sessionId, sessionManager);
			}
		});
	}

	public JoinSessionPresenter(
	  String userName,
	  int sessionId,
	  ActorRef sessionManager) {

		this.userName = userName;
		this.sessionId = sessionId;
		this.sessionManager = sessionManager;
		this.chatLines = new ArrayList<>();
		subscriptions = new Subscriptions();

		waitForSessionActor = ReceiveBuilder
		  .match(GetSessionActorReply.class, this::handleSessionActorReply)
		  .matchAny(x -> stash())
		  .build();

		normalState = ReceiveBuilder
		  .match(GetSessionActorReply.class, this::handleSessionActorReply)
		  .match(ChatLine.class, this::handleChatLine)
		  .match(Subscribe.class, this::handleSubscription)
		  .match(Unsubscribe.class, this::handleUnsubscribe)
		  .matchAny(this::handleUnknownMessage)
		  .build();

		startWaitingForSessionActor();
		requestSessionActor();
	}

	private void startWaitingForSessionActor() {
		receive(waitForSessionActor);
		unstashAll();
	}

	public
	  void
	  handleSessionActorReply(GetSessionActorReply getSessionActorReply) {
		setSessionActor(getSessionActorReply);
		startNormalOperation();
		subscribeToSession();
	}

	public void handleSubscription(Subscribe subscribe) {
		subscriptions.handleSubscription(subscribe, sender());
		sendCurrentModel();
	}

	public void handleUnsubscribe(Unsubscribe unsubscribe) {
		subscriptions.handleUnsubscribe(unsubscribe, sender());
	}

	public void handleChatLine(ChatLine chatLine) {
		chatLines.add(chatLine.getUserName() + ": " + chatLine.getMessage());
		sendCurrentModel();

	}

	private void sendCurrentModel() {
		ChatViewModel chatViewModel = new ChatViewModel(chatLines);
		subscriptions.sendToSubscribers(chatViewModel, self());
	}

	private void startNormalOperation() {
		getContext().become(normalState);
	}

	private void subscribeToSession() {
		sessionActor.tell(new Subscribe(userName), self());
	}

	private void setSessionActor(GetSessionActorReply getSessionActorReply) {
		this.sessionActor = getSessionActorReply.sessionActor;
	}

	private void requestSessionActor() {
		this.sessionManager.tell(new GetSessionActor(this.sessionId), self());
	}

	private void handleUnknownMessage(Object message) {
		logger.error("Unkown message:" + message.toString());
	}

	@Override
	public void postStop() {
		Logger.info("Websocket for {} with id {} closed", self(), userName);
		if (sessionActor != null) {
			Unsubscribe unsubscribe = new Unsubscribe();
			sessionActor.tell(unsubscribe, self());
		}
	}

}