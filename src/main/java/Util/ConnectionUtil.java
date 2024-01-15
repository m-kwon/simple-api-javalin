package Util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

public class ConnectionUtil {

	private static String url = "jdbc:h2:./h2/db;";
	private static String username = "sa";
	private static String password = "sa";

	private static JdbcDataSource pool = new JdbcDataSource();

  static {
		pool.setURL(url);
		pool.setUser(username);
		pool.setPassword(password);
	}

	public static Connection getConnection() {
		try {
			return pool.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

  public static void resetTestDatabase() {
		try {
			FileReader sqlReader = new FileReader("src/main/resources/SocialMedia.sql");
			RunScript.execute(getConnection(), sqlReader);
		} catch (SQLException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}