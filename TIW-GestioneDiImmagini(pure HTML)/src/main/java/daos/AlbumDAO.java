package daos;

import beans.dbObjects.Album;
import beans.utils.Pair;
import beans.dbObjects.User;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO implements MyDAO
	{
		private Connection connection;

		public AlbumDAO (Connection connection)
			{
				this.connection = connection;
			}
		
		public List<List<Pair<String>>> getUserResourcesInfos (String username) throws DbErrorException, BadFormatException
		/*
		 * restituisce tutte le informazioni utili alla visualizzazione dell'homepage
		 */
			{
				List<Pair<String>> userImagesInfos;
				List<Pair<String>> userAlbumNames;
				List<Pair<String>> nonUserAlbumNames;
				
				ImageDAO image_dao = new ImageDAO(connection);
				
				try
					{
						this.connection.setAutoCommit(false);
						User.checkUsername(username);
						
						userImagesInfos = image_dao.getUserImagesInfos(username);
						
						userAlbumNames = this.getUserAlbumsNames(username)
								.stream()
								.map(s -> new Pair<String>(s, username))
								.toList();
						
						nonUserAlbumNames = this.getNonUserAlbumsInfos(username);
						
						this.connection.commit();
						
						List<List<Pair<String>>> ret = new ArrayList<>(3);
						
						ret.add(userImagesInfos);
						ret.add(userAlbumNames);
						ret.add(nonUserAlbumNames);
						
						return (ret);
					}
				catch (BadFormatException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						throw (e);
					}
				catch (SQLException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						throw (new DbErrorException("una o piu' delle risorse richieste non e' disponibile"));
					}
				finally
					{
						try
							{
								this.connection.setAutoCommit(true);
							}
						catch (SQLException e)
							{
								throw (new DbErrorException());
							}
					}
			}
		
		public List<String> getUserAlbumsNames(String username) throws DbErrorException, BadFormatException
			{
				String query = "SELECT nome FROM " + MyDAO.ALBUMS_TABLE_NAME + " WHERE creatore=? ORDER BY data_creazione DESC;";
				
				ResultSet result = null;
				PreparedStatement pstatement = null;
				List<String> ret = null;

				try
					{
						User.checkUsername(username);
						pstatement = connection.prepareStatement(query);
						pstatement.setString(1, username);
						result = pstatement.executeQuery();
						ret = new ArrayList<>();

						while (result.next())
							{
								ret.add(result.getString("nome"));
							}

						return (ret);
					}
				catch (SQLException e)
					{
						throw (new DbErrorException("l'utente "+username, e));
					}
				catch (BadFormatException e)
					{
						throw (e);
					}
				finally
					{
						MyDAO.closeQuery(result, pstatement);
					}
			}
		public Album getUserAlbum(String username, String album_name) throws DbErrorException, BadFormatException
			{
				String query = "SELECT * FROM " + MyDAO.ALBUMS_TABLE_NAME + " WHERE creatore=? AND nome=?";
				String query1 = "SELECT titolo_immagine,path FROM " + MyDAO.ADDITIONS_TABLE_NAME + " , " +MyDAO.IMAGES_TABLE_NAME +" WHERE titolo=titolo_immagine AND "+MyDAO.ADDITIONS_TABLE_NAME+".creatore="+MyDAO.IMAGES_TABLE_NAME+".creatore AND nome_album=? AND "+MyDAO.IMAGES_TABLE_NAME+".creatore=? ORDER BY data_creazione DESC";

				ResultSet result = null;
				PreparedStatement pstatement = null;
				ResultSet result1 = null;
				PreparedStatement pstatement1 = null;
				
				Album ret = null;
				
				try
					{	
						this.connection.setAutoCommit(false);
						
						User.checkUsername(username);
						Album.checkAlbumName(album_name);
						
						pstatement = connection.prepareStatement(query);
						pstatement.setString(1, username);
						pstatement.setString(2, album_name);
						
						result = pstatement.executeQuery();
						result.next();
						
						pstatement1 = connection.prepareStatement(query1);
						pstatement1.setString(1, album_name);
						pstatement1.setString(2, username);
						
						result1 = pstatement1.executeQuery();	
						
						List<Pair<String>> images_infos = new ArrayList<>();
						while (result1.next())
							{
								images_infos.add(new Pair<>(result1.getString("titolo_immagine"),result1.getString("path")));
							}
						
						try
							{
								ret = new Album(result.getDate("data_creazione"),album_name,username, images_infos);
							}
						catch (BadFormatException e)
							{
								throw (e);
							}
						
						this.connection.commit();
						
						return (ret);
					}
				catch (SQLException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						throw (new DbErrorException("l'album "+album_name+" di "+username, e));
					}
				finally
					{
						try
							{
								this.connection.setAutoCommit(true);
							}
						catch (SQLException e)
							{
								throw (new DbErrorException());
							}
						
						MyDAO.closeQuery(result, pstatement);
					}
			}
		public List<Pair<String>> getNonUserAlbumsInfos(String username) throws DbErrorException, BadFormatException
			{
				String query = "SELECT nome,creatore FROM " + MyDAO.ALBUMS_TABLE_NAME + " WHERE creatore<>? ORDER BY data_creazione DESC;";

				ResultSet result = null;
				PreparedStatement pstatement = null;
				List<Pair<String>> ret = null;

				try
					{
						User.checkUsername(username);
						pstatement = connection.prepareStatement(query);
						pstatement.setString(1, username);
						result = pstatement.executeQuery();
						ret = new ArrayList<>();

						while (result.next())
							{
								ret.add(new Pair<String>(result.getString("nome"), result.getString("creatore")));
							}

						return (ret);
					}
				catch (SQLException e)
					{
						throw (new DbErrorException("l'utente "+username, e));
					}
				catch (BadFormatException e)
					{
						throw (e);
					}
				finally
					{
						MyDAO.closeQuery(result, pstatement);
					}
			}
		public void createAlbum(Album album) throws DbErrorException
			{
				String update_albums = "INSERT INTO " + MyDAO.ALBUMS_TABLE_NAME + " VALUES(?,?,?);";
				String update_additions = "INSERT INTO " + MyDAO.ADDITIONS_TABLE_NAME + " VALUES(?,?,?);";

				PreparedStatement pstatement = null;
				PreparedStatement pstatement1 = null;

				try
					{
						this.connection.setAutoCommit(false);
						
						pstatement = connection.prepareStatement(update_albums);
						pstatement.setString(1, album.getName());
						pstatement.setString(2, album.getCreator());
						pstatement.setDate(3, album.getDate());
						pstatement.executeUpdate();
						
						pstatement1 = connection.prepareStatement(update_additions);
						pstatement1.setString(1, album.getName());
						pstatement1.setString(2, album.getCreator());
						
						for (Pair<String> image : album.getImagesInfos())
							{
								pstatement1.setString(3, image.getElem1());
								pstatement1.executeUpdate();
							}
						
						this.connection.commit();

					}
				catch (SQLException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						throw (new DbErrorException("l'album "+album.getName()+" di "+album.getCreator(), e));
					}
				finally
					{
						try
							{
								this.connection.setAutoCommit(true);
							}
						catch (SQLException e)
							{
								throw (new DbErrorException());
							}
						
						MyDAO.closeQuery(pstatement);
						MyDAO.closeQuery(pstatement1);
					}

				return;
			}
		public void deleteAlbum(String user, String album_name) throws DbErrorException, BadFormatException
			{
				String query = "SELECT * FROM "+ MyDAO.ALBUMS_TABLE_NAME + " WHERE creatore=? AND nome=?;";
				String update = "DELETE FROM " + MyDAO.ALBUMS_TABLE_NAME + " WHERE creatore=? AND nome=?;";

				PreparedStatement pstatement0 = null;
				PreparedStatement pstatement = null;
				ResultSet res0 = null;

				try
					{
						this.connection.setAutoCommit(false);
						
						Album.checkAlbumName(album_name);
						User.checkUsername(user);
						
						pstatement0 = connection.prepareStatement(query);
						pstatement0.setString(1, user);
						pstatement0.setString(2, album_name);
						res0 = pstatement0.executeQuery();
						res0.next();
						res0.getString("creatore");
						
						pstatement = connection.prepareStatement(update);
						pstatement.setString(1, user);
						pstatement.setString(2, album_name);
						pstatement.executeUpdate();
						
						this.connection.commit();						
					}
				catch (BadFormatException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						throw (e);
					}
				catch (SQLException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						throw (new DbErrorException("l'album "+album_name+" di "+user, e));
					}
				finally
					{
						try
							{
								this.connection.setAutoCommit(true);
							}
						catch (SQLException e1)
							{
								throw (new DbErrorException());
							}
						
						MyDAO.closeQuery(res0, pstatement0);
						MyDAO.closeQuery(pstatement);
					}
			}
	}