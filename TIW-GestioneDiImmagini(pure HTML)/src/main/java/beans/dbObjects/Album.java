package beans.dbObjects;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import beans.exceptions.BadFormatException;
import beans.utils.Pair;

public class Album implements Bean
	{		
		private Date date;
		private String name;
		private String creator;
		private List<Pair<String>> images_infos;

		public Album (Date date, String name, String creator, List<Pair<String>> images_infos) throws BadFormatException
			{
				Album.checkAlbumName(name);
				User.checkUsername(creator);
				for (Pair<String> image : images_infos)
					{
						Image.checkImageName(image.getElem1());
						Image.checkImagePath(image.getElem2());
					}
				
				if (images_infos==null || images_infos.size()==0)
					throw (new BadFormatException("images_infos", "", "Non e' ammesso creare album vuoti"));
				
				this.creator = creator;
				this.date = date;
				this.name = name;
				this.images_infos = images_infos;
			}
		public Album (String[] images_names, Date date, String name, String creator) throws BadFormatException
			{
				Album.checkAlbumName(name);
				User.checkUsername(creator);
				
				if (images_names==null || images_names.length==0)
					throw (new BadFormatException("images_names", "", "Non e' ammesso creare album vuoti"));
				
				this.creator = creator;
				this.date = date;
				this.name = name;
				
				this.images_infos = new ArrayList<>();
				for (String image_name : images_names)
					{
						Image.checkImageName(image_name);
						this.images_infos.add(new Pair<String>(image_name, ""));
					}
			}
		public static int fixStart (String start, int max_length)
			{
				 try
				 	{
					 	int start_number = Integer.parseInt(start);
					    
					    if (start_number<0) //un valore minore di zero viene trattato come una richiesta alla prima pagina del carosello
					    	return (0);
					    
					 	int final_length;
					 	if (max_length%5 == 0)
						 	{
					 			final_length=max_length;
						 	}
					 	else
						 	{
					 		 	final_length = max_length-max_length%5+5;
						 	}
					 	
					    if (start_number>=max_length)
					    	return (final_length-5); //un valore superiore all'ultimo assegnabile viene trattato
					    											  //come se venisse chiesta l'ultima pagina del carosello
					    
					    return(start_number - ((start_number)%5)); //qualunque valore viene "arrotondato" per difetto al multiplo di "5" più "vicino"
					}
				 catch (NumberFormatException e) //posto a "0" di default se viene dato un valore non numerico
				 	{
					    return 0;
					}
			}
		public static void checkAlbumName (String name) throws BadFormatException
			{
				if (name==null || name.isEmpty())
					{
						throw (new BadFormatException("album_name", "", "Il nome dell'album e' un campo obbligatorio"));
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
		public List<Pair<String>> getImagesInfos ()
			{
				return this.images_infos;
			}
		public List<Pair<String>> getImages(int start, int end)
			{
				int real_end = (end>this.images_infos.size()?this.images_infos.size():end);
				
				List<Pair<String>> ret = new ArrayList<>();
				
				for (int i = start ; i < real_end ; i++)
					{
						ret.add(this.images_infos.get(i));
					}
				for (int i = real_end ; i < end ; i++)
					{
						ret.add(new Pair<>(null, null));
					}
				
				return (ret);
			}
	}