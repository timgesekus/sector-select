package joinSessionView;

import java.io.IOException;

import joinSession.viewmodel.WorkspaceAssignementViewModel;
import play.Logger;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import chat.command.ChatCommand.ChatMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eventBus.EventBus;
import eventBus.Topic;

public class JoinSessionWS extends AbstractActor
{

  public static Props props(
    ActorRef out,
    String userName,
    EventBus eventBus,
    String sessionId)
  {
    return Props.create(new Creator<JoinSessionWS>()
    {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public JoinSessionWS create() throws Exception
      {
        return new JoinSessionWS(out, userName, eventBus, sessionId);
      }

    });

  }

  private final ObjectMapper objectMapper;
  final Logger.ALogger logger = Logger.of(this.getClass());
  private final ActorRef out;
  private String userName;
  private EventBus eventBus;
  private String sessionId;
  private String chatId;

  public JoinSessionWS(
    ActorRef out,
    String userName,
    EventBus eventBus,
    String sessionId) throws JsonProcessingException
  {
    this.userName = userName;
    this.eventBus = eventBus;
    this.sessionId = sessionId;
    receive(ReceiveBuilder
      .match(String.class, this::receiveJsonFromSocket)
      .match(
        WorkspaceAssignementViewModel.class,
        this::receiveWorkspaceAssignementsViewModel)
      .build());

    this.out = out;
    this.userName = userName;
    this.chatId = "chat-" + sessionId;

    objectMapper = new ObjectMapper();
  }

  public void receiveJsonFromSocket(String json)
    throws JsonParseException,
    JsonMappingException,
    IOException
  {
    Logger.info("Received a message:" + json);
    JsonNode jsonNode = objectMapper.readTree(json);
    String topic = jsonNode.get("topic").asText();
    Logger.info("Topic is :" + topic);
    if (topic.equals("select"))
    {
      Logger.info("Select event: ");
      String workspace = jsonNode.get("sector").asText();
      WorkspaceSelection event = new WorkspaceSelection(workspace, userName);
      eventBus.publish(Topic.CHAT_COMMAND, event);
    } else if (topic.equals("chatMessage"))
    {
      String message = jsonNode.get("message").asText();
      ChatMessage chatLine = ChatMessage
        .newBuilder()
        .setUserId(userName)
        .setChatId(chatId)
        .setMessage(message)
        .build();
      eventBus.publish(Topic.CHAT_COMMAND, chatLine);
    }

  }

  public void receiveWorkspaceAssignementsViewModel(
    WorkspaceAssignementViewModel workspaceAssignementViewModel)
    throws JsonProcessingException
  {
    logger.info("Received a workspaceAssignementViewModel "
        + workspaceAssignementViewModel.workspaceAssignements.size());
    String sectorsAsJson = objectMapper
      .writeValueAsString(workspaceAssignementViewModel);
    out.tell(sectorsAsJson, self());
  }

  public static class PropCreater
  {
    private final String userName;

    public PropCreater(String userName)
    {
      this.userName = userName;
    }

    public Props props(ActorRef out)
    {
      return Props.create(JoinSessionWS.class, out, userName);
    }

  }

  public static class WorkspaceSelection
  {
    public String topic;
    public String sector;
    public String userName;

    public WorkspaceSelection(String workspace, String userName)
    {
      this.sector = workspace;
      this.userName = userName;
      topic = "event";
    }
  }

}
