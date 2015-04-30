package services;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import chat.command.ChatCommand.ChatMessage;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import chat.command.ChatCommand.RestoreChat;
import chat.command.ChatCommand.UserJoinChat;
import chat.command.ChatCommand.UserLeaveChat;
import chat.event.ChatEvent.MessageChated;
import chat.event.ChatEvent.RestoreChatComplete;
import chat.event.ChatEvent.UserJoinedChat;
import chat.event.ChatEvent.UserLeftChat;
import eventBus.Event;
import eventBus.EventBus;
import eventBus.Topics;

public class Chat extends AbstractActor
{

  final Logger.ALogger logger = Logger.of(this.getClass());

  public static Props props(String chatId, EventBus eventBus)
  {
    return Props.create(new Creator<Chat>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public Chat create() throws Exception
      {
        return new Chat(chatId, eventBus);
      }
    });
  }

  private String chatId;
  private EventBus eventBus;
  private List<Object> events = new ArrayList<>();

  public Chat(String chatId, EventBus eventBus)
  {
    logger.info("Chat with id {} created", chatId);
    this.chatId = chatId;
    this.eventBus = eventBus;
    configureMessageHandling();
    subscribteToChatCommands();
  }

  private void subscribteToChatCommands()
  {
    eventBus.subscribe(self(), Topics.CHAT_COMMAND.toString());
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(UserJoinChat.class, this::userJoinChat)
      .match(UserLeaveChat.class, this::userLeaveChat)
      .match(ChatMessage.class, this::chatMessage)
      .match(RestoreChat.class, this::restoreChat)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info("Received unknown message {}", message);
  }

  private void userJoinChat(UserJoinChat userJoinChat)
  {
    if (userJoinChat.getChatId().equals(chatId))
    {
      String userId = userJoinChat.getUserId();
      UserJoinedChat userJoinedChat = UserJoinedChat
        .newBuilder()
        .setChatId(chatId)
        .setUserId(userId)
        .build();
      MessageChated messageChated = MessageChated
        .newBuilder()
        .setChatId(chatId)
        .setUserId(userId)
        .setMessage(userId + "entered chat")
        .build();
      publishAndStore(userJoinedChat);
      publishAndStore(messageChated);
    }
  }

  private void userLeaveChat(UserLeaveChat userLeaveChat)
  {
    if (userLeaveChat.getChatId().equals(chatId))
    {
      String userId = userLeaveChat.getUserId();
      UserLeftChat userLeftChat = UserLeftChat
        .newBuilder()
        .setChatId(chatId)
        .setUserId(userId)
        .build();
      MessageChated messageChated = MessageChated
        .newBuilder()
        .setChatId(chatId)
        .setUserId(userId)
        .setMessage(userId + "left chat")
        .build();
      publishAndStore(userLeftChat);
      publishAndStore(messageChated);
    }
  }

  private void chatMessage(ChatMessage chatMessage)
  {
    logger.info("Chat message: {}", chatMessage);

    if (chatMessage.getChatId().equals(chatId))
    {
      String userId = chatMessage.getUserId();
      String message = chatMessage.getMessage();
      MessageChated messageChated = MessageChated
        .newBuilder()
        .setChatId(chatId)
        .setUserId(userId)
        .setMessage(message)
        .build();
      publishAndStore(messageChated);
    }
  }

  private void restoreChat(RestoreChat requestChat)
  {
    events.stream().forEach(event -> sendEventToSender(event));
    sendEventToSender(new Event(
      Topics.CHAT_EVENT.toString(),
      RestoreChatComplete.newBuilder().build()));
  }

  private void publishAndStore(Object message)
  {
    events.add(message);
    eventBus.publish(Topics.CHAT_EVENT.toString(), message);
  }

  private void sendEventToSender(Object event)
  {
    sender().tell(new Event(Topics.CHAT_EVENT.toString(), event), self());
  }
}
