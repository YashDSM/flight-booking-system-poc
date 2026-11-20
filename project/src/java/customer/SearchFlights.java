package customer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import models.FBS;
import models.Features;
import models.Flight;
import java.io.IOException;
import java.util.Date;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SearchFlights extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ArrayList<Flight> flights =

				(ArrayList<Flight>) getServletContext().getAttribute("flights");

		ArrayList<Flight> results = new ArrayList<Flight>();

		String from = request.getParameter("from");

		String to = request.getParameter("to");

		String dateText = request.getParameter("depart"); // HTML date input: yyyy-MM-dd

		String travelClass = request.getParameter("class");

		String peopleText = request.getParameter("people");

		try {

			int people = Integer.parseInt(peopleText);

			java.sql.Date requestedDate = java.sql.Date.valueOf(dateText);

			if (flights != null) {

				for (Flight flight : flights) {

					if (flight.getDepartureDate() == null) {

						continue;

					}

					boolean routeMatches =

							flight.getDepartureCity().equalsIgnoreCase(from)
									&& flight.getArrivalCity().equalsIgnoreCase(to);

					boolean dateMatches =

							flight.getDepartureDate().toLocalDate()

									.equals(requestedDate.toLocalDate());

					boolean enoughSeats;

					if ("Economy".equals(travelClass)) {

						enoughSeats = flight.getEconomySeats() >= people;

					} else if ("Business".equals(travelClass)) {

						enoughSeats = flight.getBusinessSeats() >= people;

					} else if ("First Class".equals(travelClass)) {

						enoughSeats = flight.getFirstSeats() >= people;

					} else {

						enoughSeats = false;

					}

					if (routeMatches && dateMatches && enoughSeats && !flight.isChanged && !Features.isChanged) {

						results.add(flight);

					}

				}

			}

		} catch (IllegalArgumentException ex) {

			// Invalid/missing date or passenger count: show no matching results.

			getServletContext().log("Invalid flight-search parameters.", ex);

		}

		request.setAttribute("results", results);

		request.getRequestDispatcher("ShowFlights.jsp")

				.forward(request, response);

	}
}
