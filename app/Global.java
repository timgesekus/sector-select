import modules.ActorModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {
	private static final Injector INJECTOR = createInjector();

	@Override
	public <A> A getControllerInstance(Class<A> controllerClass)
			throws Exception {
		Logger.info("Get instance called" + controllerClass.toString());

		return INJECTOR.getInstance(controllerClass);
	}

	private static Injector createInjector() {
		Logger.info("Creating injector");
		return Guice.createInjector(new ActorModule());
	}

}
