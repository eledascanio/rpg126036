package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.app.GameSessionFactory;

import java.util.Objects;

/**
 * Servizi condivisi a disposizione delle viste: la navigazione tra schermate, la
 * factory con cui creare o riprendere le partite e il risolutore dei contenuti di
 * scena. Viene costruito una sola volta all'avvio e passato alle viste, evitando
 * che ciascuna ricrei repository, campagna e cataloghi.
 */
public class AppContext {

    private final ViewNavigator navigator;
    private final GameSessionFactory sessionFactory;
    private final ContentResolver contentResolver;

    /**
     * @param navigator       il gestore della navigazione (non nullo)
     * @param sessionFactory  la factory delle sessioni di gioco (non nulla)
     * @param contentResolver il risolutore dei contenuti di scena (non nullo)
     */
    public AppContext(ViewNavigator navigator, GameSessionFactory sessionFactory,
                      ContentResolver contentResolver) {
        this.navigator = Objects.requireNonNull(navigator, "Il navigator non puo' essere nullo.");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "La factory non puo' essere nulla.");
        this.contentResolver = Objects.requireNonNull(contentResolver, "Il risolutore non puo' essere nullo.");
    }

    public ViewNavigator navigator() {
        return navigator;
    }

    public GameSessionFactory sessionFactory() {
        return sessionFactory;
    }

    public ContentResolver contentResolver() {
        return contentResolver;
    }
}
