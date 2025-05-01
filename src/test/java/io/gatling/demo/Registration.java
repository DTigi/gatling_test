package io.gatling.demo;

import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class Registration extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:1080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .doNotTrackHeader("1")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:138.0) Gecko/20100101 Firefox/138.0");
  
  private Map<CharSequence, String> headers_0 = Map.ofEntries(
    Map.entry("If-Modified-Since", "Fri, 15 Dec 2023 21:35:31 GMT"),
    Map.entry("If-None-Match", "\"16e-60c932df4aec0\""),
    Map.entry("Priority", "u=0, i")
  );
  
  private Map<CharSequence, String> headers_1 = Map.ofEntries(
    Map.entry("If-Modified-Since", "Fri, 15 Dec 2023 21:35:31 GMT"),
    Map.entry("If-None-Match", "\"2c6-60c932df4aec0\""),
    Map.entry("Priority", "u=4")
  );
  
  private Map<CharSequence, String> headers_2 = Map.of("Priority", "u=4");
  
  private Map<CharSequence, String> headers_5 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary9cc4837a075520d9adbc452bf4d17fac"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );
  
  private Map<CharSequence, String> headers_6 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary5e09b71bebad82804c0c97f34cd3e85d"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );

  // кормушки
  private static final FeederBuilder <String> userDataFeeder = csv("fake_users.csv").circular();

  private ScenarioBuilder scn = scenario("Registration")
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
      // signup,
      http("Signup")
        .get("/cgi-bin/login.pl?username=&password=&getInfo=true")
        .headers(headers_2),
      pause(3),
      // signupDone,
      http("SignupDone")
        .post("/cgi-bin/login.pl")
        .headers(headers_6)
        .body(ElFileBody("signup_request_body.html"))
        .check(bodyString().saveAs("responseBody"))
        .check(substring("Thank you, <b>#{username}</b>, for registering")),
      pause(3),
      // login_after_registration,
      http("Login_after_registration")
        .get("/cgi-bin/welcome.pl?page=menus")
        .headers(headers_2)
        .resources(
          http("/cgi-bin/nav.pl?page=menu&in=home")
            .get("/cgi-bin/nav.pl?page=menu&in=home")
            .headers(headers_2),
          http("/cgi-bin/login.pl?intro=true")
            .get("/cgi-bin/login.pl?intro=true")
            .headers(headers_2)
            .check(substring("Welcome, <b>#{username}</b>, to the Web Tours reservation"))
        ),
      pause(3),
      // signoff,
      http("SignOff")
        .get("/cgi-bin/welcome.pl?signOff=1")
        .headers(headers_2)
        .resources(
          http("/cgi-bin/nav.pl?in=home")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
        )
    )

    .exec(session -> {
      String responseBody = session.getString("responseBody");
      System.out.println("Response body:" + responseBody);
      return session;
    });

  {
	  setUp(scn.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
  }
}
