package services;

import java.util.HashMap;
import java.util.Map;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
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
  private EventBus eventBus;
  private Map<String, ActorRef> chats = new HashMap<>();

  public ChatService(EventBus eventBus)
  {
    this.eventBus = eventBus;
    configureMessageHandling();
    subscribteToChatCommands();
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(CreateChat.class, this::createChat)
      .match(CloseChat.class, this::closeChat)
      .build());
  }

  private void subscribteToChatCommands()
  {
    eventBus.subscribe(self(), Topics.CHAT_COMMAND.toString());
  }

  private void createChat(CreateChat createChat)
  {
    String chatId = createChat.getChatId();
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
