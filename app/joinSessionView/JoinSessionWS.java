package joinSessionView;

import java.io.IOException;

import joinSession.viewmodel.ChatViewModel;
import joinSession.viewmodel.WorkspaceAssignementViewModel;
import play.Logger;
import actor.messages.Subscribe;
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
import eventBus.Topics;

public class JoinSessionWS extends AbstractActor
{

 
  public static Props props(
    ActorRef out,
    String userName,
    EventBus eventBus,
    int sessionId)
  {
    return Props.create(new Creator<JoinSessionWS>()
    {

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
  private int sessionId;
  private String chatId;
  
  public JoinSessionWS(
    ActorRef out,
    String userName,
    EventBus eventBus,
    int sessionId) throws JsonProcessingException
  {
    this.userName = userName;
    this.eventBus = eventBus;
    this.sessionId = sessionId;
    receive(ReceiveBuilder
      .match(String.class, this::receiveJsonFromSocket)
      .match(
        WorkspaceAssignementViewModel.class,
        this::receiveWorkspaceAssignementsViewModel)
      .match(ChatViewModel.class, this::receiveChatViewModel)
      .build());

    this.out = out;
    this.userName = userName;
    this.chatId = "chat-" + sessionId;
    
    objectMapper = new ObjectMapper();
    Props chatPresenterProps = ChatPresenter.props(
      userName,
      sessionId,
      chatId,
      eventBus,
      out);
    getContext().actorOf(chatPresenterProps);
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
      eventBus.publish("workspaceSelectionEvent", event);
    } else if (topic.equals("chatMessage"))
    {
      String message = jsonNode.get("message").asText();
      ChatMessage chatLine = ChatMessage
        .newBuilder()
        .setUserId(userName)
        .setChatId(chatId)
        .setMessage(message)
        .build();
      eventBus.publish(Topics.CHAT_COMMAND.toString(), chatLine);
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

  public void receiveChatViewModel(ChatViewModel chatViewModel)
    throws JsonProcessingException
  {
    logger.info("Received a chat view model ");
    String chatViewModelAsJson = objectMapper.writeValueAsString(chatViewModel);
    out.tell(chatViewModelAsJson, self());
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
