package io.gatling.demo;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class BuyTicket extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:1080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .doNotTrackHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:138.0) Gecko/20100101 Firefox/138.0");
  
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
  
  private Map<CharSequence, String> headers_9 = Map.ofEntries(
    Map.entry("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5"),
    Map.entry("Priority", "u=5, i")
  );
  
  private Map<CharSequence, String> headers_12 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary88bc938cc53581b7af330b67d732e27"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_13 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary451f0c1fc5650f8b42a62a985233a420"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_14 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary627c1e524e41cd7617a3069c78bb423b"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_21 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundaryde91567629dd36f3fb194c745e53bb06"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );

// кормушки
  private static final FeederBuilder <String> userDataFeeder = csv("fake_users.csv").circular();

  private ScenarioBuilder scn = scenario("BuyTicket")
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
              .headers(headers_2),
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
              .formParam("username", "#{username}")
              .formParam("password", "#{password}")
              .formParam("login.x", "56")
              .formParam("login.y", "8")
              .formParam("JSFormSubmit", "off")
              .check(substring("User password was correct"))
        .resources(
          http("request_5")
            .get("/cgi-bin/nav.pl?page=menu&in=home")
            .headers(headers_2),
          http("request_6")
            .get("/cgi-bin/login.pl?intro=true")
            .headers(headers_2)
        ),
      pause(30),
      // flights,
      http("request_7")
        .get("/cgi-bin/welcome.pl?page=search")
        .headers(headers_2)
        .resources(
          http("request_8")
            .get("/cgi-bin/nav.pl?page=menu&in=flights")
            .headers(headers_2),
          http("request_9")
            .get("/WebTours/images/in_flights.gif")
            .headers(headers_9),
          http("request_10")
            .get("/WebTours/images/home.gif")
            .headers(headers_9),
          http("request_11")
            .get("/cgi-bin/reservations.pl?page=welcome")
            .headers(headers_2)
        ),
      pause(73),
      // findFlight,
      http("request_12")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_12)
        .body(RawFileBody("io/gatling/demo/buyticket/0012_request.html")),
      pause(50),
      // paymenDetails,
      http("request_13")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_13)
        .body(RawFileBody("io/gatling/demo/buyticket/0013_request.html")),
      pause(72),
      // Invoice,
      http("request_14")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_14)
        .body(RawFileBody("io/gatling/demo/buyticket/0014_request.html")),
      pause(31),
      // showItineraty,
      http("request_15")
        .get("/cgi-bin/welcome.pl?page=itinerary")
        .headers(headers_2)
        .resources(
          http("request_16")
            .get("/cgi-bin/nav.pl?page=menu&in=itinerary")
            .headers(headers_2),
          http("request_17")
            .get("/WebTours/images/in_itinerary.gif")
            .headers(headers_9),
          http("request_18")
            .get("/cgi-bin/itinerary.pl")
            .headers(headers_2),
          http("request_19")
            .get("/WebTours/images/cancelreservation.gif")
            .headers(headers_9),
          http("request_20")
            .get("/WebTours/images/cancelallreservations.gif")
            .headers(headers_9)
        ),
      pause(38),
      // deleteOneTicket,
      http("DeleteOneTicket")
        .post("/cgi-bin/itinerary.pl")
        .headers(headers_21)
        .body(RawFileBody("io/gatling/demo/buyticket/0021_request.html")),
      pause(17),
      // signOff,
      http("request_22")
        .get("/cgi-bin/welcome.pl?signOff=1")
        .headers(headers_2)
        .resources(
          http("request_23")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
        )
    );

  {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
