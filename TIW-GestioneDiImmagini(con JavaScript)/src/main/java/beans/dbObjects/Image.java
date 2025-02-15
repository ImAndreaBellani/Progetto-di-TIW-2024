package beans.dbObjects;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.List;
import java.util.StringTokenizer;

import beans.exceptions.BadFormatException;

public class Image implements Bean
	{
		private Date date;
		private String creator;
		private String name;
		private String text;
		private String path;
		private List<Comment> comments;
		
		public static String decode (String path) throws UnsupportedEncodingException
		/*
		 * si legga il paragrafo "1.3.3" della documentazione
		 */
			{
				StringTokenizer st = new StringTokenizer(path, "\\");
				
				String user = URLEncoder.encode(st.nextToken(), "UTF-8");
				String name = URLEncoder.encode(st.nextToken(), "UTF-8");
				String filename = URLEncoder.encode(st.nextToken(), "UTF-8").replace("+", "%20");
								
				return (user+"/"+name+"/"+filename);
			}
		
		public static String encode (String user, String name, String filename) throws UnsupportedEncodingException
		/*
		 * si legga il paragrafo "1.3.3" della documentazione
		 */	
			{
				try
					{
						user = URLEncoder.encode(user, "UTF-8");
						name = URLEncoder.encode(name, "UTF-8");
					}
				catch (UnsupportedEncodingException e)
					{
						throw (e);
					}
								
				return (user+"\\"+name+"\\"+filename);
			}
		
		public Image (Date date, String name, String creator, String text, String path, List<Comment> comments) throws BadFormatException
			{
				User.checkUsername(creator);
				Image.checkImageData(name, text);
				Image.checkImagePath(path);
				if (comments == null)
					{
						throw (new BadFormatException("comments", "", "La lista di commenti e' obbligatoria (al limite puo' essere vuota)")); 
					}
				this.creator = creator;
				this.name = name;
				this.date = date;
				this.text = text;
				this.path = path;
				this.comments = comments;
			}
		public static void checkImageIds (String name, String username) throws BadFormatException
			{
				Image.checkImageName(name);
				User.checkUsername(username);
			}
		
		public static void checkImageName (String name) throws BadFormatException
			{
				if (name==null || name.isEmpty())
					{
						throw (new BadFormatException("image_name", "", "Il nome dell'immagine e' un campo obbligatorio"));
					}
				if (name.codePointCount(0, name.length())>50)
					{
						throw (new BadFormatException("image_name", name, "Il nome di un'immagine non puo' superare i 50 caratteri"));
					}
			}
		public static void checkImageData (String name, String description) throws BadFormatException
			{
				Image.checkImageName(name);
				if (description==null || description.isEmpty())
					{
						throw (new BadFormatException("image_description", "", "La descrizione di un'immagine e' un campo obbligatorio"));
					}
				if (description.codePointCount(0, description.length())>500)
					{
						throw (new BadFormatException("image_description", description, "La descrizione di un'immagine non puo' superare i 500 caratteri"));
					}			
			}
		public static void checkImagePath (String path) throws BadFormatException
			{
				if (path==null || path.isEmpty())
					{
						throw (new BadFormatException("image_path", "", "Il path di un'immagine e' un campo obbligatorio"));
					}
				if (path.length()>500)
					{
						throw (new BadFormatException("image_path", path, "Il path di un'immagine non puo' superare i 500 caratteri"));
					}			
			}
		public void setPath (String path)
			{
				this.path = path;
			}
		public String getCreator ()
			{
				return this.creator;
			}
		public String getName ()
			{
				return this.name;
			}
		public Date getDate ()
			{
				return this.date;
			}
		public String getText ()
			{
				return this.text;
			}
		public String getPath ()
			{
				return this.path;
			}
		public List<Comment> getComments ()
			{
				return this.comments;
			}		
	}