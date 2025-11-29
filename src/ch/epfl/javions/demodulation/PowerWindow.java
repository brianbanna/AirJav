package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Classe qui représente une fenêtre de taille fixe sur une séquence
 * d'échantillons de puissance produits par un calculateur de puissance.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class PowerWindow {
    private static final int LIMIT = 1 << 16; // équivalent à Math.pow(2, 16) donc 2 puissance 16
    private final PowerComputer powerComputer;
    private final int windowSize;

    // Le tableau primaire
    private int[] mainTab = new int[LIMIT];

    // Le tableau secondaire
    private int[] secondaryTab = new int[LIMIT];
    private int windowPosition = 0;
    private long totalNumberElements;

    /**
     * Constructeur de PowerWindow qui retourne une fenêtre de taille
     * donnée sur la séquence d'échantillons de puissance calculés à partir
     * des octets fournis par le flot d'entrée donné.
     *
     * @param stream     flot des données.
     * @param windowSize taille de la fenêtre.
     * @throws IllegalArgumentException si la taille de la fenêtre n'est pas strictement positive
     *                                  ou si elle n'est pas inférieure ou égale à la taille limite
     * @throws IOException              si le flot n'est pas fermé.
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        boolean windowSizeIsValid = (windowSize > 0) && (windowSize <= LIMIT);
        Preconditions.checkArgument(windowSizeIsValid);

        this.windowSize = windowSize;
        powerComputer = new PowerComputer(stream, LIMIT);
        totalNumberElements = powerComputer.readBatch(mainTab);
    }

    /**
     * Retourne la taille de la fenêtre.
     *
     * @return la taille de la fenêtre.
     */
    public int size() {
        return windowSize;
    }

    /**
     * Retourne la position de la fenêtre.
     *
     * @return la position de la fenêtre.
     */
    public long position() {
        return windowPosition;
    }

    /**
     * Retourne vrai si et seulement si la fenêtre est pleine.
     *
     * @return false si la fin du flot d'échantillons a été atteinte, et que la fenêtre la dépasse
     * true si la la fenêtre est pleine
     */
    public boolean isFull() {
        return windowPosition + windowSize <= totalNumberElements;
    }

    /**
     * Retourne l'échantillon de puissance à l'index i.
     *
     * @param i l'index donné.
     * @return l'échantillon de puissance à l'index i.
     * @throws IndexOutOfBoundsException si i n'est pas compris entre 0 (inclus) et la taille de la fenêtre (exclu)
     */
    public int get(int i) {
        Objects.checkIndex(i, windowSize);

        // On fait (windowPosition % LIMIT) pour savoir si on a atteint le début du deuxième tableau
        int extractionPosition = (windowPosition % LIMIT) + i;

        /* Si on chevauche sur le second tableau, le tableau (donc si la position d'extraction est supérieure
         a la limite), le nouvel index d'extraction (donc dans le second tableau) sera l'ancien moins la limite */
        if (extractionPosition < LIMIT) {
            return mainTab[extractionPosition];
        } else {
            return secondaryTab[extractionPosition - LIMIT];
        }
    }

    /**
     * Permet de faire avancer la fenêtre d'un échantillon.
     *
     * @throws IOException en cas d'erreur d'entrée/sortie.
     */
    public void advance() throws IOException {
        windowPosition++; // On fait avancer la fenêtre d'une position

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Juste avant d'atteindre le deuxième tableau, on lit des valeurs dans le second tableau
    --------------------------------------------------------------------------------------------------------------------
    */
        /* On fait (windowPosition + windowSize - 1) pour lire des
        valeurs dans le deuxième tableau juste avant de l'atteindre */
        boolean startOfNewWindowIsAchieved = (windowPosition + windowSize - 1) % LIMIT == 0;
        if (startOfNewWindowIsAchieved) {
            totalNumberElements += powerComputer.readBatch(secondaryTab);
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Quand la fenêtre est totalement dans le second tableau, on permute les 2 tableaux
    --------------------------------------------------------------------------------------------------------------------
    */
        /* On fait (windowPosition % LIMIT) pour savoir si le début de la fenêtre est dans le second tableau
        donc si la fenêtre est totalement dans le second tableau */
        boolean windowIsFullyInSecondaryTab = (windowPosition % LIMIT) == 0;
        if (windowIsFullyInSecondaryTab) {
            int[] tempTab = secondaryTab.clone(); // On crée un tableau temporaire pour permuter les tableaux
            secondaryTab = mainTab;
            mainTab = tempTab;
        }
    }

    /**
     * Permet de faire avancer la fenêtre d'un nombre d'échantillons donné.
     *
     * @param offset le nombre d'échantillons donné.
     * @throws IOException              en cas d'erreur d'entrée/sortie.
     * @throws IllegalArgumentException si offset n'est pas positif ou nul.
     */
    public void advanceBy(int offset) throws IOException {
        boolean offsetIsPositive = offset >= 0;
        Preconditions.checkArgument(offsetIsPositive);

        // On applique la méthode "advance()" un nombre "offset" de fois.
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}
