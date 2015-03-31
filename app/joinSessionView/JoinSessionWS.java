package joinSessionView;

import java.io.IOException;

import joinSession.viewmodel.ChatViewModel;
import joinSession.viewmodel.WorkspaceAssignementViewModel;
import play.Logger;
import actor.SessionChat.ChatMessage;
import actor.messages.Subscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JoinSessionWS extends AbstractActor {

	private final ObjectMapper objectMapper;
	final Logger.ALogger logger = Logger.of(this.getClass());
	private final ActorRef out;
	private String userName;
	private ActorRef joinSessionPresenter;

	public static Props props(
	  ActorRef out,
	  String userName,
	  ActorRef joinSessionPresenter) {
		return Props.create(new Creator<JoinSessionWS>() {

			@Override
			public JoinSessionWS create() throws Exception {
				return new JoinSessionWS(out, userName, joinSessionPresenter);
			}

		});

	}

	public JoinSessionWS(
	  ActorRef out,
	  String userName,
	  ActorRef joinSessionPresenter) throws JsonProcessingException {
		this.userName = userName;
		this.joinSessionPresenter = joinSessionPresenter;
		receive(ReceiveBuilder
		  .match(String.class, this::receiveJsonFromSocket)
		  .match(
		    WorkspaceAssignementViewModel.class,
		    this::receiveWorkspaceAssignementsViewModel)
		  .match(ChatViewModel.class, this::receiveChatViewModel)
		  .build());

		subscribeToPresenter();

		this.out = out;
		this.userName = userName;
		objectMapper = new ObjectMapper();

	}

	private void subscribeToPresenter() {
		this.joinSessionPresenter.tell(new Subscribe(userName), self());
	}

	public void receiveJsonFromSocket(String json)
	  throws JsonParseException,
	  JsonMappingException,
	  IOException {
		Logger.info("Received a message:" + json);
		JsonNode jsonNode = objectMapper.readTree(json);
		String topic = jsonNode.get("topic").asText();
		logger.info("Topic is :" + topic);
		if (topic.equals("select")) {
			Event readValue;
			readValue = objectMapper.readValue(json, Event.class);
			// sessionActor.tell(readValue, self());
		} else if (topic.equals("chatMessage")) {
			String message = jsonNode.get("message").asText();
			ChatMessage chatLine = new ChatMessage(userName, message);
			// sessionActor.tell(chatLine, self());
		}

	}

	public void receiveWorkspaceAssignementsViewModel(
	  WorkspaceAssignementViewModel workspaceAssignementViewModel)
	  throws JsonProcessingException {
		logger.info("Received a workspaceAssignementViewModel "
		    + workspaceAssignementViewModel.workspaceAssignements.size());
		String sectorsAsJson = objectMapper
		  .writeValueAsString(workspaceAssignementViewModel);
		out.tell(sectorsAsJson, self());
	}

	public void receiveChatViewModel(ChatViewModel chatViewModel)
	  throws JsonProcessingException {
		logger.info("Received a chat view model ");
		String chatViewModelAsJson = objectMapper.writeValueAsString(chatViewModel);
		out.tell(chatViewModelAsJson, self());
	}

	public static class PropCreater {
		private final String userName;

		public PropCreater(String userName) {
			this.userName = userName;
		}

		public Props props(ActorRef out) {
			return Props.create(JoinSessionWS.class, out, userName);
		}

	}

	public static class Event {
		public String topic;
		public String sector;
		public String userName;

		public Event(String workspace, String userName) {
			this.sector = workspace;
			this.userName = userName;
			topic = "event";
		}
	}

}
