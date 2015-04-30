package services;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import chat.command.ChatCommand.CloseChat;
import chat.command.ChatCommand.CreateChat;
import chat.event.ChatEvent.ChatClosed;
import chat.event.ChatEvent.ChatCreated;
import chat.error.ChatError.CreateChatFailed;
import eventBus.Event;
import eventBus.EventBus;
import eventBus.Topics;

public class ChatService extends AbstractActor
{
  public static Props props(EventBus eventBus)
  {
    return Props.create(new Creator<ChatService>()
    {
      private static final long serialVersionUID = 279988858865554278L;

      @Override
      public ChatService create() throws Exception
      {
        return new ChatService(eventBus);
      }
    });
  }

  private EventBus eventBus;
  private Map<String, ActorRef> chats = new HashMap<>();
  final Logger.ALogger logger = Logger.of(this.getClass());

  public ChatService(EventBus eventBus)
  {
    logger.info("Chat service created");
    this.eventBus = eventBus;
    configureMessageHandling();
    subscribteToChatCommands();
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(CreateChat.class, this::createChat)
      .match(CloseChat.class, this::closeChat)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info("Received unknown message {}", message);
  }

  private void subscribteToChatCommands()
  {
    logger.info("Subscribing to chat commands");
    eventBus.subscribe(self(), Topics.CHAT_COMMAND.toString());
  }

  private void createChat(CreateChat createChat)
  {
    String chatId = createChat.getChatId();
    logger.info("Create chat with id {}", chatId);
    if (chats.containsKey(chatId))
    {
      sender().tell(
        CreateChatFailed
          .newBuilder()
          .setChatId(chatId)
          .setCause("ChatId exists")
          .build(),
        self());
    } else
    {
      Props chatProps = Chat.props(chatId, eventBus);
      ActorRef chat = getContext().actorOf(chatProps, chatId);
      chats.put(chatId, chat);
      ChatCreated chatCreated = ChatCreated
        .newBuilder()
        .setChatId(chatId)
        .build();
      eventBus.publish(new Event(Topics.CHAT_EVENT.toString(), chatCreated));

    }
  }

  private void closeChat(CloseChat closeChat)
  {
    String chatId = closeChat.getChatId();
    ChatClosed chatCreated = ChatClosed.newBuilder().setChatId(chatId).build();
    eventBus.publish(new Event(Topics.CHAT_EVENT.toString(), chatCreated));
  }
}
