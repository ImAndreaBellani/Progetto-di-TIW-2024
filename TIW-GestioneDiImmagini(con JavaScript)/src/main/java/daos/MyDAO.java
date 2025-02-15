package daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import beans.exceptions.DbErrorException;

public interface MyDAO
	{
		public final String DB_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
		
		public final String DB_URL = "jdbc:mysql://127.0.0.1:3306/db_for_js_version";
		public final String DB_NAME = "db_for_js_version";
		public final String DB_ADMIN = "root";
		public final String DB_ADMIN_PASSWORD = "Vulcania";

		public final String ALBUMS_TABLE_NAME = DB_NAME+".album";
		public final String IMAGES_TABLE_NAME = DB_NAME+".immagini";
		public final String USERS_TABLE_NAME = DB_NAME+".utenti";
		public final String COMMENTS_TABLE_NAME = DB_NAME+".commenti";
		public final String ADDITIONS_TABLE_NAME = DB_NAME+".aggiunte";
		public final String SORTINGS_TABLE_NAME = DB_NAME+".ordinamenti";

		public static void closeQuery (ResultSet result, PreparedStatement pstatement) throws DbErrorException
			{
				if (result!=null)
					{
						try
							{
								result.close();
							}
						catch (Exception e)
							{
								throw (new DbErrorException());
							}
					}
				
				MyDAO.closeQuery(pstatement);	
			}
		public static void closeQuery (PreparedStatement pstatement) throws DbErrorException
			{
				if (pstatement!=null)
					{
						try
							{
								pstatement.close();
							}
						catch (Exception e)
							{
								throw (new DbErrorException());
							}
					}
			}
	}