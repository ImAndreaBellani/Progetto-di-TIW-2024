package beans.dbObjects;

import java.sql.Timestamp;

import beans.exceptions.BadFormatException;

public class Comment implements Bean
	{		
		private String author;
		private String image_name;
		private String image_creator;
		private String text;
		private Timestamp timestamp;

		public Comment (String author, String image_name, String image_creator, Timestamp timestamp, String text) throws BadFormatException
			{
				Comment.checkCommentData(author, image_name, image_creator, text);
				this.author = author;
				this.image_name = image_name;
				this.image_creator = image_creator;
				this.text = text;
				this.timestamp = timestamp;
			}
		public static void checkCommentData (String author, String image_name, String image_creator, String text) throws BadFormatException
			{
				User.checkUsername(author);
				User.checkUsername(image_creator);
				Image.checkImageName(image_name);
				
				if (text==null || text.isEmpty())
					{
						throw (new BadFormatException("comment_body", "", "Non e' possibile inviare commenti vuoti"));
					}
				if (text.codePointCount(0, text.length())>500)
					{
						throw (new BadFormatException("comment_body", text, "Un commento non puo' superare i 500 caratteri"));
					}
			}
		public String getAuthor()
			{
				return this.author;
			}
		public String getImageName()
			{
				return this.image_name;
			}
		public String getImageCreator()
			{
				return this.image_creator;
			}
		public String getText()
			{
				return this.text;
			}
		public Timestamp getTimestamp()
			{
				return this.timestamp;
			}
	}