package modules;

import play.Logger;
import play.libs.Akka;
import actor.ExerciseService;
import actor.SessionManager;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import controllers.JoinSession;

/**
 * Guice doesn't handle java 8 very well. So we need to catch exception by
 * ourselfs. Can be removed with guice 4.0
 */
public class ActorModule extends AbstractModule {

	@Override
	protected void configure() {
	}

	@Provides
	@Singleton
	@Named("SessionManager")
	ActorRef provideSessionManager() {
		try {
			Logger.info("Creating session manager");
			Props props = SessionManager.props();
			return Akka.system().actorOf(props, "SessionManager");
		} catch (Exception e) {
			Logger.error("Failed in provide", e);
		}
		Logger.info("All is well");
		return null;
	}

	@Provides
	@Singleton
	@Named("ExerciseService")
	ActorRef provideExerciseService() {
		try {
			Logger.info("Creating exercise service");
			Props props = ExerciseService.props();
			return Akka.system().actorOf(props, "EcerciseServices");
		} catch (Exception e) {
			Logger.error("Failed in provide exercise service", e);
		}
		Logger.info("All is well");
		return null;
	}

	@Provides
	JoinSession provideSessionController(
	  @Named("SessionManager") ActorRef sessionManager) {
		try {
			Logger.info("Creating session controller");
			return new JoinSession(sessionManager);
		} catch (Exception e) {
			Logger.error("Failed in provide", e);
		}
		Logger.info("All is well");
		return null;
	}
}
