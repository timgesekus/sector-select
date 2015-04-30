package joinSessionView;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import view.chat.ChatView;
import view.chat.ChatView.Chat;

import com.googlecode.protobuf.format.JsonFormat;

import chat.command.ChatCommand;
import chat.command.ChatCommand.RestoreChat;
import chat.command.ChatCommand.RestoreChat.Builder;
import chat.event.ChatEvent;
import chat.event.ChatEvent.MessageChated;
import chat.event.ChatEvent.RestoreChatComplete;
import eventBus.EventBus;
import eventBus.Topics;
import actor.SessionManager.GetSessionActorReply;
import actor.SessionWorkspaceAssignements.WorkspaceAssignements;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class ChatPresenter extends AbstractActor
{
  public static Props props(final String userName, final int sessionId, final String chatId,
      final EventBus eventBus, final ActorRef out)
  {
    return Props.create(new Creator< ChatPresenter >()
    {
      private static final long serialVersionUID = 1398731621836926808L;


      @Override
      public ChatPresenter create() throws Exception
      {
        return new ChatPresenter(userName, sessionId, chatId, eventBus, out);
      }

    });

  }


  private List< String > chat = new ArrayList<>();
  private boolean restored;
  private ActorRef out;
  final Logger.ALogger logger = Logger.of(this.getClass());


  public ChatPresenter(String userName, int sessionId, final String chatId, EventBus eventBus, ActorRef out)
  {
    this.out = out;
    restored = false;
    receive(ReceiveBuilder.match(ChatEvent.MessageChated.class, this::messageChated)
        .match(RestoreChatComplete.class, this::restoreChatCompelte)
        .matchAny(this::unhandled)
        .build());
    eventBus.subscribe(self(), Topics.CHAT_EVENT.toString());
    RestoreChat restoreChat = ChatCommand.RestoreChat.newBuilder()
        .setChatId(chatId)
        .build();
    eventBus.publish(Topics.CHAT_COMMAND.toString(), restoreChat, self());
  }


  public void unhandled(Object message)
  {
    logger.info("Received unknown message {} {}", message.getClass()
        .toString(), message);
  }


  public void restoreChatCompelte(RestoreChatComplete restoreChatComplete)
  {
    restored = true;
  }


  public void messageChated(MessageChated messageChated)
  {
    logger.info("Message chated {}", messageChated.getChatId());
    chat.add(messageChated.getUserId() + ":" + messageChated.getMessage());
    if (restored)
    {
      Chat.Builder chatBuilder = Chat.newBuilder();
      chatBuilder.setTopic("ChatViewModel");
      chat.stream()
          .forEach(message -> chatBuilder.addMessages(message));
      Chat chatMessage = chatBuilder.build();
      String jsonMessage = JsonFormat.printToString(chatMessage);
      logger.info("Sending message {}", jsonMessage);
      out.tell(jsonMessage, self());
    }
  }
}
