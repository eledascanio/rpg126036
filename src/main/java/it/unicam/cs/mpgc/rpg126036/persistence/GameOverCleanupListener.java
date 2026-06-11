package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Elimina lo slot di salvataggio quando la partita termina. Registrato come
 * {@link GameListener} sull'engine, su {@code onGameOver} cancella il salvataggio
 * del personaggio, liberando lo slot: la partita conclusa (per sconfitta o
 * vittoria) non e' piu' riprendibile.
 *
 * <p>Un eventuale errore di cancellazione non interrompe il gioco: viene
 * inoltrato all'handler degli errori.</p>
 */
public class GameOverCleanupListener implements GameListener {

    private final SaveRepository repository;
    private final GameState stato;
    private final Consumer<PersistenceException> onErrore;

    /**
     * Crea il listener ignorando silenziosamente gli errori di cancellazione.
     *
     * @param repository il repository su cui operare (non nullo)
     * @param stato      lo stato della partita (non nullo)
     */
    public GameOverCleanupListener(SaveRepository repository, GameState stato) {
        this(repository, stato, errore -> {
        });
    }

    /**
     * Crea il listener con un handler per gli errori di cancellazione.
     *
     * @param repository il repository su cui operare (non nullo)
     * @param stato      lo stato della partita (non nullo)
     * @param onErrore   handler invocato in caso di errore (non nullo)
     */
    public GameOverCleanupListener(SaveRepository repository, GameState stato,
                                   Consumer<PersistenceException> onErrore) {
        this.repository = Objects.requireNonNull(repository, "Il repository non puo' essere nullo.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.onErrore = Objects.requireNonNull(onErrore, "L'handler degli errori non puo' essere nullo.");
    }

    @Override
    public void onGameOver() {
        try {
            repository.delete(stato.getPlayer().getNome());
        } catch (PersistenceException e) {
            onErrore.accept(e);
        }
    }
}
