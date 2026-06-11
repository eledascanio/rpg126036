package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Salvataggio automatico a fine capitolo. Registrato come {@link GameListener}
 * sull'engine, salva la partita quando un capitolo si conclude, ossia dopo la
 * sequenza di fine capitolo (ripristino energia, scelta dell'upgrade e
 * avanzamento). L'utente non salva mai manualmente.
 *
 * <p>Un eventuale errore di scrittura non interrompe il gioco: viene inoltrato
 * all'handler degli errori.</p>
 */
public class AutoSaveListener implements GameListener {

    private final SaveRepository repository;
    private final GameState stato;
    private final Consumer<PersistenceException> onErrore;

    /**
     * Crea l'auto-salvataggio ignorando silenziosamente gli errori di scrittura.
     *
     * @param repository il repository su cui salvare (non nullo)
     * @param stato      lo stato della partita da salvare (non nullo)
     */
    public AutoSaveListener(SaveRepository repository, GameState stato) {
        this(repository, stato, errore -> {
        });
    }

    /**
     * Crea l'auto-salvataggio con un handler per gli errori di scrittura.
     *
     * @param repository il repository su cui salvare (non nullo)
     * @param stato      lo stato della partita da salvare (non nullo)
     * @param onErrore   handler invocato in caso di errore di salvataggio (non nullo)
     */
    public AutoSaveListener(SaveRepository repository, GameState stato,
                            Consumer<PersistenceException> onErrore) {
        this.repository = Objects.requireNonNull(repository, "Il repository non puo' essere nullo.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.onErrore = Objects.requireNonNull(onErrore, "L'handler degli errori non puo' essere nullo.");
    }

    @Override
    public void onChapterCompleted(Chapter capitolo) {
        try {
            repository.save(stato);
        } catch (PersistenceException e) {
            onErrore.accept(e);
        }
    }
}
