package actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class SessionChat extends AbstractActor {

	private final Map<ActorRef, String> subscribers = new HashMap<>();
	private final List<ChatLine> chat = new ArrayList<>();

	public static Props props() {
		return Props.create(new Creator<SessionChat>() {
			private static final long serialVersionUID = 1L;

			@Override
			public SessionChat create() throws Exception {
				return new SessionChat();
			}
		});
	}

	public SessionChat() {
		configureMessageHandling();
	}

	private void configureMessageHandling() {
		receive(ReceiveBuilder
		  .match(ChatMessage.class, this::handleChatMessage)
		  .match(Subscribe.class, this::handleSubscription)
		  .match(Unsubscribe.class, this::handleUnsubscribe)
		  .matchAny(this::unhandled)
		  .build());
	}

	private void handleChatMessage(ChatMessage chatMessage) {
		String userName = chatMessage.userName;
		String message = chatMessage.message;
		addChatLine(userName, message);
	}

	private void handleSubscription(Subscribe subscribe) {
		Logger.info(
		  "Chat received subscription {}:{}: ",
		  sender(),
		  subscribe.userName);
		subscribers.put(sender(), subscribe.userName);
		sendChat();
		addChatLine(subscribe.userName, "Entered chat");
	}

	private void handleUnsubscribe(Unsubscribe unsubscribe) {
		Logger.info("Chat received unsubscribe {}:{}: ", sender());
		String userName = subscribers.getOrDefault(sender(), "unknown");
		subscribers.remove(sender());
		addChatLine(userName, "Left chat");
	}

	private void sendChat() {
		Logger.info("Sending chat");
		for (ChatLine chatLine : chat) {
			sender().tell(chatLine, self());
		}
	}

	private void addChatLine(String userName, String message) {
		Logger.info("Adding to chat {}:{}", userName, message);
		ChatLine chatLine = new ChatLine(userName, message);
		chat.add(chatLine);
		sendLastLineToAll();
	}

	private void sendLastLineToAll() {
		if (chat.size() > 0) {
			Logger.info("Sending last line");
			ChatLine chatLine = chat.get(chat.size() - 1);
			for (ActorRef subscriber : subscribers.keySet()) {
				subscriber.tell(chatLine, subscriber);
			}
		} else {
			Logger.info("Chat is empty");
		}
	}

	public static class ChatMessage {
		private String message;
		private String userName;

		public ChatMessage(String userName, String message) {
			this.message = message;
			this.userName = userName;

		}
	}

	public static class ChatLine {
		private String userName;
		private String message;
		private String topic;

		public ChatLine(String userName, String message) {
			setUserName(userName);
			setMessage(message);
			setTopic("chatline");
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}
	}

}
