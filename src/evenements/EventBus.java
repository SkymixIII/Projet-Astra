package evenements;

import java.util.*;

import entites.Ouvrier;
import ressources.Stock;
import jeu.Temps;

public class EventBus implements EventListener {

    // Mapping canal -> liste d'abonnés
    private final Map<Canal, List<EventListener>> abonnes = new EnumMap<>(Canal.class);

	private final Random random = new Random();
	// Seuils de déclenchement pour les événements aléatoires
	private static final double SEUIL_TEMPETE = 0.15;
	private static final double SEUIL_GREVE   = 0.10;

    public EventBus() {
        for (Canal canal : Canal.values()) {
            abonnes.put(canal, new ArrayList<>());
        }
    }

    /** Connecte un composant au bus sur un canal donné. */
    public void sEnregistrer(Canal canal, EventListener listener) {
        abonnes.get(canal).add(listener);
    }

    /** Déconnecte un composant d'un canal. */
    public void seDesinscrire(Canal canal, EventListener listener) {
        abonnes.get(canal).remove(listener);
    }

    /** Diffuse l'événement à tous les abonnés du canal. */
    public void publier(Canal canal, Evenement evenement) {
        List<EventListener> liste = abonnes.get(canal);
        for (EventListener listener : liste) {
            listener.surEvenement(evenement);
        }
    }

    /** Vérifie la présence d'un abonné sur un canal. */
    public boolean possedeAbonne(Canal canal, EventListener listener) {
        return abonnes.get(canal).contains(listener);
    }

    /** Implémentation de EventListener : l'EventBus peut lui-même réagir à des événements. 
	 * @param evenement L'événement reçu, à traiter selon les besoins
	*/
    @Override
    public void surEvenement(Evenement evenement) {
        // À définir selon les besoins (ex: routage automatique)
    }

	/** Traite les événements aléatoires selon le temps, le stock et les ouvriers. 
	 * @param temps Le temps actuel du jeu (ticks)
	 * @param stock Le stock de ressources actuel
	 * @param ouvriers La liste des ouvriers actifs	
	*/
	public void traiter(Temps temps, Stock stock, List<Ouvrier> ouvriers) {
		double chance = random.nextDouble();

		if (chance < SEUIL_TEMPETE) {
			Tempete tempete = new Tempete(1 + random.nextInt(5));
			publier(Canal.ENVIRONNEMENT, tempete);
		}

		if (chance < SEUIL_GREVE) {
			// Grève si moral ou logement insuffisant — à implémenter
			// publier(Canal.SOCIAL, new Greve(...));
		}
	}
}