package actor;

import actor.GroupsAndExerciseServiceActor.Request;
import actor.messages.Sectors;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Logger;
import viewmodels.exerciseselect.ExerciseViewModel;
import viewmodels.exerciseselect.SelectableItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GroupListActor extends UntypedActor {

	private final ObjectMapper objectMapper;

	public static Props props(ActorRef out) {
		return Props.create(GroupListActor.class, out);
	}

	private final ActorRef out;
	private String userName;
	private ActorRef groupsAndServicesService;

	public GroupListActor(ActorRef out, String userName, ActorRef groupsAndServicesService)
	  throws JsonProcessingException {
		this.out = out;
		this.userName = userName;
		this.groupsAndServicesService = groupsAndServicesService;

		objectMapper = new ObjectMapper();
		requestGroupsAndServices();
	}

	private void requestGroupsAndServices() {
	  Request request = GroupsAndExerciseServiceActor.request(userName);
		groupsAndServicesService.tell(request, getSelf());
  }

	public void onReceive(Object message) throws Exception {
		if (message instanceof String) {
			Logger.info("Received a message:" + message);
		}
		
		if (message instanceof GroupsAndExerciseServiceActor.GroupsAndExercises ) {
			GroupsAndExerciseServiceActor.GroupsAndExercises groups = (GroupsAndExerciseServiceActor.GroupsAndExercises) message;
			present(groups);
		}
	}

	private void present(GroupsAndExerciseServiceActor.GroupsAndExercises groups)
    throws JsonProcessingException {
	  ArrayList<SelectableItem> selectableItems = new ArrayList<>();
	  for (String group : groups.groups.keySet()) {
	    SelectableItem item = new SelectableItem();
	    item.name = group;
	    selectableItems.add(item);
	  }
	  ExerciseViewModel exerciseViewModel = new ExerciseViewModel(Arrays.asList(), "Group", selectableItems);
	  String exerciseViewModelAsJson = objectMapper.writeValueAsString(exerciseViewModel);
	  out.tell(exerciseViewModelAsJson, self());
  }

	public static class PropCreater {
		private String userName;
		private ActorRef groupsAndServicesService;

		public PropCreater(String aUserName, ActorRef groupsAndServicesService) {
			userName = aUserName;
			this.groupsAndServicesService = groupsAndServicesService;
		}

		public Props props(ActorRef out) {
			return Props.create(GroupListActor.class, out, userName, groupsAndServicesService);
		}

	}

	public static class Event {
		public String topic;
		public String sector;

		public Event() {
			topic = "event";
		}
	}
}
