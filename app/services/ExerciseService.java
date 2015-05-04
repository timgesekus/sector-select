package services;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;
import exercises.messages.Exercises;
import exercises.messages.Exercises.ExercisesResponse;
import exercises.messages.Exercises.ExercisesResponse.Exercise;
import exercises.messages.Exercises.RequestExercises;
import play.Logger;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Service to retrieve exercises that are valid for a user. Messages:
 * {@link ExercisesRequest} : Request groups and services for a user, replied
 * with {@link Exercises}. : All exercises belonging to the user.
 *
 */
public class ExerciseService extends AbstractActor
{

  private final ExecutionContextExecutor dispatcher;

  public static Props props()
  {
    return Props.create(ExerciseService.class);
  }

  public ExerciseService()
  {
    dispatcher = context().system().dispatcher();
    receive(ReceiveBuilder
      .match(RequestExercises.class, this::exerciseRequest)
      .matchAny(o -> Logger.info("received unknown message" + o))
      .build());
  }

  // Overengineered for testing and demonstrating how not to block on long
  // running tasks.
  public void exerciseRequest(RequestExercises requestExercises)
  {
    Logger.info("Receive exercise request");
    Future<ExercisesResponse> f = future(() -> {
      return presentExercises(requestExercises.getUserName());
    }, dispatcher);
    pipe(f, dispatcher).to(sender());
  }

  public static ExercisesRequest createRequest(String userName)
  {
    ExercisesRequest request = new ExercisesRequest();
    request.userName = userName;
    return request;
  }

  /**
   * Lets assume this takes a long time, aka getting it from atportal.
   *
   * @param userName
   * @return
   */
  private ExercisesResponse presentExercises(String userName)
  {
    ExercisesResponse.Builder exercisesBuilder = ExercisesResponse.newBuilder();

    Exercise.Builder exerciseBuilder = Exercise.newBuilder();

    exerciseBuilder.setExerciseId("ex-1");
    exerciseBuilder.setExerciseName("VT2002");
    exercisesBuilder.addExercises(exerciseBuilder.build());

    exerciseBuilder.setExerciseId("ex-2");
    exerciseBuilder.setExerciseName("VT402");
    exercisesBuilder.addExercises(exerciseBuilder.build());

    return exercisesBuilder.build();
  }

  public static class ExercisesRequest
  {
    String userName;
  }

}
