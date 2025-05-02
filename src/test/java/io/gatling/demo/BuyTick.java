package io.gatling.demo;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class BuyTick extends Simulation {

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
  
  private Map<CharSequence, String> headers_4 = Map.ofEntries(
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );
  
  private Map<CharSequence, String> headers_10 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary4f9bb9813126c83459047e11f152c405"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );
  
  private Map<CharSequence, String> headers_11 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary2b1a2987ebe1b21a2c5ff8a4a8e1f20"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );
  
  private Map<CharSequence, String> headers_12 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary5aa2717916976d55e0f37af98c5c81b"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4")
  );

    // кормушки
    private static final FeederBuilder <String> userDataFeeder = csv("fake_users.csv").circular();
    private static final FeederBuilder <String> citiesDataFeeder = csv("routes.csv").circular();

  private ScenarioBuilder scn = scenario("BuyTick")
      .feed(userDataFeeder)
      .feed(citiesDataFeeder)
    .exec(
      // homepage,
      http("HomePage")
        .get("/WebTours/")
        .headers(headers_0),
          http("request_1")
            .get("/WebTours/header.html")
            .headers(headers_1),
          http("request_2")
            .get("/cgi-bin/welcome.pl?signOff=1")
            .headers(headers_2)
              .check(substring("A Session ID has been created and loaded into a cookie called MSO")),
          http("request_3")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
            .check(regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession")),
      pause(2),
      // login,
      http("LogIn")
        .post("/cgi-bin/login.pl")
        .headers(headers_4)
        .formParam("userSession", "#{userSession}")
        .formParam("username", "#{username}")
        .formParam("password", "#{password}")
        .formParam("login.x", "64")
        .formParam("login.y", "11")
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
      pause(2),
      // flights,
      http("Flights")
        .get("/cgi-bin/welcome.pl?page=search")
        .headers(headers_2)
          .check(substring("User has returned to the search page.  Since user has already logged on,\n" +
              " we can give them the menu in the navbar."))
        .resources(
          http("request_8")
            .get("/cgi-bin/nav.pl?page=menu&in=flights")
            .headers(headers_2),
          http("request_9")
            .get("/cgi-bin/reservations.pl?page=welcome")
            .headers(headers_2)
        ),
      pause(2),
      // findflight,
      http("FindFlight")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_10)
        .body(ElFileBody("0010_request.html"))
          .check(regex("name=\"outboundFlight\" value=\"(.+?)\"").saveAs("outboundFlight"))
          .check(substring("Flight departing from <B>#{departCity}</B> to <B>#{arriveCity}</B>")),
      pause(2),
      // paymentdetail,
      http("PaymentDetail")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_11)
        .body(ElFileBody("0011_request.html"))
            .check(substring("name=\"outboundFlight\" value=\"#{outboundFlight}")),
//            .check(bodyString().saveAs("responseBody")),
      pause(2),
      // invoice,
      http("Invoice")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_12)
        .body(ElFileBody("0012_request.html"))
            .check(regex(".+?#{departCity}.+?for.+?#{arriveCity}")),
      pause(2),
      // itinerary,
      http("Itinerary")
        .get("/cgi-bin/welcome.pl?page=itinerary")
        .headers(headers_2)
          .check(substring("User wants the intineraries.  Since user has already logged on,\n" +
                  " we can give them the menu in the navbar."))
        .resources(
          http("request_14")
            .get("/cgi-bin/nav.pl?page=menu&in=itinerary")
            .headers(headers_2),
          http("request_15")
            .get("/cgi-bin/itinerary.pl")
            .headers(headers_2)
        ),
      pause(2),
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
          http("request_17")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
        )
    )
/////// Логи
        .exec(session -> {
            System.out.println("Search params: " +
                "depart=" + session.getString("departCity") +
                ", arrive=" + session.getString("arriveCity") + "\n" +
                "Current outboundFlight: " + session.getString("outboundFlight") + "\n" +
                "Response body:" + session.getString("responseBody"));
            return session;
        });
//        .exec(session -> {
//            try {
//                String content = new String(Files.readAllBytes(
//                        Paths.get(getClass().getResource("/payment_details_body.html").toURI())));
//                System.out.println("File content: " + content);
//                }
//            catch (Exception e) {
//                System.out.println("Failed to read file: " + e.getMessage());
//                }
//            return session;
//        });

  {
	  setUp(scn.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
  }
}
