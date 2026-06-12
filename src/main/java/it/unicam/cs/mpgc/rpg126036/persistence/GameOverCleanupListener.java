package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.engine.GameSummary;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Elimina lo slot di salvataggio quando la partita termina. Registrato come
 * {@link GameListener} sull'engine, cancella il salvataggio del personaggio sia
 * alla sconfitta ({@code onGameOver}) sia al completamento vittorioso
 * ({@code onGameCompleted}): in entrambi i casi la partita non e' piu'
 * riprendibile e lo slot viene liberato.
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
        cancellaSlot();
    }

    @Override
    public void onGameCompleted(GameSummary riepilogo) {
        cancellaSlot();
    }

    /**
     * Cancella lo slot del personaggio, inoltrando all'handler un eventuale errore.
     */
    private void cancellaSlot() {
        try {
            repository.delete(stato.getPlayer().getNome());
        } catch (PersistenceException e) {
            onErrore.accept(e);
        }
    }
}
