package daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import beans.exceptions.BadFormatException;

import beans.dbObjects.Comment;
import beans.dbObjects.Image;
import beans.utils.Pair;
import beans.dbObjects.User;

import beans.exceptions.DbErrorException;

public class ImageDAO implements MyDAO
	{
		private Connection connection;

		public ImageDAO (Connection connection)
			{
				this.connection = connection;
			}
		
		public List<Pair<String>> getUserImagesInfos (String username) throws DbErrorException, BadFormatException
			{
				String query = "SELECT titolo,path FROM " + MyDAO.IMAGES_TABLE_NAME + " WHERE creatore=?;";

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
								ret.add(new Pair<String>(result.getString("titolo"), result.getString("path")));
							}

						return (ret);
					}
				catch (BadFormatException e)
					{
						throw (e);
					}
				catch (SQLException e)
					{
						throw (new DbErrorException("utente: " + username, e));
					}
				finally
					{
						MyDAO.closeQuery(result, pstatement);
					}
			}
		
		public void isPrivate (String image_name, String creator) throws DbErrorException, BadFormatException
		/*
		 * controlla che l'immagine richiesta sia presente in almeno un album (le immagini non presenti
		 * in alcun album non andrebbero mostrate a utenti diversi da chi le ha caricate)
		 */
			{
				String query_checking = "SELECT * FROM " + MyDAO.ADDITIONS_TABLE_NAME + " WHERE titolo_immagine=? AND creatore=?;";
				
				ResultSet result_check = null;
				PreparedStatement pstatement_check = null;
				
				try
					{
						Image.checkImageIds(image_name, creator);
						pstatement_check = connection.prepareStatement(query_checking);
						pstatement_check.setString(1, image_name);
						pstatement_check.setString(2, creator);
						
						result_check = pstatement_check.executeQuery();
						
						if (!result_check.next())
							{
								throw (new BadFormatException(image_name, image_name, "non sei autorizzato ad accedere a questa immagine"));
							}
					}
				catch (SQLException e)
					{
						throw (new DbErrorException());
					}
				finally
					{	
						MyDAO.closeQuery(result_check, pstatement_check);
					}
			}
		
		public Image getImage(String creator, String image_name, String requester) throws DbErrorException, BadFormatException
			{
				String query_image = "SELECT * FROM " + MyDAO.IMAGES_TABLE_NAME + " WHERE titolo=? AND creatore=?;";
				String query_comments = "SELECT * FROM " + MyDAO.COMMENTS_TABLE_NAME + " WHERE nome_immagine=? AND creatore_immagine=? ORDER BY timestamp ASC;";
				
				ResultSet result_image = null;
				PreparedStatement pstatement_image = null;
				ResultSet result_comments = null;
				List<Comment> comments = null;
				PreparedStatement pstatement_comments = null;
				Image ret = null;
				
				try
					{
						this.connection.setAutoCommit(false);
						
						Image.checkImageIds(image_name, creator);
						User.checkUsername(requester);
						
						if (!requester.equalsIgnoreCase(creator))
							{
								this.isPrivate(image_name, creator);
							}
						
						pstatement_comments = connection.prepareStatement(query_comments);
						pstatement_comments.setString(1, image_name);
						pstatement_comments.setString(2, creator);
						
						result_comments = pstatement_comments.executeQuery();
						
						comments = new ArrayList<>();
						
						while (result_comments.next())
							{
								try
									{
										comments.add(new Comment(result_comments.getString("commentatore"), result_comments.getString("nome_immagine"), result_comments.getString("creatore_immagine"), result_comments.getTimestamp("timestamp"), result_comments.getString("testo")));
									}
								catch (BadFormatException e)
									{
										throw (e);
									}
							}
						
						pstatement_image = connection.prepareStatement(query_image);
						pstatement_image.setString(1, image_name);
						pstatement_image.setString(2, creator);
						
						result_image = pstatement_image.executeQuery();
			
						result_image.next();
						try
							{
								ret = new Image(result_image.getDate("data_creazione"), result_image.getString("titolo"), result_image.getString("creatore"), result_image.getString("testo"), result_image.getString("path"),comments);
							}
						catch (BadFormatException e)
							{
								throw (e);
							}
						
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
						
						throw (new DbErrorException("l'immagine "+image_name+" di "+creator, e));
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
						
						MyDAO.closeQuery(result_comments, pstatement_comments);
						MyDAO.closeQuery(result_image, pstatement_image);
					}
			}
		
		public void uploadImage(String image_name, String username, String description, String path) throws DbErrorException, BadFormatException
			{
				String update = "INSERT INTO " + MyDAO.IMAGES_TABLE_NAME + " VALUES(?,?,?,?,?);";

				PreparedStatement pstatement = null;

				try
					{
						Image.checkImageData(image_name, description);
						Image.checkImagePath(path);
						User.checkUsername(username);
						pstatement = connection.prepareStatement(update);
						pstatement.setString(1, image_name);
						pstatement.setString(2, username);
						pstatement.setDate(3, new Date(Calendar.getInstance().getTime().getTime()));
						pstatement.setString(4, description);
						pstatement.setString(5, path);
						pstatement.executeUpdate();

						return;
					}
				catch (BadFormatException e)
					{
						throw (e);
					}
				catch (SQLException e)
					{
						throw (new DbErrorException("l'immagine "+image_name+" di "+username, e));
					}
				finally
					{
						MyDAO.closeQuery(pstatement);
					}
			}

		public String deleteImage(String user, String image_name) throws DbErrorException, BadFormatException
			{
				/*
				 * logica della cancellazione: cancella l'immagine e tutti gli album dell'utente che contengono solo quell'immagine
				 */
				String query = "SELECT path FROM " + MyDAO.IMAGES_TABLE_NAME + " WHERE creatore=? AND titolo=?;";

				String update = "DELETE FROM "+MyDAO.ALBUMS_TABLE_NAME+" WHERE "+
							  " (SELECT COUNT(*) FROM "+MyDAO.ADDITIONS_TABLE_NAME+" WHERE (nome=nome_album AND titolo_immagine=? AND creatore=?))>0"+
							  " AND "+
							  "((SELECT COUNT(*) FROM "+MyDAO.ADDITIONS_TABLE_NAME+" WHERE nome_album=nome)=1);";
				
				String update1 = "DELETE FROM " + MyDAO.IMAGES_TABLE_NAME + " WHERE creatore=? AND titolo=?;";
				
				PreparedStatement pstatement0 = null;
				PreparedStatement pstatement = null;
				PreparedStatement pstatement1 = null;
				ResultSet res0 = null;
				
				try
					{	
						this.connection.setAutoCommit(false);
						
						Image.checkImageIds(image_name, user);
						
						pstatement0 = connection.prepareStatement(query);
						pstatement0.setString(1, user);
						pstatement0.setString(2, image_name);
						res0 = pstatement0.executeQuery();
						res0.next();
						
						pstatement = connection.prepareStatement(update);
						pstatement.setString(1, image_name);
						pstatement.setString(2, user);
						pstatement.executeUpdate();
						pstatement1 = connection.prepareStatement(update1);
						pstatement1.setString(1, user);
						pstatement1.setString(2, image_name);
						pstatement1.executeUpdate();
						
						this.connection.commit();
						
						return (res0.getString("path"));
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
						
						throw (new DbErrorException("l'immagine "+image_name+" di "+user, e));
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
						
						MyDAO.closeQuery(res0, pstatement0);
						MyDAO.closeQuery(pstatement);
						MyDAO.closeQuery(pstatement1);
					}
			}
		public void uploadComment(Comment comment) throws DbErrorException, BadFormatException
			{
				String update = "INSERT INTO " + MyDAO.COMMENTS_TABLE_NAME + " VALUES(?,?,?,?,?)";

				PreparedStatement pstatement = null;

				try
					{
						this.connection.setAutoCommit(false);
						
						if (!comment.getImageCreator().equalsIgnoreCase(comment.getAuthor()))
							{
								this.isPrivate(comment.getImageName(), comment.getImageCreator());
							}
						
						pstatement = connection.prepareStatement(update);
						pstatement.setString(1, comment.getAuthor());
						pstatement.setString(2, comment.getImageName());
						pstatement.setString(3, comment.getImageCreator());
						pstatement.setTimestamp(4, comment.getTimestamp());
						pstatement.setString(5, comment.getText());
						
						pstatement.executeUpdate();
						
						this.connection.commit();

						return;
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
						
						throw (new DbErrorException("il commento", e));
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
					}
			}
	}