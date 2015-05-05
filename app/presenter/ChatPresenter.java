package presenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import view.chat.ChatView.Chat;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import chat.command.ChatCommand;
import chat.command.ChatCommand.ChatMessage;
import chat.command.ChatCommand.RestoreChat;
import chat.command.ChatCommand.UserJoinChat;
import chat.command.ChatCommand.UserLeaveChat;
import chat.event.ChatEvent.MessageChated;
import chat.event.ChatEvent.RestoreChatComplete;
import chat.event.ChatEvent.UserJoinedChat;
import chat.event.ChatEvent.UserLeftChat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.protobuf.format.JsonFormat;

import eventBus.EventBus;
import eventBus.Topic;

public class ChatPresenter extends AbstractActor
{
  public static Props props(
    final String userName,
    final String chatId,
    final EventBus eventBus,
    final ActorRef out)
  {
    return Props.create(new Creator<ChatPresenter>()
    {
      private static final long serialVersionUID = 1398731621836926808L;

      @Override
      public ChatPresenter create() throws Exception
      {
        return new ChatPresenter(userName, chatId, eventBus, out);
      }

    });

  }

  private List<String> chat = new ArrayList<>();
  private boolean restored;
  private ActorRef out;
  final Logger.ALogger logger = Logger.of(this.getClass());
  private String userName;
  private String chatId;
  private ObjectMapper objectMapper;
  private EventBus eventBus;

  public ChatPresenter(
    final String userName,
    final String chatId,
    final EventBus eventBus,
    final ActorRef out)
  {
    this.userName = userName;
    this.chatId = chatId;
    this.eventBus = eventBus;

    this.out = out;
    objectMapper = new ObjectMapper();
    subscribteForChatEvents(eventBus);
    requestChatRestoration();
    sendJoinChat();
  }

  private void requestChatRestoration()
  {
    logger.info("Request chat restoration {} ", chatId);
    restored = false;
    RestoreChat restoreChat = ChatCommand.RestoreChat
      .newBuilder()
      .setChatId(chatId)
      .build();
    eventBus.publish(Topic.CHAT_COMMAND, restoreChat, self());
  }

  private void sendJoinChat()
  {
    UserJoinChat userJoinChat = UserJoinChat
      .newBuilder()
      .setChatId(chatId)
      .setUserId(userName)
      .build();
    eventBus.publish(Topic.CHAT_COMMAND, userJoinChat, self());
  }

  private void subscribteForChatEvents(final EventBus eventBus)
  {
    configureMessageReceiving();
    eventBus.subscribe(self(), Topic.CHAT_EVENT);
  }

  private void configureMessageReceiving()
  {
    receive(ReceiveBuilder
      .match(MessageChated.class, this::messageChated)
      .match(UserJoinedChat.class, this::userJoinedChat)
      .match(UserLeftChat.class, this::userLeftChat)
      .match(RestoreChatComplete.class, this::restoreChatCompelte)
      .match(String.class, this::handleJsonFromSocket)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info(
      "Received unknown message {} {}",
      message.getClass().toString(),
      message);
  }

  public void restoreChatCompelte(RestoreChatComplete restoreChatComplete)
  {
    logger.info("Restore complete {} ", chatId);
    restored = true;
  }

  public void messageChated(MessageChated messageChated)
  {
    if (messageChated.getChatId().equals(chatId)) {
      logger.info("Message chated {}", messageChated.getChatId());
      storeMessage(messageChated.getUserId() + ":" + messageChated.getMessage());
      if (restored)
      {
        sendViewModelToSocket();
      }
    }
  }

  private void userJoinedChat(UserJoinedChat userJoinedChat)
  {
    if (userJoinedChat.getChatId().equals(chatId))
    {
      String joinMessage = userJoinedChat.getUserId() + " entered chat";
      storeMessage(joinMessage);
      if (restored)
      {
        sendViewModelToSocket();
      }
    }
  }

  private void userLeftChat(UserLeftChat userLeftChat)
  {
    if (userLeftChat.getChatId().equals(chatId))
    {
      String joinMessage = userLeftChat.getUserId() + " left chat";
      storeMessage(joinMessage);
      if (restored)
      {
        sendViewModelToSocket();
      }
    }
  }

  private void storeMessage(String message)
  {
    chat.add(message);
  }

  private void sendViewModelToSocket()
  {
    Chat.Builder chatBuilder = Chat.newBuilder();
    chatBuilder.setTopic("ChatViewModel");
    chat.stream().forEach(message -> chatBuilder.addMessages(message));
    Chat chatMessage = chatBuilder.build();
    String jsonMessage = JsonFormat.printToString(chatMessage);
    out.tell(jsonMessage, self());
  }

  public void handleJsonFromSocket(String json)
    throws JsonParseException,
    JsonMappingException,
    IOException
  {
    logger.info("Received a message:" + json);
    JsonNode jsonNode = objectMapper.readTree(json);
    String topic = extractTopic(jsonNode);
    Logger.info("Topic is :" + topic);
    if (topic.equals("chatMessage"))
    {
      String message = extractMessage(jsonNode);
      publishMessage(message);
    } else
    {
      logger.error("Unkown message from socket {} ", topic);
    }
  }

  private String extractMessage(JsonNode jsonNode)
  {
    return jsonNode.get("message").asText();
  }

  private void publishMessage(String message)
  {
    ChatMessage chatLine = ChatMessage
      .newBuilder()
      .setUserId(userName)
      .setChatId(chatId)
      .setMessage(message)
      .build();
    eventBus.publish(Topic.CHAT_COMMAND, chatLine);
  }

  private String extractTopic(JsonNode jsonNode)
  {
    return jsonNode.get("topic").asText();
  }

  @Override
  public void postStop() throws Exception
  {
    eventBus.unsubscribe(self());
    UserLeaveChat userLeaveChat = UserLeaveChat
      .newBuilder()
      .setChatId(chatId)
      .setUserId(userName)
      .build();
    eventBus.publish(Topic.CHAT_COMMAND, userLeaveChat);
  }
}
