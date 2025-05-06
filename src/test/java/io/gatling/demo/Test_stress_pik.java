package io.gatling.demo;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class Test_stress_pik extends Simulation {

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

    private Map<CharSequence, String> headers_5 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary91deba9b6ede90d17894f4067bbb453f"),
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

//  private ScenarioBuilder scn = scenario("BuyTick")
    ChainBuilder DataPrepare = exec(
        feed(userDataFeeder)
        .feed(citiesDataFeeder)
        .exec(session -> {
          // генерируем необходимые параметры
          LocalDate today = LocalDate.now();
          LocalDate tomorrow = today.plusDays(1);
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

          // случайные параметры
          String seatPref = List.of("Aisle", "Window", "None")
                  .get(ThreadLocalRandom.current().nextInt(3));
          String seatType = List.of("First", "Business", "Coach")
                  .get(ThreadLocalRandom.current().nextInt(3));
          int numPassengers = ThreadLocalRandom.current().nextInt(1, 6);
          // Записываем в сессию
          return session
                  .set("departDate", today.format(formatter))
                  .set("returnDate", tomorrow.format(formatter))
                  .set("seatPref", seatPref)
                  .set("seatType", seatType)
                  .set("numPassengers", numPassengers);
        })
        );

    ChainBuilder homepage = exec(
      http("HomePage")
        .get("/WebTours/")
        .headers(headers_0),
          http("/WebTours/header.html")
            .get("/WebTours/header.html")
            .headers(headers_1),
          http("/cgi-bin/welcome.pl?signOff=1")
            .get("/cgi-bin/welcome.pl?signOff=1")
            .headers(headers_2)
              .check(substring("A Session ID has been created and loaded into a cookie called MSO")),
          http("/cgi-bin/nav.pl?in=home")
            .get("/cgi-bin/nav.pl?in=home")
            .headers(headers_2)
            .check(regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"))
      );

      ChainBuilder login = exec(
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
          http("/cgi-bin/nav.pl?page=menu&in=home")
            .get("/cgi-bin/nav.pl?page=menu&in=home")
            .headers(headers_2),
          http("/cgi-bin/login.pl?intro=true")
            .get("/cgi-bin/login.pl?intro=true")
            .headers(headers_2)
        )
              );
    ChainBuilder flights = exec(
      http("Flights")
        .get("/cgi-bin/welcome.pl?page=search")
        .headers(headers_2)
          .check(substring("User has returned to the search page.  Since user has already logged on,\n" +
              " we can give them the menu in the navbar."))
        .resources(
          http("/cgi-bin/nav.pl?page=menu&in=flights")
            .get("/cgi-bin/nav.pl?page=menu&in=flights")
            .headers(headers_2),
          http("/cgi-bin/reservations.pl?page=welcome")
            .get("/cgi-bin/reservations.pl?page=welcome")
            .headers(headers_2)
        )
            );

    ChainBuilder find_flights = exec(
      http("FindFlight")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_10)
        .body(ElFileBody("find_flight_request_body.html"))
          .check(regex("name=\"outboundFlight\" value=\"(.+?)\"").saveAs("outboundFlight"))
          .check(substring("Flight departing from <B>#{departCity}</B> to <B>#{arriveCity}</B>"))
            );

    ChainBuilder payment_details = exec(
      http("PaymentDetail")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_11)
        .body(ElFileBody("payment_detail_request_body.html"))
        .check(substring("name=\"outboundFlight\" value=\"#{outboundFlight}"))
//            .check(bodyString().saveAs("responseBody")),
    );

    ChainBuilder invoice = exec(
      http("Invoice")
        .post("/cgi-bin/reservations.pl")
        .headers(headers_12)
        .body(ElFileBody("invoice_request_body.html"))
        .check(regex(".+?#{departCity}.+?for.+?#{arriveCity}"))
            );

    ChainBuilder itinerary = exec(
      http("Itinerary")
        .get("/cgi-bin/welcome.pl?page=itinerary")
        .headers(headers_2)
        .check(substring("User wants the intineraries.  Since user has already logged on,\n" +
                  " we can give them the menu in the navbar."))
        .resources(
          http("/cgi-bin/nav.pl?page=menu&in=itinerary")
            .get("/cgi-bin/nav.pl?page=menu&in=itinerary")
            .headers(headers_2),
          http("/cgi-bin/itinerary.pl")
            .get("/cgi-bin/itinerary.pl")
            .headers(headers_2)
            .check(regex("name=\"flightID\".*?value=\"(.*?)\"").findAll().saveAs("flightIDs"))
        )
            );

    ChainBuilder signoff = exec(
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

    ChainBuilder delete_itinerary = exec(
    // формируем тело для удаления
      exec(session -> {
        List<String> flightIDs = session.getList("flightIDs");

        if (flightIDs == null || flightIDs.isEmpty()) {
            System.err.println("❌ No flightIDs found — canceling session.");
            return session.markAsFailed();
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(flightIDs.size());
        String selectedFlightID = flightIDs.get(randomIndex);

        String boundary = "----geckoformboundary2b1a2987ebe1b21a2c5ff8a4a8e1f20";
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
    }),

    // deleteItinerary,
    http("DeleteItinerary")
        .post("/cgi-bin/itinerary.pl")
        .headers(headers_11)
        //.body(ElFileBody("delete_request_body.html")),
        .body(StringBody(session -> session.getString("body")))
        .check(regex("name=\"flightID\".*?value=\"(.*?)\"").findAll().saveAs("afterDeleteIDs")),// после удаления

    // Проверка после удаления
        exec(session -> {
        List<String> afterDeleteIDs = session.getList("afterDeleteIDs");
        String deletedID = session.getString("deletedFlightID");

        if (afterDeleteIDs != null && afterDeleteIDs.contains(deletedID)) {
            System.err.println("❌ Flight ID " + deletedID + " was NOT deleted!");
            return session.markAsFailed();
        }
        System.out.println("✅ Flight ID " + deletedID + " successfully deleted.");
        return session;
        })
    );

    ChainBuilder signup = exec(
            // signup,
            http("SignUp")
                .get("/cgi-bin/login.pl?username=&password=&getInfo=true")
                .headers(headers_2)
                .check(substring("Please choose a username and password combination for your account"))
    );

    ChainBuilder signup_done = exec(
            feed(userDataFeeder)
            // генерация username и password
            .exec(session -> {
                String username = UUID.randomUUID().toString().substring(0, 8);
                String password = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

                return session
                        .set("username", username)
                        .set("password", password);
            }),
            // signup_done,
            http("SignUpDone")
                .post("/cgi-bin/login.pl")
                .headers(headers_5)
                .body(ElFileBody("signup_request_body.html"))
                    .check(bodyString().saveAs("responseBody"))
                    .check(substring("Thank you, <b>#{username}</b>, for registering"))
    );

    ChainBuilder login_after_registration = exec(
            // login_after_reg,
            http("Login_after_reg")
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
                )
    );

private ScenarioBuilder scnBuyTicket = scenario("UC1_BuyTicket")
        .forever().on(
            pace(42)
            .exec(DataPrepare)
            .exec(homepage)
            .pause(2)
            .exec(login)
            .pause(2)
            .exec(flights)
            .pause(2)
            .exec(find_flights)
            .pause(2)
            .exec(payment_details)
            .pause(2)
            .exec(invoice)
            .pause(2)
            .exec(signoff)
            );

private ScenarioBuilder scnDeleteTicket = scenario("UC2_DeleteTicket")
        .forever().on(
            pace(52)
            .exec(DataPrepare)
            .exec(homepage)
            .pause(2)
            .exec(login)
            .pause(2)
            .exec(itinerary)
            .pause(2)
            .exec(delete_itinerary)
            .pause(2)
            .exec(signoff)
        );

    private ScenarioBuilder scnRegistration = scenario("UC3_Registration")
            .forever().on(
                pace(38)
                    .exec(homepage)
                    .pause(2)
                    .exec(signup)
                    .pause(2)
                    .exec(signup_done)
                    .pause(2)
                    .exec(login_after_registration)
                    );

    private ScenarioBuilder scnWithoutPayment = scenario("UC4_WithoutPayment")
            .forever().on(
                pace(64)
                    .exec(DataPrepare)
                    .exec(homepage)
                    .pause(2)
                    .exec(login)
                    .pause(2)
                    .exec(flights)
                    .pause(2)
                    .exec(find_flights)
                    .pause(2)
                    .exec(payment_details)
                    .pause(2)
                    .exec(itinerary)
            );

    private ScenarioBuilder scnShowItinerary = scenario("UC5_ShowItinerary")
            .forever().on(
                pace(84)
                    .exec(DataPrepare)
                    .exec(homepage)
                    .pause(2)
                    .exec(login)
                    .pause(2)
                    .exec(itinerary)
                    .pause(2)
                    .exec(signoff)
            );

    private ScenarioBuilder scnLoginLogout = scenario("UC6_LoginLogout")
            .forever().on(
                pace(960)
                    .exec(DataPrepare)
                    .exec(homepage)
                    .pause(2)
                    .exec(login)
                    .pause(2)
                    .exec(flights)
                    .pause(2)
                    .exec(signoff)
                 );
{
  setUp(scnBuyTicket.injectClosed(//rampConcurrentUsers(1).to(4).during(60),
                  constantConcurrentUsers(6).during(1200)),
          scnDeleteTicket.injectClosed(//rampConcurrentUsers(1).to(2).during(60),
                  constantConcurrentUsers(4).during(1200)),
          scnRegistration.injectClosed(//rampConcurrentUsers(1).to(2).during(60),
                  constantConcurrentUsers(4).during(1200)),
          scnWithoutPayment.injectClosed(//rampConcurrentUsers(1).to(4).during(60),
                  constantConcurrentUsers(6).during(1200)),
          scnShowItinerary.injectClosed(//rampConcurrentUsers(1).to(4).during(60),
                  constantConcurrentUsers(6).during(1200)),
          scnLoginLogout.injectClosed(//rampConcurrentUsers(1).to(4).during(60),
                  constantConcurrentUsers(6).during(1200))
  ).protocols(httpProtocol).maxDuration(1200);
}
}
