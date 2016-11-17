package de.tehame.rs;

import javax.servlet.annotation.WebServlet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("tehame")
@WebServlet("/*")
public class TehameRS extends Application {

}
