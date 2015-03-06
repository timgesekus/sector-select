package actor;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import viewmodels.exerciseselect.ExercisesViewModel;
import viewmodels.exerciseselect.ExercisesViewModel.Group;
import viewmodels.exerciseselect.ExercisesViewModel.Exercise;
import actor.ExerciseSelectionWebsocketHandler.SelectionEvent;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Service to retrieve exercises and groups, that are valid for a user.
 * Messages: {@link ExercisesRequest} : Request groups and services for a user,
 * replied with {@link ExerciseViewModel}. {@link ExerciseViewModel} : All
 * exercises belonging to the user.
 *
 */
public class ExerciseService extends AbstractActor {

	private final ExecutionContextExecutor dispatcher;

	public static Props props() {
		return Props.create(ExerciseService.class);
	}

	public ExerciseService() {
		dispatcher = context().system().dispatcher();
		receive(ReceiveBuilder
				.match(ExercisesRequest.class, this::receiveRequest)
				.match(SelectionEvent.class, this::receiveSelect)
				.matchAny(o -> Logger.info("received unknown message" + o))
				.build());
	}

	// Overengineered for testing and demonstration purposed.
	public void receiveRequest(ExercisesRequest exercisesRequest) {
		Logger.info("Receive exercise request");
		Future<ExercisesViewModel> f = future(() -> {
			return presentExercises(exercisesRequest.userName);
		}, dispatcher);
		pipe(f, dispatcher).to(sender());
	}

	public void receiveSelect(SelectionEvent selectionEvent) {
		if (selectionEvent.type.equals("group")) {

		}
	}

	public static ExercisesRequest createRequest(String userName) {
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
	private ExercisesViewModel presentExercises(String userName) {
		List<Group> groups = new ArrayList<>();

		Group group = new Group();
		group.name = "Week 1";
		group.id = 1;
		Exercise exercise = new Exercise();
		exercise.name = "VT2002";
		exercise.id = 1;
		group.exercises = Arrays.asList(exercise);

		groups.add(group);
		ExercisesViewModel exercises = new ExercisesViewModel(groups);
		return exercises;
	}

	public static class ExercisesRequest {
		String userName;
	}

}
