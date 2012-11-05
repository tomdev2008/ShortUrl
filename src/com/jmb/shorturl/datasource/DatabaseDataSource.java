package com.jmb.shorturl.datasource;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jmb.shorturl.model.Url;
import com.jmb.shorturl.exception.UrlNotFoundException;
import com.jmb.shorturl.exception.UrlException;
import com.jmb.shorturl.exception.UrlException;
import com.jmb.shorturl.util.Config;

public class DatabaseDataSource extends AbstractDatasource {

	private static final Logger log = Logger.getLogger(DatabaseDataSource.class);

	private static final String CONNECTION_URL = "jdbc:mysql://" + Config.SQL_HOST + "/" + Config.DB_NAME + "?user=" + Config.SQL_USERNAME + "&password=" + Config.SQL_PASSWORD;

	//TODO - look into better null pointer allerting
	private static Connection connection = null;

	public DatabaseDataSource() {
		log.info("establishing new database connection");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			if (connection == null) {// first time startup null pointer issues
				connection = DriverManager.getConnection(CONNECTION_URL);
			} else if(connection.isClosed()) {
				connection = DriverManager.getConnection(CONNECTION_URL);
			} else{
				log.fatal("FAILED TO GET/RE-OPEN DATABASE CONNECTION");
			}
		} catch (ClassNotFoundException e) {
			log.info("The database driver class was not found, class=com.mysql.jdbc.Driver", e);
		} catch (SQLException e) {
			log.info("SQL exception while extablishing a database connection", e);
		}
	}

	@Override
	public Url getUrl(int urlId) throws UrlNotFoundException {
		Url url = null;
		Statement statement = null;
		ResultSet set = null;
		
		try {
			statement = connection.createStatement();
			set = statement.executeQuery("SELECT * FROM " + Config.DB_NAME + "." + Config.TABLE_NAME + " WHERE id = " + urlId);
			
			//there is of course only one url with this id
			if(set.next()){
				int id = set.getInt("id");
				String shortUrl = set.getString("short_url");
				String longUrl = set.getString("long_url");
				url = new Url(id, shortUrl, longUrl);
			}

			set.close();
			statement.close();

			if(url == null){
				throw new UrlNotFoundException("no url found with id = " + urlId);
			}

		} catch (SQLException e) {
			log.info("sql exception while retrieving url id=" + urlId, e);
			throw new UrlNotFoundException("sql exception while getting url", e);
		}
		return url;
	}

}