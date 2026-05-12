package entites;

public enum EtatOuvrier {
		MALADE 	 	 (0.01), //TODO à revoir
        TRES_FATIGUE (0.3),
        FATIGUE      (0.8),
        NORMAL       (1.0),
        MOTIVE       (1.2),
        TRES_MOTIVE  (1.7);

        public final double multiplicateur;

        EtatOuvrier(double multiplicateur) {
            this.multiplicateur = multiplicateur;
        }
    }