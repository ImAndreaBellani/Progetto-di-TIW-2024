package beans.exceptions;

/**
 * Tutti gli errori sintattici vengono segnalati mediante un'eccezione "BadFormatException", la quale dispone di tre attributi configurabili:
 * -> affected_value : il nome del campo interessato dall'errore;
 * -> error_description : una descrizione dell'errore segnalato;
 * -> given_value: il valore che ha scatenato l'errore.
 */
public class BadFormatException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		private String affected_value;
		private String error_description;
		private String given_value;
		
		public BadFormatException(String affected_value, String given_value, String error_description)
			{
				this.affected_value = affected_value;
				this.given_value = given_value;
				this.error_description = error_description;
			}
		
		public String getAffectedValue ()
			{
				return this.affected_value;
			}
		public String getGivenValue ()
			{
				return this.given_value;
			}
		public String getErrorDescription ()
			{
				return this.error_description;
			}
		public String toString()
			{
				return (this.error_description);
			}
		public String getMessage()
			{
				return (this.toString());
			}
	}