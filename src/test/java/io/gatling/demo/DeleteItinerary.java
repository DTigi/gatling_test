package io.gatling.demo;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class DeleteItinerary extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:1080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0");
  
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
  
  private Map<CharSequence, String> headers_11 = Map.ofEntries(
    Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary222472d79676a793e1f78f0d2856ffc0"),
    Map.entry("Origin", "http://localhost:1080"),
    Map.entry("Priority", "u=4"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );

  // кормушки
  private static final FeederBuilder <String> userDataFeeder = csv("fake_users.csv").circular();

  private ScenarioBuilder scn = scenario("DeleteItinerary")
    .feed(userDataFeeder)
    .exec(
      // homepage,
      http("HomePage")
        .get("/WebTours/")
        .headers(headers_0),
          http("WebTours/header.html")
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

      pause(2),
      // login,
      http("LogIn")
        .post("/cgi-bin/login.pl")
        .headers(headers_4)
        .formParam("userSession", "#{userSession}")
        .formParam("username", "#{username}")
        .formParam("password", "#{password}")
        .formParam("login.x", "55")
        .formParam("login.y", "10")
        .formParam("JSFormSubmit", "off")
        .check(substring("User password was correct"))
        .resources(
          http("/cgi-bin/nav.pl?page=menu&in=home")
            .get("/cgi-bin/nav.pl?page=menu&in=home")
            .headers(headers_2),
          http("/cgi-bin/login.pl?intro=true")
            .get("/cgi-bin/login.pl?intro=true")
            .headers(headers_2)
        ),
      pause(2),
      // itinerary,
      http("Itinerary")
        .get("/cgi-bin/welcome.pl?page=itinerary")
        .headers(headers_2)
        .check(substring("User wants the intineraries.  Since user has already logged on,\n" +
                " we can give them the menu in the navbar."))
        .resources(
          http("/cgi-bin/nav.pl?page=menu&in=itinerary")
            .get("/cgi-bin/nav.pl?page=menu&in=itinerary")
            .headers(headers_2),
          http("/WebTours/images/in_itinerary.gif")
            .get("/WebTours/images/in_itinerary.gif")
            .headers(headers_9),
          http("/cgi-bin/itinerary.pl")
            .get("/cgi-bin/itinerary.pl")
            .headers(headers_2)
            .check(regex("name=\"flightID\".*?value=\"(.*?)\"").findAll().saveAs("flightIDs"))
        )
    )
          // формируем тело для удаления
          .exec(session -> {
            List<String> flightIDs = session.getList("flightIDs");

            if (flightIDs == null || flightIDs.isEmpty()) {
              System.err.println("❌ No flightIDs found — canceling session.");
              return session.markAsFailed();
            }

            int randomIndex = ThreadLocalRandom.current().nextInt(flightIDs.size());
            String selectedFlightID = flightIDs.get(randomIndex);

            String boundary = "----geckoformboundary222472d79676a793e1f78f0d2856ffc0";
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < flightIDs.size(); i++) {
              String flightID = flightIDs.get(i);
              sb.append("--").append(boundary).append("\r\n")
                      .append("Content-Disposition: form-data; name=\"flightID\"\r\n\r\n")
                      .append(flightID).append("\r\n");

              if (i == randomIndex) {
                sb.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"").append(i + 1).append("\"\r\n\r\n")
                        .append("on\r\n");
              }
            }

            sb.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"removeFlights.x\"\r\n\r\n")
                    .append("48\r\n")
                    .append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"removeFlights.y\"\r\n\r\n")
                    .append("10\r\n");

            for (int i = 1; i <= flightIDs.size(); i++) {
              sb.append("--").append(boundary).append("\r\n")
                      .append("Content-Disposition: form-data; name=\".cgifields\"\r\n\r\n")
                      .append(i).append("\r\n");
            }

            sb.append("--").append(boundary).append("--\r\n");

            return session
                    .set("body", sb.toString())
                    .set("boundary", boundary)
                    .set("deletedFlightID", selectedFlightID);
          })

      .exec(
      pause(2),
      // deleteItinerary,
      http("DeleteItinerary")
        .post("/cgi-bin/itinerary.pl")
        .headers(headers_11)
        //.body(ElFileBody("delete_request_body.html")),
        .body(StringBody(session -> session.getString("body")))
        .check(regex("name=\"flightID\".*?value=\"(.*?)\"").findAll().saveAs("afterDeleteIDs")) // после удаления
      )

          // Проверка после удаления
          .exec(session -> {
            List<String> afterDeleteIDs = session.getList("afterDeleteIDs");
            String deletedID = session.getString("deletedFlightID");

            if (afterDeleteIDs != null && afterDeleteIDs.contains(deletedID)) {
              System.err.println("❌ Flight ID " + deletedID + " was NOT deleted!");
              return session.markAsFailed();
            }

            System.out.println("✅ Flight ID " + deletedID + " successfully deleted.");
            return session;
          })

      .exec(
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
          http("/cgi-bin/nav.pl?in=home")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
        )
    );

  {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
