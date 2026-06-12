package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSessionFactory;

import java.util.Objects;

/**
 * Servizi condivisi a disposizione delle viste: la navigazione tra schermate e la
 * factory con cui creare o riprendere le partite. Viene costruito una sola volta
 * all'avvio e passato alle viste, evitando che ciascuna ricrei repository e
 * campagna.
 */
public class AppContext {

    private final ViewNavigator navigator;
    private final GameSessionFactory sessionFactory;

    /**
     * @param navigator      il gestore della navigazione (non nullo)
     * @param sessionFactory la factory delle sessioni di gioco (non nulla)
     */
    public AppContext(ViewNavigator navigator, GameSessionFactory sessionFactory) {
        this.navigator = Objects.requireNonNull(navigator, "Il navigator non puo' essere nullo.");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "La factory non puo' essere nulla.");
    }

    public ViewNavigator navigator() {
        return navigator;
    }

    public GameSessionFactory sessionFactory() {
        return sessionFactory;
    }
}
