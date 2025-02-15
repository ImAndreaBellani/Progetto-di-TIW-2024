package beans.dbObjects;

import beans.exceptions.BadFormatException;
import org.apache.commons.validator.routines.*;

public class User implements Bean
	{				
		private String username;
		private String password;
		private String mail;
		
		public User (String username, String password, String password1, String mail) throws BadFormatException
			{
				checkFields(username, password, password1, mail);
				
				this.username = username;
				this.password = password;
				this.mail = mail;
			}
		public static void checkUsername (String username) throws BadFormatException
			{
				if (username==null || username.isEmpty())
					{
						throw (new BadFormatException("username", "", "Lo username e' un campo obbligatorio"));
					}
				if (username.codePointCount(0, username.length())>50)
					{
						throw (new BadFormatException("username", username, "Lo username non puo' superare i 50 caratteri"));
					}
			}
		public static void checkPassword (String password) throws BadFormatException
			{
				if (password==null || password.isEmpty())
					{
						throw (new BadFormatException("password", "", "La password e' un campo obbligatorio"));
					}
				if (password.codePointCount(0, password.length())>50)
					{
						throw (new BadFormatException("password", "PASSWORD", "La password non puo' superare i 50 caratteri"));
					}
			}
		public static void checkPasswordMismatch (String password, String password1) throws BadFormatException
			{
				if (!password.equals(password1))
					{
						throw (new BadFormatException("password", "PASSWORD", "Le password non corrispondono"));
					}
			}
		public static void checkMail (String mail) throws BadFormatException
			{
				if (mail==null || mail.isEmpty())
					{
						throw (new BadFormatException("mail", "", "L'indirizzo mail e' un campo obbligatorio"));
					}
								
				if (!EmailValidator.getInstance().isValid(mail))
					{
						throw (new BadFormatException("mail", mail, "L'indirizzo mail fornito ha una sintassi errata"));
					}				
			}
		public static void checkFields (String username, String password, String password1, String mail) throws BadFormatException
			{
				User.checkUsername(username);
				User.checkPassword(password);
				User.checkPasswordMismatch(password, password1);
				User.checkMail(mail);	
			}
		public String getUsername ()
			{
				return this.username;
			}
		public String getPassword ()
			{
				return this.password;
			}
		public String getMail ()
			{
				return this.mail;
			}
	}
