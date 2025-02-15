package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.dbObjects.User;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;

public class UserDAO implements MyDAO
	{
		private Connection connection;

		public UserDAO (Connection connection)
			{
				this.connection = connection;
			}

		public void createUser (User utente) throws DbErrorException
			{
				String update = "INSERT INTO " +MyDAO.USERS_TABLE_NAME+ " VALUES (?,?,?);";
	
				PreparedStatement pstatement = null;
				
				try
					{						
						pstatement = connection.prepareStatement(update);
						
						pstatement.setString(1, utente.getUsername());
						pstatement.setString(2, utente.getPassword());
						pstatement.setString(3, utente.getMail());

						pstatement.executeUpdate();
					}
				catch (SQLException e) //lo username scelto per la registrazione è già stato utilizzato
					{
						throw (new DbErrorException("l'utente "+utente.getUsername(), e));
					}
				finally
					{
						MyDAO.closeQuery(pstatement);
					}
			}
		
		public String checkAutentication (String username, String password) throws DbErrorException, BadFormatException
			{
				String query = "SELECT username,password FROM " + MyDAO.USERS_TABLE_NAME+" WHERE username=?";
				
				PreparedStatement pstatement = null;
				ResultSet result = null;
				
				try
					{
						User.checkUsername(username);
						User.checkPassword(password);
						
						pstatement = connection.prepareStatement(query);
						
						pstatement.setString(1, username);
						
						result = pstatement.executeQuery();
						
						result.next();
						
						if (result.getString("password").equals(password))
							{
								return (result.getString("username"));
							}
						else
							{
								throw (new DbErrorException("password errata"));
							}
					}
				catch (BadFormatException e)
					{
						throw (e);
					}
				catch (SQLException e)
					{
						throw (new DbErrorException("l'utente "+username, e));
					}
				finally
					{
						MyDAO.closeQuery(pstatement);
					}
			}
	}
