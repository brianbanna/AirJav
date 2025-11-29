package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Classe qui gère la ligne d'état.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class StatusLineController {
    private final BorderPane pane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;

    /**
     * Constructeur public de la classe StatusLineController.
     */
    public StatusLineController() {
        aircraftCountProperty = new SimpleIntegerProperty();
        messageCountProperty = new SimpleLongProperty();

        pane = new BorderPane();
        pane.getStyleClass().add("status.css");

        Text aircraftCount = new Text(); // Texte qui indique Le nombre d'aéronefs visibles.
        Text messageCount = new Text(); // Texte qui indique le nombre de messages reçus.
        aircraftCount.textProperty().bind
                (Bindings.concat("Aéronefs visibles : ", aircraftCountProperty));
        messageCount.textProperty().bind
                (Bindings.concat("Messages reçus : ", messageCountProperty));

        pane.setLeft(aircraftCount);
        pane.setRight(messageCount);
    }

    /**
     * Méthode qui retourne le panneau contenant la ligne d'état.
     *
     * @return le panneau contenant la ligne d'état.
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Méthode qui retourne la propriété contenant le nombre d'aéronefs actuellement visibles.
     *
     * @return la propriété contenant le nombre d'aéronefs actuellement visibles.
     */
    public IntegerProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    /**
     * Méthode qui retourne la propriété contenant le nombre de
     * messages reçus depuis le début de l'exécution du programme.
     *
     * @return la propriété contenant le nombre de messages reçus depuis le début de l'exécution du programme.
     */
    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }
}
