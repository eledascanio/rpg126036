package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.view.CamerinoFilesApp;
import javafx.application.Application;

/**
 * Punto di ingresso dell'applicazione <i>Camerino Files</i>.
 *
 * <p>Funge da semplice launcher della GUI JavaFX: delega a
 * {@link Application#launch(Class, String...)}. Tenere il {@code main} in una
 * classe che <b>non</b> estende {@link Application} evita l'errore "JavaFX runtime
 * components are missing" quando l'app viene avviata dal classpath.</p>
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(CamerinoFilesApp.class, args);
    }
}
