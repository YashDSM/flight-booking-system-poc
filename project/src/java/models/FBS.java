package models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FBS {

	private static final Logger LOGGER = Logger.getLogger(FBS.class.getName());

	public ArrayList<Features> populateFeatures(Connection con) {
		ArrayList<Features> features = new ArrayList<Features>();

		String sql = "SELECT * FROM Features ORDER BY type";

		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				boolean isChanged = rs.getBoolean("isChanged");

				int type = rs.getInt("type");

				double seatPitch = rs.getDouble("seatPitch");
				double seatWidth = rs.getDouble("seatWidth");

				double newSeatPitch = rs.getDouble("newSeatPitch");
				double newSeatWidth = rs.getDouble("newSeatWidth");

				String videoType = rs.getString("videoType");
				String newVideoType = rs.getString("newVideoType");

				String seatType = rs.getString("seatType");
				String newSeatType = rs.getString("newSeatType");

				String powerType = rs.getString("powerType");
				String newPowerType = rs.getString("newPowerType");

				String wifi = rs.getString("wifi");
				String newWifi = rs.getString("newWifi");

				String specialFood = rs.getString("specialFood");
				String newSpecialFood = rs.getString("newSpecialFood");

				int price = rs.getInt("price");
				int newPrice = rs.getInt("newPrice");

				features.add(new Features(price, newPrice, type, isChanged, seatPitch, seatWidth, newSeatPitch,
						newSeatWidth, videoType, newVideoType, seatType, newSeatType, powerType, newPowerType, wifi,
						newWifi, specialFood, newSpecialFood));
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Could not populate flight features.", ex);
		}

		return features;
	}

	public ArrayList<Customer> populateCustomers(Connection con) {
		ArrayList<Customer> customers = new ArrayList<Customer>();

		String sql = "SELECT name, email FROM Customers";

		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String name = rs.getString("name");
				String email = rs.getString("email");

				customers.add(new Customer(name, email, null));
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Could not populate customers.", ex);
		}

		return customers;
	}

	public ArrayList<Flight> getAllFlights(Connection con, ArrayList<Customer> customers,
			ArrayList<Features> features) {

		ArrayList<Flight> flights = new ArrayList<Flight>();

		String flightSql = "SELECT * FROM Flights";

		try (Statement flightStatement = con.createStatement();
				ResultSet flightResult = flightStatement.executeQuery(flightSql)) {

			while (flightResult.next()) {
				boolean isChanged = flightResult.getBoolean("isChanged");

				String flightName = flightResult.getString("flightName");

				int totalSeats = flightResult.getInt("totalSeats");
				int currentSeats = flightResult.getInt("currentSeats");

				String departureCity = flightResult.getString("departureCity");
				String arrivalCity = flightResult.getString("arrivalCity");

				Date departureDate = flightResult.getDate("departureDate");
				Date arrivalDate = flightResult.getDate("arrivalDate");

				int economySeats = flightResult.getInt("economySeats");
				int businessSeats = flightResult.getInt("businessSeats");
				int firstSeats = flightResult.getInt("firstSeats");

				int oldESeats = flightResult.getInt("oldESeats");
				int oldBSeats = flightResult.getInt("oldBSeats");
				int oldFSeats = flightResult.getInt("oldFSeats");
				int oldTSeats = flightResult.getInt("oldTSeats");

				Flight flight = new Flight(isChanged, oldESeats, oldBSeats, oldFSeats, oldTSeats, flightName, null,
						totalSeats, currentSeats, departureCity, arrivalCity, departureDate, arrivalDate, economySeats,
						businessSeats, firstSeats);

				flights.add(flight);

				ArrayList<Seat> seats = new ArrayList<Seat>();

				String seatSql = "SELECT seatNumber, customerEmail, featureType " + "FROM Seats "
						+ "WHERE flightName = ? " + "ORDER BY seatNumber";

				try (PreparedStatement seatStatement = con.prepareStatement(seatSql)) {

					seatStatement.setString(1, flightName);

					try (ResultSet seatResult = seatStatement.executeQuery()) {

						while (seatResult.next()) {
							int seatNumber = seatResult.getInt("seatNumber");

							String customerEmail = seatResult.getString("customerEmail");

							String featureType = seatResult.getString("featureType");

							Customer customer = null;

							for (Customer candidate : customers) {
								if (customerEmail != null && candidate.getEmail().equals(customerEmail)) {
									customer = candidate;
									break;
								}
							}

							Features seatFeatures = null;

							if ("Economy".equals(featureType) && features.size() > 0) {
								seatFeatures = features.get(0);
							} else if ("Business".equals(featureType) && features.size() > 1) {
								seatFeatures = features.get(1);
							} else if ("First Class".equals(featureType) && features.size() > 2) {
								seatFeatures = features.get(2);
							}

							Seat seat = new Seat(seatNumber, flight, seatFeatures, customer);

							seats.add(seat);

							if (customer != null) {
								customer.setSeat(seat);
							}
						}
					}
				}

				flight.setSeats(seats);
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Could not populate flights.", ex);
		}

		return flights;
	}

	// ----------------- Methods exposed to web services -----------------

	public static int getPrice(String origin, String destination) {
		String flightSql = "SELECT 1 " + "FROM Flights " + "WHERE departureCity = ? " + "AND arrivalCity = ? "
				+ "AND economySeats > 0 " + "LIMIT 1";

		String priceSql = "SELECT price FROM Features WHERE type = 0";

		try (Connection con = Database.getConnection();
				PreparedStatement flightStatement = con.prepareStatement(flightSql)) {

			flightStatement.setString(1, origin);
			flightStatement.setString(2, destination);

			try (ResultSet flightResult = flightStatement.executeQuery()) {

				if (!flightResult.next()) {
					return 0;
				}
			}

			try (PreparedStatement priceStatement = con.prepareStatement(priceSql);
					ResultSet priceResult = priceStatement.executeQuery()) {

				return priceResult.next() ? priceResult.getInt("price") : 0;
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Could not retrieve the flight price.", ex);
			return 0;
		}
	}

	public static int getSeats(String flightName, String date) {
		String sql = "SELECT currentSeats " + "FROM Flights " + "WHERE flightName = ? "
				+ "AND CAST(departureDate AS DATE) = ?";

		SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

		inputFormat.setLenient(false);

		try {
			java.util.Date parsedDate = inputFormat.parse(date);
			java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());

			try (Connection con = Database.getConnection(); PreparedStatement statement = con.prepareStatement(sql)) {

				statement.setString(1, flightName);
				statement.setDate(2, sqlDate);

				try (ResultSet result = statement.executeQuery()) {

					return result.next() ? result.getInt("currentSeats") : 0;
				}
			}
		} catch (ParseException ex) {
			LOGGER.log(Level.WARNING, "Could not parse flight date: " + date, ex);
			return 0;
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Could not retrieve available seats.", ex);
			return 0;
		}
	}

	// ----------------- City autocomplete -----------------

	public ArrayList<String> getCities(Connection con, String cityName) {

		ArrayList<String> cities = new ArrayList<String>();

		String sql = "SELECT cityName " + "FROM Cities " + "WHERE LOWER(cityName) LIKE LOWER(?) " + "ORDER BY cityName";

		try (PreparedStatement statement = con.prepareStatement(sql)) {

			statement.setString(1, cityName + "%");

			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					cities.add(rs.getString("cityName"));
				}
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, "Could not retrieve matching cities.", ex);
		}

		return cities;
	}
}