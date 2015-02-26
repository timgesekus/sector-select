package security.model;

import model.AuthorisedUser;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import play.mvc.Result;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;

public class MyDeadBoltHandler extends AbstractDeadboltHandler {

	@Override
	public Promise<Result> beforeAuthCheck(Context arg0) {
		return F.Promise.pure(null);
	}

	@Override
	public Subject getSubject(Context context) {
		Session session = context.session();
		if (session.containsKey("userName")) {
			AuthorisedUser authorisedUser = new AuthorisedUser();
			authorisedUser.userName = session.get("userName");
			return (authorisedUser);
		} else {
			return (null);
		}

	}

}
