package io.gatling.demo;

import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class Register extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:1080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0");
  
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
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary91deba9b6ede90d17894f4067bbb453f"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );

  private static final FeederBuilder <String> userDataFeeder = csv("fake_users.csv").circular();

  private ScenarioBuilder scn = scenario("Register")
    .feed(userDataFeeder)
    .exec(
      // homepage,
      http("Homepage")
        .get("/WebTours/")
        .headers(headers_0)
        .resources(
          http("request_1")
            .get("/WebTours/header.html")
            .headers(headers_1),
          http("request_2")
            .get("/cgi-bin/welcome.pl?signOff=true")
            .headers(headers_2)
            .check(substring("A Session ID has been created and loaded into a cookie called MSO")),
          http("request_3")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
            .check(regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"))
        ),
      pause(2),
      // signup,
      http("SignUp")
        .get("/cgi-bin/login.pl?username=&password=&getInfo=true")
        .headers(headers_2)
        .check(substring("Please choose a username and password combination for your account")),
      pause(2),
      // signUpDone,
      http("SignUpDone")
        .post("/cgi-bin/login.pl")
        .headers(headers_5)
        .body(ElFileBody("0005_signup_request_body.html"))
        .check(bodyString().saveAs("responseBody"))
        .check(substring("Thank you, <b>#{username}</b>, for registering")),
      pause(2),
      // login_after_reg,
      http("Login_after_reg")
        .get("/cgi-bin/welcome.pl?page=menus")
        .headers(headers_2)
        .resources(
          http("request_7")
            .get("/cgi-bin/nav.pl?page=menu&in=home")
            .headers(headers_2),
          http("request_8")
            .get("/cgi-bin/login.pl?intro=true")
            .headers(headers_2)
            .check(substring("Welcome, <b>#{username}</b>, to the Web Tours reservation"))
        ),
      pause(2),
      // signOff,
      http("SignOff")
        .get("/cgi-bin/welcome.pl?signOff=1")
        .headers(headers_2)
        .check(substring("<!-- \n" +
              " A Session ID has been created and loaded into a cookie called MSO.\n" +
              " Also, the server options have been loaded into the cookie called\n" +
              " MSO as well.  The server options can be set via the Admin page.\n" +
              " --->"))
        .resources(
          http("request_10")
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
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
