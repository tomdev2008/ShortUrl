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

	private static final String CONNECTION_URL = "jdbc:mysql://" + Config.SQL_HOST + "/" + Config.DB_NAME + "?autoReconnect=true&user=" + Config.SQL_USERNAME + "&password=" + Config.SQL_PASSWORD;

	private static final String INSERT_LONG_URL_STATEMENT = "INSERT INTO `short_url`.`short_url` (`long_url`) VALUES ( ? );";
	private static final String UPDATE_LONG_URL_STATEMENT = "UPDATE `short_url`.`short_url` SET `short_url`= ? WHERE `id`= ? ;";
	private static final String SELECT_LONG_URL_QUERY = "SELECT `id`, `short_url` FROM `short_url`.`short_url` WHERE `long_url` = ?;";
	private static final String SELECT_SHORT_URL_QUERY = "SELECT * FROM `short_url`.`short_url` WHERE id = ?;";

	//will handle null connections better
	private Connection getConnection() throws SQLException {
		Connection cnx = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			cnx = DriverManager.getConnection(CONNECTION_URL);
		}catch(ClassNotFoundException e){
			log.error("sql driver class not found", e);
		}
		if(cnx == null){
			log.error("db connection was null");
		}
		return cnx;
	}

	@Override
	public Url getUrl(int urlId) throws UrlNotFoundException {
		Url url = null;
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			Connection cnx = getConnection();
			statement = cnx.prepareStatement(SELECT_SHORT_URL_QUERY);
			statement.setInt(1, urlId);
			set = statement.executeQuery();
			
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
			cnx.close();
		} catch (SQLException e) {
			log.info("sql exception while retrieving url id=" + urlId, e);
			throw new UrlNotFoundException("sql exception while getting url", e);
		}
		return url;
	}

	public Url createNewShortUrl(final String longUrl) throws ShortUrlException {
		Url createdUrl = null;
				
		PreparedStatement insertStatement = null;
		PreparedStatement postInsertUpdateStatement = null;

		ResultSet foundLongUrlSet = null;
		PreparedStatement selectLongStatements = null;
		
		String generatedShortUrl = null;
		int generatedId = 0; //must be greater than 1 later
		
		try{
			Connection cnx = getConnection();
			
			//see if the long url already exists
			selectLongStatements = cnx.prepareStatement(SELECT_LONG_URL_QUERY);
			selectLongStatements.setString(1, longUrl);
			foundLongUrlSet = selectLongStatements.executeQuery();
			
			//return existing short url if long already exists
			if(foundLongUrlSet.next()){				
				int foundId = foundLongUrlSet.getInt("id");
				createdUrl = new Url(foundId, longUrl);
				cnx.close();
				log.info("long url exists already, returning id= " + foundId + " instead of creating new one");
				return createdUrl;
			}else{
				log.info("existing long url for url: " + longUrl + " does not exist, creating new one");
			}


			//begin create if long doesnt exist already
			cnx.setAutoCommit(false);
			insertStatement = cnx.prepareStatement(INSERT_LONG_URL_STATEMENT, Statement.RETURN_GENERATED_KEYS);
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
			postInsertUpdateStatement = cnx.prepareStatement(UPDATE_LONG_URL_STATEMENT);
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
			cnx.close();
		}catch(SQLException e){
			throw new ShortUrlException(e);
		}
		return createdUrl;
	}

}