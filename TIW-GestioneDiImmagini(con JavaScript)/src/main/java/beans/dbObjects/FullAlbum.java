package beans.dbObjects;

import java.sql.Date;
import java.util.List;

import beans.exceptions.BadFormatException;

public class FullAlbum implements Bean
/*
 * "Album" è una versione "ridotta" di "FullAlbum", contiene infatti solo i nomi e i path delle immagini
 */
	{		
		private Date date;
		private String name;
		private String creator;
		private List<Image> images;

		public FullAlbum (Date date, String name, String creator, List<Image> images) throws BadFormatException
			{
				Album.checkAlbumName(name);
				User.checkUsername(creator);
				
				if (images==null || images.size()==0)
					throw (new BadFormatException("images", "", "Non e' ammesso creare album vuoti"));
				
				this.creator = creator;
				this.date = date;
				this.name = name;
				this.images = images;
			}	
		public static void checkAlbumName (String name) throws BadFormatException
			{
				if (name==null || name.isEmpty())
					{
						throw (new BadFormatException("album_name", name, "Il nome dell'album e' un campo obbligatorio"));
					}
				if (name.codePointCount(0, name.length())>50)
					{
						throw (new BadFormatException("album_name", name, "Il nome di un album non puo' superare i 50 caratteri"));
					}
			}
		public String getCreator ()
			{
				return this.creator;
			}
		public Date getDate ()
			{
				return this.date;
			}
		public String getName ()
			{
				return this.name;
			}
		public List<Image> getImages ()
			{
				return this.images;
			}
	}
