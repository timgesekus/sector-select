package services;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import chat.command.ChatCommand.ChatMessage;
import chat.command.ChatCommand.RestoreChat;
import chat.command.ChatCommand.UserJoinChat;
import chat.command.ChatCommand.UserLeaveChat;
import chat.event.ChatEvent.MessageChated;
import chat.event.ChatEvent.RestoreChatComplete;
import chat.event.ChatEvent.UserJoinedChat;
import chat.event.ChatEvent.UserLeftChat;
import eventBus.EventBus;
import eventBus.Topic;

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
    subscribeToChatCommands();
  }

  private void subscribeToChatCommands()
  {
    eventBus.subscribe(self(), Topic.CHAT_COMMAND);
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
    logger.info("Received unknown message {} {}", message.getClass(), message);
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
      publishAndStore(userJoinedChat);
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
      publishAndStore(userLeftChat);
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
    logger.info(
      "Restoration requst {} sender:{} ",
      requestChat.getChatId(),
      sender());
    events.stream().forEach(event -> sendMessageToSender(event));
    sendMessageToSender(RestoreChatComplete
      .newBuilder()
      .setChatId(chatId)
      .build());
  }

  private void publishAndStore(Object message)
  {
    events.add(message);
    eventBus.publish(Topic.CHAT_EVENT, message);
  }

  private void sendMessageToSender(Object event)
  {
    sender().tell(event, self());
  }
}
