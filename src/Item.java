public interface Item {
	/** L'abscisse de l'item */
	int x;
	/** L'ordonnée de l'item */
	int y;

	/** Retourne la distance entre cet item et un autre item.
	 * 
	 * @param item
	 * @return distance entre les deux items
	 */
	double distance(Item item);
	
	/** La méthode pour déplacer l'item avec des coordonnées absolues.
	 * 
	 * @param x
	 * @param y
	*/
	void deplacer(int x, int y);

	/** La méthode pour déplacer l'item avec une direction relative.
	 * 
	 * @param direction
	 */
	void deplacer(Direction direction);

}
