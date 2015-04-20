package actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eventBus.Event;
import eventBus.EventBus;
import play.Logger;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import actor.util.Subscriptions;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class SessionChat extends AbstractActor
{

  private final Subscriptions subscriptions = new Subscriptions();
  private final List<ChatLine> chat = new ArrayList<>();
  private ActorRef messageForwarder;
  private EventBus eventBus;

  public static Props props(EventBus eventBus)
  {
    return Props.create(new Creator<SessionChat>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public SessionChat create() throws Exception
      {
        return new SessionChat(eventBus);
      }
    });
  }

  public SessionChat(EventBus eventBus)
  {
    this.eventBus = eventBus;
    configureMessageHandling();
    this.eventBus.subscribe(self(), "ChatMessage");
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(ChatMessage.class, this::handleChatMessage)
      .match(Subscribe.class, this::handleSubscribe)
      .match(Unsubscribe.class, this::handleUnsubscribe)
      .match(Event.class, this::handleEvent)
      .matchAny(this::unhandled)
      .build());
  }

  private void handleEvent(Event event)
  {
    ChatMessage chatMessage = (ChatMessage) event.payload;
    handleChatMessage(chatMessage);
    Logger.info("Chat message arrived");
  }

  private void handleChatMessage(ChatMessage chatMessage)
  {
    String userName = chatMessage.userName;
    String message = chatMessage.message;
    addChatLine(userName, message);
  }

  private void handleSubscribe(Subscribe subscribe)
  {
    Logger.info("Chat received unsubscribe {}:{}: ", sender());
    String userName = subscribe.userName;
    subscriptions.handleSubscription(subscribe, sender());
    sendChat(sender());
    addChatLine(userName, "Entered chat");
  }

  private void handleUnsubscribe(Unsubscribe unsubscribe)
  {
    Logger.info("Chat received unsubscribe {}:{}: ", sender());
    String userName = subscriptions.getUserName(sender());
    addChatLine(userName, "Left chat");
    subscriptions.handleUnsubscribe(unsubscribe, sender());
  }

  private void sendChat(ActorRef newSubscriber)
  {
    Logger.info("Sending chat");
    for (ChatLine chatLine : chat)
    {
      newSubscriber.tell(chatLine, self());
    }
  }

  private void addChatLine(String userName, String message)
  {
    Logger.info("Adding to chat {}:{}", userName, message);
    ChatLine chatLine = new ChatLine(userName, message);
    chat.add(chatLine);
    sendLastLineToAll();
  }

  private void sendLastLineToAll()
  {
    if (chat.size() > 0)
    {
      Logger.info("Sending last line");
      ChatLine chatLine = chat.get(chat.size() - 1);
      subscriptions.publish(chatLine, self());
    } else
    {
      Logger.info("Chat is empty");
    }

  }

  public static class ChatMessage
  {
    private String message;
    private String userName;
    private int sessionId;

    public ChatMessage(String userName, int sessionId, String message)
    {
      this.sessionId = sessionId;
      this.message = message;
      this.userName = userName;

    }
  }

  public static class ChatLine
  {
    private String userName;
    private String message;
    private String topic;

    public ChatLine(String userName, String message)
    {
      setUserName(userName);
      setMessage(message);
      setTopic("chatline");
    }

    public String getUserName()
    {
      return userName;
    }

    public void setUserName(String userName)
    {
      this.userName = userName;
    }

    public String getMessage()
    {
      return message;
    }

    public void setMessage(String message)
    {
      this.message = message;
    }

    public String getTopic()
    {
      return topic;
    }

    public void setTopic(String topic)
    {
      this.topic = topic;
    }
  }

}
