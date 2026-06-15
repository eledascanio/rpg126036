package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.achievement.AchievementManager;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.persistence.AutoSaveListener;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.GameOverCleanupListener;
import it.unicam.cs.mpgc.rpg126036.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Costruisce una {@link GameSession} pronta all'uso, assemblando i componenti del
 * gioco e registrando sul motore i listener standard di una partita:
 * <ul>
 *     <li>{@link AchievementManager} per i traguardi;</li>
 *     <li>{@link AutoSaveListener} per il salvataggio automatico a fine capitolo;</li>
 *     <li>{@link GameOverCleanupListener} per liberare lo slot a fine partita.</li>
 * </ul>
 *
 * <p>Concentra qui il cablaggio (composition root della partita): aggiungere un
 * nuovo osservatore di sistema richiede solo registrarlo in questo punto.</p>
 */
public class GameSessionFactory {

    private final SaveRepository repository;
    private final Campaign campaign;

    /**
     * @param repository il repository dei salvataggi (non nullo)
     * @param campaign   la campagna da giocare (non nulla)
     */
    public GameSessionFactory(SaveRepository repository, Campaign campaign) {
        this.repository = Objects.requireNonNull(repository, "Il repository non puo' essere nullo.");
        this.campaign = Objects.requireNonNull(campaign, "La campagna non puo' essere nulla.");
    }

    /**
     * Avvia una nuova partita creando il personaggio e assemblando la sessione dal
     * primo capitolo della campagna.
     *
     * @param nomePersonaggio nome del personaggio (non nullo)
     * @param classe          classe scelta (non nulla)
     * @return la sessione di gioco pronta
     */
    public GameSession nuovaPartita(String nomePersonaggio, CharacterClass classe) {
        // La campagna (e i suoi Chapter) è condivisa e viene riusata a ogni partita:
        // azzera il progresso ereditato da una run precedente, così la nuova partita
        // riparte davvero dalla prima scena di ogni capitolo.
        campaign.getCapitoli().forEach(Chapter::reset);
        Player player = new Player(nomePersonaggio, classe);
        return assembla(new GameState(player), campaign.getCapitoli(), 0);
    }

    /**
     * Riprende una partita salvata dallo slot del personaggio indicato.
     *
     * @param nomePersonaggio nome del personaggio/slot da caricare (non nullo)
     * @return la sessione ripristinata, o {@link Optional#empty()} se lo slot non esiste
     * @throws PersistenceException se il salvataggio esiste ma e' illeggibile o corrotto
     */
    public Optional<GameSession> caricaPartita(String nomePersonaggio) {
        Objects.requireNonNull(nomePersonaggio, "Il nome non puo' essere nullo.");
        return repository.load(nomePersonaggio).map(this::riprendi);
    }

    /**
     * Riconcilia lo stato salvato con la campagna: individua il capitolo corrente
     * nella sequenza e ne usa la versione con il progresso salvato, poi assembla la
     * sessione a partire da quel capitolo.
     */
    private GameSession riprendi(GameState statoSalvato) {
        Chapter capitoloSalvato = statoSalvato.getCapitoloCorrente()
                .orElseThrow(() -> new PersistenceException("Salvataggio privo di capitolo corrente."));
        List<Chapter> capitoli = new ArrayList<>(campaign.getCapitoli());
        int indice = indiceDi(capitoli, capitoloSalvato.getId());
        // Sostituisce il capitolo "fresco" con quello che porta il progresso salvato.
        capitoli.set(indice, capitoloSalvato);
        return assembla(statoSalvato, capitoli, indice);
    }

    /**
     * @return l'indice del capitolo con l'id indicato nella campagna
     * @throws PersistenceException se la campagna non contiene quel capitolo
     */
    private int indiceDi(List<Chapter> capitoli, String idCapitolo) {
        for (int i = 0; i < capitoli.size(); i++) {
            if (capitoli.get(i).getId().equals(idCapitolo)) {
                return i;
            }
        }
        throw new PersistenceException("Il capitolo del salvataggio non e' nella campagna: " + idCapitolo);
    }

    /**
     * Assembla la sessione su uno stato e una sequenza di capitoli, registrando i
     * listener di sistema e posizionando il motore sul capitolo indicato.
     */
    private GameSession assembla(GameState stato, List<Chapter> capitoli, int indiceIniziale) {
        GameEngine engine = new GameEngine(stato, capitoli, indiceIniziale);
        AchievementManager achievementManager = new AchievementManager(stato);
        engine.addListener(achievementManager);
        engine.addListener(new AutoSaveListener(repository, stato));
        engine.addListener(new GameOverCleanupListener(repository, stato));
        return new GameSession(engine, stato, achievementManager, campaign);
    }
}
