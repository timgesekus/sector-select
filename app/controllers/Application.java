package controllers;

import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.data.validation.Constraints;

public class Application extends Controller {
	static Form<User> userForm = Form.form(User.class);

	@SubjectPresent
	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}

	public static Result login() {
		return ok(views.html.login.render(userForm));
	}

	public static Result logout() {
		session().remove("userName");
		return redirect(routes.Application.login());

	}

	public static Result loginSubmit() {
		Logger.info(request().body().toString());
		Form<User> filledUserForm = userForm.bindFromRequest();
		if (filledUserForm.hasErrors()) {
			return badRequest("Somethings missing");
		} else {
			User user = filledUserForm.get();
			session("userName", user.getUserName());
			return redirect(routes.Application.index());
		}
	}

	public static class User {
		@Constraints.Required
		private String userName;
		@Constraints.Required
		private String password;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}
}
