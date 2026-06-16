package it.unicam.cs.mpgc.rpg126036.achievement;

import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Gestore globale dei traguardi del gioco. Registrato come {@link GameListener}
 * sull'engine (pattern Observer), osserva gli eventi di gioco e sblocca gli
 * {@link Achievement} al verificarsi delle relative condizioni.
 *
 * <p>Mantiene il registro di tutti i traguardi disponibili e l'insieme di quelli
 * gia' sbloccati, garantendo che ciascun traguardo venga sbloccato una sola
 * volta. Lo sblocco e' notificato agli {@link AchievementListener} registrati,
 * cosi' che la vista possa mostrarlo.</p>
 *
 * <p>Primo traguardo previsto: la raccolta della chiave nel Capitolo 1.</p>
 */
public class AchievementManager implements GameListener {

    /** Id del capitolo in cui la chiave sblocca il primo traguardo. */
    private static final String ID_CAPITOLO_UNO = "capitolo1";

    /** Id del capitolo in cui si affronta il PC della vittima. */
    private static final String ID_CAPITOLO_DUE = "capitolo2";

    private final GameState stato;
    private final Map<String, Achievement> registro = new LinkedHashMap<>();
    private final Set<String> sbloccati = new LinkedHashSet<>();
    private final List<AchievementListener> osservatori = new ArrayList<>();

    /**
     * Crea il gestore inizializzato con tutti i traguardi del
     * {@link AchievementCatalog}.
     *
     * @param stato lo stato della partita, da cui leggere il capitolo corrente (non nullo)
     */
    public AchievementManager(GameState stato) {
        this(stato, AchievementCatalog.tutti());
    }

    /**
     * Crea il gestore con un insieme esplicito di traguardi (utile per i test).
     *
     * @param stato     lo stato della partita (non nullo)
     * @param traguardi i traguardi da registrare (non nulli)
     */
    public AchievementManager(GameState stato, Collection<Achievement> traguardi) {
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        Objects.requireNonNull(traguardi, "I traguardi non possono essere nulli.");
        for (Achievement achievement : traguardi) {
            registro.put(achievement.id(), achievement);
        }
    }

    // ----------------------------------------------------------------------
    // Registrazione osservatori
    // ----------------------------------------------------------------------

    /**
     * Registra un osservatore degli sblocchi dei traguardi.
     *
     * @param osservatore l'osservatore (non nullo)
     */
    public void addAchievementListener(AchievementListener osservatore) {
        osservatori.add(Objects.requireNonNull(osservatore, "L'osservatore non puo' essere nullo."));
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     *
     * @param osservatore l'osservatore da rimuovere
     * @return {@code true} se era registrato
     */
    public boolean removeAchievementListener(AchievementListener osservatore) {
        return osservatori.remove(osservatore);
    }

    // ----------------------------------------------------------------------
    // Eventi di gioco osservati
    // ----------------------------------------------------------------------

    /**
     * Sblocca il traguardo della chiave quando questa viene raccolta nel
     * Capitolo 1.
     */
    @Override
    public void onItemFound(Item item) {
        if (item != null
                && ItemCatalog.ID_CHIAVE_CAPITOLO1.equals(item.id())
                && isCapitoloCorrente(ID_CAPITOLO_UNO)) {
            sblocca(AchievementCatalog.ID_CHIAVE_CAPITOLO1);
        }
    }

    /**
     * Segnala che il giocatore ha individuato il PC della vittima al primissimo
     * click, senza aver esaminato alcun computer sbagliato: sblocca il relativo
     * traguardo se ci si trova nel Capitolo 2. La condizione "primo colpo" e'
     * verificata dalla vista, che invoca questo metodo solo quando soddisfatta.
     */
    public void segnalaPcVittimaAlPrimoColpo() {
        if (isCapitoloCorrente(ID_CAPITOLO_DUE)) {
            sblocca(AchievementCatalog.ID_PC_PRIMO_COLPO);
        }
    }

    // ----------------------------------------------------------------------
    // Stato dei traguardi
    // ----------------------------------------------------------------------

    /**
     * @param idAchievement id del traguardo
     * @return {@code true} se il traguardo e' stato sbloccato
     */
    public boolean isSbloccato(String idAchievement) {
        return sbloccati.contains(idAchievement);
    }

    /**
     * @return tutti i traguardi del gioco, in sola lettura
     */
    public Collection<Achievement> getTutti() {
        return Collections.unmodifiableCollection(registro.values());
    }

    /**
     * @return i traguardi sbloccati, nell'ordine di sblocco e in sola lettura
     */
    public List<Achievement> getSbloccati() {
        List<Achievement> risultato = new ArrayList<>();
        for (String id : sbloccati) {
            risultato.add(registro.get(id));
        }
        return Collections.unmodifiableList(risultato);
    }

    // ----------------------------------------------------------------------
    // Logica interna
    // ----------------------------------------------------------------------

    /**
     * Sblocca il traguardo indicato, se registrato e non gia' sbloccato,
     * notificando gli osservatori.
     */
    private void sblocca(String idAchievement) {
        Achievement achievement = registro.get(idAchievement);
        if (achievement != null && sbloccati.add(idAchievement)) {
            osservatori.forEach(o -> o.onAchievementSbloccato(achievement));
        }
    }

    /**
     * @return {@code true} se il capitolo corrente ha l'id indicato
     */
    private boolean isCapitoloCorrente(String idCapitolo) {
        return stato.getCapitoloCorrente()
                .map(Chapter::getId)
                .filter(idCapitolo::equals)
                .isPresent();
    }
}
