import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import models.Customer;
import models.Database;
import models.FBS;
import models.Features;
import models.Flight;

public class SContextListener implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(SContextListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		Connection connection = null;

		try {
			connection = Database.getConnection();
			executeSqlResource(connection, "/db/schema.sql");
			seedIfEmpty(connection);

			FBS fbs = new FBS();
			ArrayList<Features> features = fbs.populateFeatures(connection);
			ArrayList<Customer> customers = fbs.populateCustomers(connection);
			ArrayList<Flight> flights = fbs.getAllFlights(connection, customers, features);

			context.setAttribute("con", connection);
			context.setAttribute("features", features);
			context.setAttribute("fbs", fbs);
			context.setAttribute("flights", flights);
			context.setAttribute("customers", customers);

			Statement st = connection.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'");
			if (rs.next())
				LOGGER.info("H2 tables created: " + rs.getInt(1));

			LOGGER.info("H2 database initialized and application data loaded.");
		} catch (Exception ex) {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException closeException) {
					ex.addSuppressed(closeException);
				}
			}

			LOGGER.log(Level.SEVERE, "Could not initialize the H2 database.", ex);
			throw new IllegalStateException("Application startup failed during H2 initialization.", ex);
		}
	}

	private void createSchema(Connection connection) throws IOException, SQLException {

		InputStream input = getClass().getResourceAsStream("/db/schema.sql");

		if (input == null) {
			throw new IOException("Could not find /db/schema.sql on the application classpath.");
		}

		StringBuilder script = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				script.append(line).append('\n');
			}
		}

		// This simple split is suitable for this schema file, which has no
		// semicolons inside string literals or stored procedure definitions.
		String[] statements = script.toString().split(";");

		try (Statement statement = connection.createStatement()) {
			for (String sql : statements) {
				String trimmed = sql.trim();
				if (!trimmed.isEmpty()) {
					statement.execute(trimmed);
				}
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		Object value = event.getServletContext().getAttribute("con");

		if (value instanceof Connection) {
			try {
				((Connection) value).close();
			} catch (SQLException ex) {
				LOGGER.log(Level.WARNING, "Could not close the H2 connection.", ex);
			}
		}
	}

	private void seedIfEmpty(Connection connection) throws SQLException, IOException {
		boolean needsSeed;
		try (Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM Features")) {
			result.next();
			needsSeed = result.getInt(1) == 0;
		}
		if (needsSeed) {
			executeSqlResource(connection, "/db/data.sql");
			LOGGER.log(Level.INFO,"Loaded initial data from /db/data.sql");
		} else {
			LOGGER.log(Level.INFO,"H2 already contains feature rows; skipping seed data.");
		}
	}

	private void executeSqlResource(Connection connection, String resourcePath)	throws IOException, SQLException {
		InputStream input = getClass().getResourceAsStream(resourcePath);
		if (input == null) {
			throw new IOException(
					"Could not find classpath resource: " + resourcePath);
		}
		StringBuilder sqlScript = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(input, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sqlScript.append(line).append('\n');
			}
		}

		// Suitable for these simple schema/seed scripts. Avoid semicolons
		// inside quoted values or more complex SQL constructs.

		String[] statements = sqlScript.toString().split(";");
		boolean originalAutoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			for (String sql : statements) {
				String trimmed = sql.trim();
				if (!trimmed.isEmpty()) {
					statement.execute(trimmed);
				}
			}
			connection.commit();
		} catch (SQLException ex) {
			connection.rollback();
			throw ex;
		} finally {
			connection.setAutoCommit(originalAutoCommit);
		}
	}

}