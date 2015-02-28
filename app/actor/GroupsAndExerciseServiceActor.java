package actor;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Service to retrieve exercises and groups, that are valid for a user.
 * Currently a stupid dummy Messages: {@link Request} : Request groups and
 * services for a user, replied with {@link GroupsAndExercises}.
 * {@link GroupsAndExercises} : A list of groups and belonging exercises.
 *
 */
public class GroupsAndExerciseServiceActor extends AbstractActor {

	private ExecutionContextExecutor dispatcher;

	public static Props props() {
		return Props.create(GroupsAndExerciseServiceActor.class);
	}

	public GroupsAndExerciseServiceActor() {
		dispatcher = context().system().dispatcher();
		receive(ReceiveBuilder.match(Request.class, this::receiveRequest).build());
	}

	// Overengineered for testing and demonstration purposed. 
	public void receiveRequest(Request request) {
		Future<GroupsAndExercises> f = future(() -> {
			return getExerciseData(request.userName);
		}, dispatcher);
		pipe(f, dispatcher).to(sender());
	}

	public static Request request(String userName) {
		Request request = new Request();
		request.userName = userName;
		return request;
	}

	/**
	 * Lets assume this takes a long time, aka getting it from atportal.
	 * 
	 * @param userName
	 * @return
	 */
	private GroupsAndExercises getExerciseData(String userName) {
		HashMap<String, List<String>> groups = new HashMap<String, List<String>>();
		List<String> exer1 = Arrays.asList("WURL 1", "WURH 3", "WURH 2");
		List<String> exer2 = Arrays.asList("DZN 2", "AFE 3");
		List<String> exer3 = Arrays.asList("AGE 3", "ETG 3");
		groups.put("Group 1", exer1);
		groups.put("Group 2", exer2);
		groups.put("Group 3", exer3);
		GroupsAndExercises groupsAndExercises = new GroupsAndExercises();
		groupsAndExercises.groups = groups;
		return groupsAndExercises;
	}

	public static class Request {
		String userName;
	}

	public static class GroupsAndExercises {
		Map<String, List<String>> groups;
	}

}
