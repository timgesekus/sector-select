package actor;

import java.io.IOException;

import play.Logger;
import viewmodels.exerciseselect.ExercisesViewModel;
import actor.ExerciseService.ExercisesRequest;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExerciseSelectionWS extends AbstractActor
{

  private final ObjectMapper objectMapper;

  public static Props props(
    ActorRef out,
    String userName,
    ActorRef groupsAndServicesService)
  {
    return Props.create(
      ExerciseSelectionWS.class,
      out,
      userName,
      groupsAndServicesService);
  }

  private final ActorRef out;
  private final String userName;
  private final ActorRef groupsAndServices;

  public ExerciseSelectionWS(
    ActorRef out,
    String userName,
    ActorRef groupsAndServices) throws JsonProcessingException
  {
    Logger.info("ExerciseSelectionWebSocketHandler created");
    this.out = out;
    this.userName = userName;
    this.groupsAndServices = groupsAndServices;

    objectMapper = new ObjectMapper();
    requestGroupsAndServices();
  }

  private void requestGroupsAndServices()
  {
    ExercisesRequest request = ExerciseService.createRequest(userName);
    groupsAndServices.tell(request, self());
  }

  public static class SelectionEvent
  {
    public String topic;
    public String type;
    public String name;

    public SelectionEvent()
    {
      topic = "event";
    }
  }
}
