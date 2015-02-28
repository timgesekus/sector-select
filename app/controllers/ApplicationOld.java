package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class ApplicationOld extends Controller
{

  public static Result index()
  {
    return ok(indexorig.render("Your new application is ready."));
  }

}
