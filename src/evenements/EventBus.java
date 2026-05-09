package evenements;

import java.util.*;

public class EventBus implements EventListener {

    // Mapping canal -> liste d'abonnés
    private final Map<Canal, List<EventListener>> abonnes = new EnumMap<>(Canal.class);

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

    /** Implémentation de EventListener : l'EventBus peut lui-même réagir à des événements. */
    @Override
    public void surEvenement(Evenement evenement) {
        // À définir selon les besoins (ex: routage automatique)
    }
}