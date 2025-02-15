package beans.utils;

/**
 * Classe di utilità per rappresentare coppie di dati di tipo diverso
 * @param <T> tipo primo elemento della coppia
 * @param <U> tipo secondo elemento della coppia
 */
public class DiffPair<T, U>
	{
		private T elem1;
		private U elem2;
		
		public DiffPair(T elem1, U elem2)
			{
				this.elem1 = elem1;
				this.elem2 = elem2;
			}
		public T getElem1()
			{
				return this.elem1;
			}
		public U getElem2()
			{
				return this.elem2;
			}
		public void setElem1 (T elem1)
			{
				this.elem1 = elem1;
			}
		public void setElem2 (U elem2)
			{
				this.elem2 = elem2;
			}
	}