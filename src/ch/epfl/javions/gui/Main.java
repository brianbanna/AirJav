package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ch.epfl.javions.Units.Time.MILLISECONDS;
import static ch.epfl.javions.Units.Time.NANOSECONDS;

/**
 * Classe qui contient le programme principal.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class Main extends Application {
    // Constantes de configuration
    private static final String CACHE_DISK_DIRECTORY = "tile-cache";
    private static final String PROJECT_TITLE = "Javions";
    private static final String STREETMAP_WEBSITE = "tile.openstreetmap.org";
    private static final int INITIAL_LATITUDE = 23_070;
    private static final int INTIAL_LONGITUDE = 33_530;
    private static final int INITIAL_ZOOM = 8;
    private static final int MIN_HEIGHT = 600;
    private static final int MIN_WIDTH = 800;

    /**
     * Point d'entrée principal de l'application.
     *
     * @param args Les arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * Démarre l'application et configure la fenêtre principale avec les paramètres de la carte,
     * crée la base de données, les instances de AircraftController, AircraftTableController et StatusLineController,
     * crée l'affichage, lit les messages ADS-B, et anime les aéronefs.
     *
     * @param primaryStage La fenêtre principale de l'application.
     * @throws Exception En cas d'erreur lors du démarrage de l'application.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        /*
        --------------------------------------------------------------------------------------------------------------------
          -- Configuration de la fenêtre principale et des paramètres de la carte.
        --------------------------------------------------------------------------------------------------------------------
        */
        primaryStage.setTitle(PROJECT_TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        MapParameters mapParameters = new MapParameters(INITIAL_ZOOM, INTIAL_LONGITUDE, INITIAL_LATITUDE);
        BaseMapController baseMapController = new BaseMapController
                (new TileManager(Path.of(CACHE_DISK_DIRECTORY),
                        STREETMAP_WEBSITE),
                        mapParameters);


        /*
        --------------------------------------------------------------------------------------------------------------------
          -- Création de la base de données.
        --------------------------------------------------------------------------------------------------------------------
        */
        AircraftDatabase database = createAircraftDatabase();


        /*
        --------------------------------------------------------------------------------------------------------------------
          -- Création des instances de AircraftController, AircraftTableController, StatusLineController.
        --------------------------------------------------------------------------------------------------------------------
        */
        AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
        ObjectProperty<ObservableAircraftState> observableAircraftStateProperty = new SimpleObjectProperty<>();

        AircraftController aircraftController = new AircraftController(mapParameters,
                aircraftStateManager.states(),
                observableAircraftStateProperty);

        AircraftTableController aircraftTableController = new AircraftTableController(aircraftStateManager.states(),
                observableAircraftStateProperty);

        //
        aircraftTableController.setOnDoubleClick
                (c -> baseMapController.centerOn(
                        c.getPosition()));

        StatusLineController statusLineController = new StatusLineController();
        statusLineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));


        /*
        --------------------------------------------------------------------------------------------------------------------
          -- Création de l'affichage
        --------------------------------------------------------------------------------------------------------------------
        */
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(baseMapController.getPane(), aircraftController.pane());

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(aircraftTableController.pane());
        borderPane.setTop(statusLineController.pane());

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(stackPane, borderPane);

        primaryStage.setScene(new Scene(splitPane));
        primaryStage.show();

        // Création d'une file d'attente de messages partagée
        Queue<RawMessage> messageQueue = new ConcurrentLinkedQueue<>();

        // Obtention des arguments de la ligne de commande
        List<String> params = getParameters().getRaw();

        if (!params.isEmpty()) {
            String fileName = params.get(0);
            addMessagesFromFileToQueue(messageQueue, fileName);
        }
        InputStream messageSource = System.in;
        // Lecture à partir de System.in si aucun argument n'est fourni
        if (params.isEmpty()) {
            // Lancement du thread de lecture des messages
            Thread messageThread = new Thread(() -> {
                // Création du démodulateur ADS-B
                AdsbDemodulator demodulator;
                try {
                    demodulator = new AdsbDemodulator(messageSource);
                } catch (IOException e) {
                    System.err.println("Erreur lors de la création du démodulateur: " + e.getMessage());
                    return;
                }
                // Lecture des messages ADS-B
                try {
                    RawMessage message;
                    while ((message = demodulator.nextMessage()) != null) {
                        messageQueue.add(message);
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture des messages ADS-B: " + e.getMessage());
                }
            });
            messageThread.setDaemon(true);
            messageThread.start();
        }


        /*
        --------------------------------------------------------------------------------------------------------------------
          -- Animation des aéronefs.
        --------------------------------------------------------------------------------------------------------------------
        */
        animationTimerCreator(aircraftStateManager, statusLineController, messageQueue);
    }


    /**
     * Ajoute les messages lus à partir d'un fichier à une file de messages.
     * Les messages sont ajoutés de manière asynchrone dans un thread séparé.
     *
     * @param messageQueue la file de messages dans laquelle ajouter les messages lus.
     * @param fileName     le nom du fichier contenant les messages à lire.
     */
    private void addMessagesFromFileToQueue(Queue<RawMessage> messageQueue, String fileName) {
        long startTime = System.nanoTime();
        new Thread(() -> {
            try (DataInputStream s = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(fileName)))) {
                byte[] bytes = new byte[RawMessage.LENGTH];
                while (true) {
                    long timeStampNs = s.readLong();
                    int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                    assert bytesRead == RawMessage.LENGTH;
                    ByteString message = new ByteString(bytes);
                    if (timeStampNs > 0) {
                        long waitTime = timeStampNs - (System.nanoTime() - startTime); // Calcule le temps d'attente
                        if (waitTime > 0) { // Attend seulement si le temps d'attente est positif
                            try {
                                // Convertit le temps d'attente de nanosecondes à millisecondes
                                Thread.sleep((long) Units.convert(waitTime, NANOSECONDS, MILLISECONDS));
                            } catch (InterruptedException e) {
                                throw new Error(e);
                            }
                        }
                        messageQueue.add(new RawMessage(timeStampNs, message)); // Ajout du message à la queue
                    }
                }
            } catch (EOFException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * Crée un objet AnimationTimer pour gérer l'animation des états des aéronefs.
     *
     * @param aircraftStateManager L'objet AircraftStateManager qui gère les états des aéronefs.
     * @param statusLineController L'objet StatusLineController qui contrôle la ligne de statut.
     * @param messageQueue         La file d'attente des messages bruts à traiter.
     */
    private void animationTimerCreator(AircraftStateManager aircraftStateManager,
                                       StatusLineController statusLineController,
                                       Queue<RawMessage> messageQueue) {

        /* On utilise un tableau de long pour le temps initial parce qu'en Java, les variables locales utilisées
        dans une expression lambda (comme la méthode handle ici) doivent être finales ou effectivement finales,
        c'est-à-dire qu'elles ne peuvent pas être modifiées une fois assignées.
        En utilisant un tableau, on contourne cette restriction puisque la référence du tableau est finale,
        mais les éléments à l'intérieur du tableau (comme initialTime[0]) peuvent être modifiés librement. */

        final long[] initialTime = {System.nanoTime()}; // Le temps initial

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    while (!messageQueue.isEmpty()) {
                        RawMessage rawMessage = messageQueue.poll(); // Récupère le prochain message de la queue
                        if (rawMessage != null) {
                            // Décodage du message
                            Message msg = MessageParser.parse(rawMessage);
                            if (msg != null) {
                                // Incrémentation du compteur de messages dans le contrôleur de la ligne de statut
                                statusLineController.messageCountProperty().setValue(statusLineController.
                                        messageCountProperty().get() + 1);
                                aircraftStateManager.updateWithMessage(msg);
                            }
                        }
                    }
                    // Appel a purge chaque seconde.
                    if ((now - initialTime[0]) >= Units.convertTo(1, NANOSECONDS)) {
                        aircraftStateManager.purge();
                        initialTime[0] = now; // On "reset" le temps initial.
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }


    /**
     * Crée une base de données d'aéronefs.
     *
     * @return la base de données d'aéronefs créée.
     * @throws URISyntaxException si une erreur de syntaxe URI se produit lors de la création du chemin.
     */
    private AircraftDatabase createAircraftDatabase() throws URISyntaxException {
        URL url = getClass().getResource("/aircraft.zip");
        assert url != null;
        Path path = Path.of(url.toURI());
        return new AircraftDatabase(path.toString());
    }
}