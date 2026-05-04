package Code;

public interface Batiment extends Item {
    
    // Retourne true si la santé est > 0[cite: 1, 2]
    boolean isOperationnel();

    // Ajoute un ouvrier au personnel si de la place est disponible[cite: 1, 2]
    void affecterPersonnel(Ouvrier ouvrier);

    // Retire un ouvrier et le rend disponible[cite: 1, 2]
    void retirerPersonnel(Ouvrier ouvrier);

    // Réduit la santé du bâtiment[cite: 1]
    void subirDegats(int quantite);

    // Augmente la santé sans dépasser le maximum[cite: 1]
    void reparer(int quantite);
}
