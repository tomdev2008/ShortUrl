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
import com.jmb.shorturl.exception.ShortUrlException;
import com.jmb.shorturl.util.Config;

public class DatabaseDataSource extends AbstractDatasource {

	private static final Logger log = Logger.getLogger(DatabaseDataSource.class);

	private static final String CONNECTION_URL = "jdbc:mysql://" + Config.SQL_HOST + "/" + Config.DB_NAME + "?user=" + Config.SQL_USERNAME + "&password=" + Config.SQL_PASSWORD;

	private static final String insertSql = "INSERT INTO `short_url`.`short_url` (`long_url`) VALUES ( ? );";
	private static final String updateSql = "UPDATE `short_url`.`short_url` SET `short_url`= ? WHERE `id`= ? ;";

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

	//will handle null connections better
	private static Connection getConnection() throws SQLException {
		if(connection.isClosed()){
			try{
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection(CONNECTION_URL);
			}catch(ClassNotFoundException e){
				log.error("sql driver class not found", e);
			}
		}
		return connection;
	}

	@Override
	public Url getUrl(int urlId) throws UrlNotFoundException {
		Url url = null;
		Statement statement = null;
		ResultSet set = null;
		
		try {
			statement = getConnection().createStatement();
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

	public Url createNewShortUrl(String longUrl) throws ShortUrlException {
		Url createdUrl = null;
				
		PreparedStatement insertStatement = null;
		PreparedStatement postInsertUpdateStatement = null;
		
		String generatedShortUrl = null;
		int generatedId = 0; //must be greater than 1 later
		
		try{
			Connection cnx = getConnection();
			cnx.setAutoCommit(false);
			insertStatement = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
			insertStatement.setString(1, longUrl);
			
			int insertRow = insertStatement.executeUpdate();	
	
			if(insertRow == 0){
				cnx.rollback();
				throw new SQLException("insert of long url " + longUrl + " failed (rolling back transaction)");
			}
	
			ResultSet generatedKeys = insertStatement.getGeneratedKeys();
			if(generatedKeys.next()){
				generatedId = generatedKeys.getInt(1);
			}else{
				cnx.rollback();
				throw new SQLException("cant get the first generated key from the insertStatement result set (rolling back transaction)");
			}
			insertStatement.close();
	
			if(generatedId < 1){
				cnx.rollback();
				throw new SQLException("the generated id for insert was less than 1, value: " + generatedId + "  (rolling back transaction)");
			}
	
			createdUrl = new Url(generatedId, longUrl);
			postInsertUpdateStatement = cnx.prepareStatement(updateSql);
			postInsertUpdateStatement.setString(1, createdUrl.getShortUrlString());
			postInsertUpdateStatement.setInt(2, createdUrl.getUrlId());
			int sqlUpdateRows = postInsertUpdateStatement.executeUpdate();
	
			if(sqlUpdateRows == 0){
				cnx.rollback();
				throw new SQLException("error running the update on the pre-existing long url: " + longUrl + "  (rolling back transaction)");
			}

			postInsertUpdateStatement.close();
			cnx.commit();
			cnx.setAutoCommit(true);
			log.info("url created sucessfully: " + createdUrl.toString());

		}catch(SQLException e){
			throw new ShortUrlException(e);
		}
		return createdUrl;
	}

}