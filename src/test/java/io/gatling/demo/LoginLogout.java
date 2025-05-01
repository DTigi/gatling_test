package io.gatling.demo;

import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class LoginLogout extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:1080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .doNotTrackHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:137.0) Gecko/20100101 Firefox/137.0");
  
  private Map<CharSequence, String> headers_0 = Map.ofEntries(
    Map.entry("If-Modified-Since", "Fri, 15 Dec 2023 21:35:31 GMT"),
    Map.entry("If-None-Match", "\"16e-60c932df4aec0\""),
    Map.entry("Priority", "u=0, i"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_1 = Map.ofEntries(
    Map.entry("If-Modified-Since", "Fri, 15 Dec 2023 21:35:31 GMT"),
    Map.entry("If-None-Match", "\"2c6-60c932df4aec0\""),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_2 = Map.ofEntries(
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_4 = Map.ofEntries(
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_7 = Map.ofEntries(
    Map.entry("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5"),
    Map.entry("Priority", "u=5, i")
  );

  // кормушки
  private static final FeederBuilder <String> userDataFeeder = csv("fake_users.csv").random();

  private ScenarioBuilder scn = scenario("RecordedSimulation")
    .feed(userDataFeeder)
    .exec(
      // homepage,
      http("Homepage")
        .get("/WebTours/")
        .headers(headers_0),
      http("/WebTours/header.html")
        .get("/WebTours/header.html")
        .headers(headers_1),
      http("/cgi-bin/welcome.pl?signOff=true")
        .get("/cgi-bin/welcome.pl?signOff=true")
        .headers(headers_2)
        .check(substring("A Session ID has been created and loaded into a cookie called MSO")),
      http("/cgi-bin/nav.pl?in=home")
        .get("/cgi-bin/nav.pl?in=home")
        .headers(headers_2)
        .check(regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession")),
      pause(3),
      // login,
      http("Login")
        .post("/cgi-bin/login.pl")
        .headers(headers_4)
        .formParam("userSession", "#{userSession}")
        .formParam("username", "jojo")
        .formParam("password", "bean")
        .formParam("login.x", "56")
        .formParam("login.y", "8")
        .formParam("JSFormSubmit", "off")
        .check(substring("User password was correct"))
        .resources(
          http("/cgi-bin/nav.pl?page=menu&in=home")
            .get("/cgi-bin/nav.pl?page=menu&in=home")
            .headers(headers_2),
          http("/cgi-bin/login.pl?intro=true")
            .get("/cgi-bin/login.pl?intro=true")
            .headers(headers_2),
          http("/WebTours/images/flights.gif")
            .get("/WebTours/images/flights.gif")
            .headers(headers_7),
          http("/WebTours/images/in_home.gif")
            .get("/WebTours/images/in_home.gif")
            .headers(headers_7),
          http("/WebTours/images/itinerary.gif")
            .get("/WebTours/images/itinerary.gif")
            .headers(headers_7),
          http("/WebTours/images/signoff.gif")
            .get("/WebTours/images/signoff.gif")
            .headers(headers_7)
        ),
      pause(3),
      // logout,
      http("SignOff")
        .get("/cgi-bin/welcome.pl?signOff=1")
        .headers(headers_2)
        .check(substring("<!-- \n" +
                " A Session ID has been created and loaded into a cookie called MSO.\n" +
                " Also, the server options have been loaded into the cookie called\n" +
                " MSO as well.  The server options can be set via the Admin page.\n" +
                " --->"))
        .resources(
          http("/cgi-bin/nav.pl?in=home")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
        )
    );

  {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
