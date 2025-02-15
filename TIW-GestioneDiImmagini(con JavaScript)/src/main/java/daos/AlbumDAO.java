package daos;

import beans.dbObjects.Album;
import beans.dbObjects.FullAlbum;
import beans.dbObjects.Image;
import beans.utils.Pair;
import beans.dbObjects.User;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumDAO implements MyDAO
	{
		private Connection connection;

		public AlbumDAO (Connection connection)
			{
				this.connection = connection;
			}
		
		public void sortAlbum (String user, String creator, String name, String[] sorting) throws DbErrorException, BadFormatException
			{				
				String update = "INSERT INTO " + MyDAO.SORTINGS_TABLE_NAME + " VALUES(?,?,?,?,?);";
					//per inserire l'ordinamento
				String update1 = "DELETE FROM " + MyDAO.SORTINGS_TABLE_NAME + " WHERE utente=? AND nome_album=? AND creatore=?";
					//per eliminare quello vecchio
				String query = "SELECT COUNT(*) FROM "+ MyDAO.ADDITIONS_TABLE_NAME + " WHERE nome_album=? AND creatore=?";
					//controllo se la dimensione il vettore coi nomi delle immagini ordinate come vuole l'utente ha
					//dimensione uguale al numero di immagini dell'album
				
				PreparedStatement pstatement = null;
				PreparedStatement pstatement1 = null;
				PreparedStatement pstatement2 = null;
				ResultSet result = null;
				
				try
					{
						this.connection.setAutoCommit(false);
						User.checkUsername(user);
						User.checkUsername(creator);
						Album.checkAlbumName(name);
						
						for (int i = 0 ; i < sorting.length ; i++)
							{
								Image.checkImageName(sorting[i]);
							}
						
						pstatement2 = connection.prepareStatement(query);
						pstatement2.setString(2, creator);
						pstatement2.setString(1, name);
						result = pstatement2.executeQuery();
						result.next();
						
						if (result.getInt(1) != sorting.length)
							throw (new BadFormatException("ordinamento", sorting.toString(), "l'ordinamento scelto non e' inseribile"));
						
						pstatement1 = connection.prepareStatement(update1);
						pstatement1.setString(1, user);
						pstatement1.setString(3, creator);
						pstatement1.setString(2, name);
						pstatement1.executeUpdate();
						
						pstatement = connection.prepareStatement(update);
						pstatement.setString(1, user);
						pstatement.setString(3, creator);
						pstatement.setString(2, name);
						
						for (int i = 0 ; i < sorting.length ; i++)
							{
								pstatement.setString(4, sorting[i]);
								pstatement.setInt(5, i);
								pstatement.executeUpdate();
							}
						
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
								throw(new DbErrorException());
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
						
						throw (new DbErrorException("l'ordinamento", e));
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
						MyDAO.closeQuery(result, pstatement2);
					}	
			}
		
		public Map<String, Object> getUserResourcesInfos (String username) throws DbErrorException, BadFormatException
		/*
		 * restituisce tutte le informazioni utili alla visualizzazione dell'homepage
		 */	
			{
				List<Image> userImagesInfos;
				List<Pair<String>> userAlbumNames;
				List<Pair<String>> nonUserAlbumNames;
				Map<String, Object> ret = new HashMap<>();
				
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
					
						ret.put("images_infos", userImagesInfos);
						ret.put("user_albums", userAlbumNames);
						ret.put("nonUserAlbums", nonUserAlbumNames);
						
						return (ret);
					}
				catch (BadFormatException | DbErrorException e)
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
						
						throw (new DbErrorException());
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
		public FullAlbum getUserAlbum(String requester, String username, String album_name) throws DbErrorException, BadFormatException //OK
			{
				String query = "SELECT data_creazione FROM "+MyDAO.ALBUMS_TABLE_NAME+ " WHERE nome=? AND creatore=?;";
				String query1 = "SELECT titolo_immagine,data_creazione,testo,path FROM " + MyDAO.ADDITIONS_TABLE_NAME + ","+MyDAO.IMAGES_TABLE_NAME+" WHERE titolo=titolo_immagine AND nome_album=? AND "+MyDAO.IMAGES_TABLE_NAME+".creatore=? AND "+MyDAO.IMAGES_TABLE_NAME+".creatore="+MyDAO.ADDITIONS_TABLE_NAME+".creatore ORDER BY data_creazione DESC;";
				String query2 = "SELECT titolo_immagine,data_creazione,testo,path FROM " + MyDAO.SORTINGS_TABLE_NAME + ","+MyDAO.IMAGES_TABLE_NAME+" WHERE titolo=titolo_immagine AND nome_album=? AND "+MyDAO.IMAGES_TABLE_NAME+".creatore=? AND "+MyDAO.IMAGES_TABLE_NAME+".creatore="+MyDAO.SORTINGS_TABLE_NAME+".creatore AND utente=? ORDER BY posizione ASC;";

				ResultSet result = null;
				PreparedStatement pstatement = null;
				ResultSet result1 = null;
				PreparedStatement pstatement1 = null;
				ResultSet result2 = null;
				PreparedStatement pstatement2 = null;
				
				ImageDAO imageDAO = new ImageDAO(this.connection);
				
				FullAlbum ret = null;
				
				try
					{	
						this.connection.setAutoCommit(false);
						
						User.checkUsername(requester);
						User.checkUsername(username);
						Album.checkAlbumName(album_name);
						
						pstatement = connection.prepareStatement(query);
						pstatement.setString(1, album_name);
						pstatement.setString(2, username);
						result = pstatement.executeQuery();
						result.next();
						
						List<Image> images = new ArrayList<>();
						
						pstatement2 = connection.prepareStatement(query2);
						pstatement2.setString(1, album_name);
						pstatement2.setString(2, username);
						pstatement2.setString(3, requester);		
						
						result2 = pstatement2.executeQuery();
						
						while (result2.next())
							{
								images.add(imageDAO.getImage(username, result2.getString("titolo_immagine"), requester));
							}
						
						if (images.size()==0)
							{
								pstatement1 = connection.prepareStatement(query1);
								pstatement1.setString(1, album_name);
								pstatement1.setString(2, username);
								
								result1 = pstatement1.executeQuery();
								
								while (result1.next())
									{
										images.add(imageDAO.getImage(username, result1.getString("titolo_immagine"), requester));
									}
							}
						
						ret = new FullAlbum(result.getDate("data_creazione"),album_name,username, images);
						
						this.connection.commit();

						return (ret);
					}
				catch (BadFormatException | DbErrorException e)
					{
						try
							{
								this.connection.rollback();
							}
						catch (SQLException e1)
							{
								throw(new DbErrorException());
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
								throw(new DbErrorException());
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
								e.printStackTrace();
							}
						MyDAO.closeQuery(result1, pstatement1);
						MyDAO.closeQuery(result2, pstatement2);
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