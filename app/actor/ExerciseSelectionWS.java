package actor;

import java.io.IOException;

import play.Logger;
import viewmodels.exerciseselect.ExercisesViewModel;
import viewmodels.exerciseselect.StartExercise;
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
    ActorRef sessionManager,
    ActorRef groupsAndServicesService)
  {
    return Props.create(
      ExerciseSelectionWS.class,
      out,
      userName,
      sessionManager,
      groupsAndServicesService);
  }

  private final ActorRef out;
  private final String userName;
  private final ActorRef sessionManager;
  private final ActorRef groupsAndServices;

  public ExerciseSelectionWS(
    ActorRef out,
    String userName,
    ActorRef sessionManager,
    ActorRef groupsAndServices) throws JsonProcessingException
  {
    Logger.info("ExerciseSelectionWebSocketHandler created");
    this.out = out;
    this.userName = userName;
    this.groupsAndServices = groupsAndServices;
    this.sessionManager = sessionManager;

    objectMapper = new ObjectMapper();
    requestGroupsAndServices();
    receive(ReceiveBuilder
      .match(String.class, this::receiveJsonFromSocket)
      .match(ExercisesViewModel.class, this::forwardAsJason)
      .build());
  }

  public void receiveJsonFromSocket(String json)
    throws JsonParseException,
    JsonMappingException,
    IOException
  {
    Logger.info("Received a message:" + json);
    StartExercise startExercise;
    startExercise = objectMapper.readValue(json, StartExercise.class);
    startExercise.ownerName = userName;
    Logger.info("Sending message to :" + sessionManager);

    sessionManager.tell(startExercise, self());

  }

  private void requestGroupsAndServices()
  {
    ExercisesRequest request = ExerciseService.createRequest(userName);
    groupsAndServices.tell(request, self());
  }

  private void forwardAsJason(ExercisesViewModel exercisesViewModel)
    throws JsonProcessingException
  {
    String exerciseViewModelAsJson = objectMapper
      .writeValueAsString(exercisesViewModel);
    out.tell(exerciseViewModelAsJson, self());
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
