package com.company.fxrateaggregatorservice.JAVA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.beans.Introspector;

import java.util.*;
import java.beans.IntrospectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.DriverManager;
import org.springframework.http.MediaType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.springframework.http.ResponseEntity;

@RestController
@EnableAutoConfiguration
public class HelloController {
	String URL_RATES_JSON = "https://api.exchangeratesapi.io/latest?base=";
	String URL_RATES_DATE = "https://api.exchangeratesapi.io/";

	@RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	String home() {
		StringBuilder builder = new StringBuilder();
		builder.append("Hello World !!");

		builder.append("\n\nJDBC connection available: ");
		try {
			Connection conn = getConnection();
			if (conn != null) {
				builder.append("yes");
				builder.append("\n\nCurrent Hana DB user:\n");
				String userName = getCurrentUser(conn);
				builder.append(userName);
				builder.append("\n\nCurrent Hana schema:\n");
				builder.append(getCurrentSchema(conn));
			} else {
				builder.append("no");
			}
		} catch (SQLException e) {
			builder.append("no");
		}

		return builder.toString();
	}

	private String getCurrentUser(Connection conn) throws SQLException {
		String currentUser = "";
		PreparedStatement prepareStatement = conn.prepareStatement("SELECT CURRENT_USER \"current_user\" FROM DUMMY;");
		ResultSet resultSet = prepareStatement.executeQuery();
		int column = resultSet.findColumn("current_user");
		while (resultSet.next()) {
			currentUser += resultSet.getString(column);
		}
		return currentUser;
	}

	private String getCurrentSchema(Connection conn) throws SQLException {
		String currentSchema = "";
		PreparedStatement prepareStatement = conn
				.prepareStatement("SELECT CURRENT_SCHEMA \"current_schema\" FROM DUMMY;");
		ResultSet resultSet = prepareStatement.executeQuery();
		int column = resultSet.findColumn("current_schema");
		while (resultSet.next()) {
			currentSchema += resultSet.getString(column);
		}
		return currentSchema;
	}

	private Connection getConnection() {
		Connection conn = null;
		String DB_USERNAME = "";
		String DB_PASSWORD = "";
		String DB_HOST = "";
		String DB_PORT = "";

		try {
			JSONObject obj = new JSONObject(System.getenv("VCAP_SERVICES"));
			JSONArray arr = obj.getJSONArray("hana");
			DB_USERNAME = arr.getJSONObject(0).getJSONObject("credentials").getString("user");
			DB_PASSWORD = arr.getJSONObject(0).getJSONObject("credentials").getString("password");
			DB_HOST = arr.getJSONObject(0).getJSONObject("credentials").getString("host").split(",")[0];
			DB_PORT = arr.getJSONObject(0).getJSONObject("credentials").getString("port");
			// String DB_READ_CONNECTION_URL = "jdbc:sap://" + DB_HOST + ":" + DB_PORT;
			String DB_READ_CONNECTION_URL = "jdbc:sap://zeus.hana.prod.eu-central-1.whitney.dbaas.ondemand.com:21794?encrypt=true&validateCertificate=true&currentschema=FXRATEAGGREGATORSERVICE_HDI_FX_DB_1";

			conn = (Connection) DriverManager.getConnection(DB_READ_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
		} catch (Exception e) {
			System.out.println("Connection Error");
		}

		return conn;
	}
    
    @PostMapping(path = "/fxrates/addapi", consumes = "application/json")
	public @ResponseBody String addApi(@RequestBody ApiDetailsEntity apiDetails) {
		PreparedStatement preparedStatement = null;
		//{name}/{apiUrl}
		try {
			Connection conn = getConnection();
			String query = "INSERT INTO " + getCurrentSchema(conn) + "."
					+ "\"FxRateAggregatorService.FX_DB::cdsArtifact.fx_rates_api_aggregator\"" + "VALUES(?,?)";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, apiDetails.getApiName());
			preparedStatement.setString(2, apiDetails.getApiUrl());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());

		}
		return null;
	}

	@RequestMapping(value = "/fxrates/apirates/{baseCurrency}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Root getAPIRates(@PathVariable("baseCurrency") String baseCurrency) {
		RestTemplate restTemplate = new RestTemplate();
		RestTemplate restTemplateQ = new RestTemplate();
		Root result = restTemplate.getForObject(URL_RATES_JSON + baseCurrency, Root.class);
		System.out.println(URL_RATES_JSON + baseCurrency);
		try {
			Connection conn = getConnection();
			Rates rates = result.getRates();
			Map<String, Object> map = beanProperties(rates);
			Object obj = map.remove("tryc");
			// map.put("try", obj);
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				System.out.println("Key = " + entry.getKey().toUpperCase() + ", Value = " + entry.getValue());
				String query = "INSERT INTO " + getCurrentSchema(conn) + "."
						+ "\"FxRateAggregatorService.FX_DB::cdsArtifact.aggregator_fx_rates\""
						+ "VALUES(?,?,?,?,?,?,?,?)";
				System.out.println(query);
				PreparedStatement preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, 7);
				preparedStatement.setString(2, baseCurrency);
				preparedStatement.setString(3, entry.getKey().toUpperCase());
				preparedStatement.setString(4, null);
				preparedStatement.setDouble(5, (Double) entry.getValue());
				preparedStatement.setString(6, getDateString());
				preparedStatement.setString(7, "exchangeratesapi.io");
				preparedStatement.setString(8, getDateString());
				preparedStatement.executeUpdate();
			}
			ResponseEntity<String> result1 = restTemplateQ.postForEntity(
					"https://emjapisamplesjmsp2p.cfapps.eu10.hana.ondemand.com/queue/sfxratesq/message", result,
					String.class);
			System.out.println("result1" + result1);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getMessage());
		}
		return result;
	}

	public static Map<String, Object> beanProperties(Object bean) {
		try {
			Map<String, Object> map = new HashMap<>();
			Arrays.asList(Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors()).stream()
					.filter(pd -> Objects.nonNull(pd.getReadMethod())).forEach(pd -> {
						// invoke method to get value
						try {
							Object value = pd.getReadMethod().invoke(bean);
							if (value != null) {
								map.put(pd.getName(), value);
							}
						} catch (Exception e) {
						}
					});
			return map;
		} catch (IntrospectionException e) {
			return Collections.emptyMap();
		}
	}

	public String getDateString() {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String strDate = dateFormat.format(date);
		System.out.println("Converted String: " + strDate);
		return strDate;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(HelloController.class, args);
	}
}
