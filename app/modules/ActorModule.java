package modules;

import play.Logger;
import play.libs.Akka;
import actor.SessionManager;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class ActorModule extends AbstractModule {

	@Override
	protected void configure() {
	}

	@Provides
	@Named("SessionManager")
	ActorRef provideSessionManager() {
		Logger.info("Creating session manager");
		Props props = SessionManager.props();
		return Akka.system().actorOf(props, "SessionManager");
	}
}
