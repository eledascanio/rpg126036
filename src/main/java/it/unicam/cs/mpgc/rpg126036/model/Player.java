package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Rappresenta il personaggio giocante. Mantiene le statistiche correnti
 * (derivate dalla {@link CharacterClass} di appartenenza), l'energia
 * disponibile per compiere azioni e i punti esperienza (XP) accumulati.
 *
 * <p>L'XP funge da valuta per potenziare le statistiche: ogni potenziamento
 * di un punto consuma {@link #COSTO_XP_POTENZIAMENTO} punti esperienza.</p>
 */
public class Player {

    /** Energia massima (e iniziale) di un personaggio. */
    public static final int ENERGIA_MASSIMA = 100;

    /** XP necessari per aumentare di 1 una statistica. */
    public static final int COSTO_XP_POTENZIAMENTO = 100;

    private final String nome;
    private final CharacterClass classe;
    private final Map<StatType, Integer> statistiche;

    private int energia;
    private int xp;

    /**
     * Crea un nuovo personaggio con le statistiche iniziali della classe scelta,
     * energia al massimo e nessun XP accumulato.
     *
     * @param nome   il nome del personaggio (non nullo)
     * @param classe la classe di appartenenza (non nulla)
     */
    public Player(String nome, CharacterClass classe) {
        this.nome = Objects.requireNonNull(nome, "Il nome non puo' essere nullo.");
        this.classe = Objects.requireNonNull(classe, "La classe non puo' essere nulla.");
        // Copia delle statistiche iniziali della classe: il Player le evolve in autonomia.
        this.statistiche = new EnumMap<>(classe.getStatisticheIniziali());
        this.energia = ENERGIA_MASSIMA;
        this.xp = 0;
    }

    // ----------------------------------------------------------------------
    // Energia
    // ----------------------------------------------------------------------

    /**
     * Tenta di consumare la quantita' di energia indicata.
     *
     * @param quantita energia da consumare (non negativa)
     * @return {@code true} se l'energia era sufficiente ed e' stata consumata,
     *         {@code false} se insufficiente (in tal caso l'energia resta invariata)
     */
    public boolean consumaEnergia(int quantita) {
        if (quantita < 0) {
            throw new IllegalArgumentException("La quantita' di energia da consumare non puo' essere negativa.");
        }
        if (quantita > energia) {
            return false;
        }
        energia -= quantita;
        return true;
    }

    /**
     * Ripristina energia senza superare il massimo consentito.
     *
     * @param quantita energia da ripristinare (non negativa)
     */
    public void ripristinaEnergia(int quantita) {
        if (quantita < 0) {
            throw new IllegalArgumentException("La quantita' di energia da ripristinare non puo' essere negativa.");
        }
        energia = Math.min(ENERGIA_MASSIMA, energia + quantita);
    }

    /**
     * @param quantita energia richiesta
     * @return {@code true} se il personaggio dispone di energia sufficiente
     */
    public boolean haEnergiaSufficiente(int quantita) {
        return energia >= quantita;
    }

    // ----------------------------------------------------------------------
    // Esperienza e potenziamento
    // ----------------------------------------------------------------------

    /**
     * Accumula punti esperienza.
     *
     * @param quantita XP da aggiungere (non negativa)
     */
    public void aggiungiXp(int quantita) {
        if (quantita < 0) {
            throw new IllegalArgumentException("La quantita' di XP da aggiungere non puo' essere negativa.");
        }
        xp += quantita;
    }

    /**
     * Potenzia di 1 punto la statistica indicata, spendendo
     * {@link #COSTO_XP_POTENZIAMENTO} punti esperienza.
     *
     * @param tipo la statistica da potenziare (non nulla)
     * @return {@code true} se l'XP era sufficiente e il potenziamento e' avvenuto,
     *         {@code false} altrimenti (statistica e XP restano invariati)
     */
    public boolean potenziaStatistica(StatType tipo) {
        Objects.requireNonNull(tipo, "Il tipo di statistica non puo' essere nullo.");
        if (xp < COSTO_XP_POTENZIAMENTO) {
            return false;
        }
        xp -= COSTO_XP_POTENZIAMENTO;
        statistiche.merge(tipo, 1, Integer::sum);
        return true;
    }

    // ----------------------------------------------------------------------
    // Accessori
    // ----------------------------------------------------------------------

    public String getNome() {
        return nome;
    }

    public CharacterClass getClasse() {
        return classe;
    }

    public int getEnergia() {
        return energia;
    }

    public int getXp() {
        return xp;
    }

    /**
     * @param tipo la statistica richiesta (non nulla)
     * @return il valore corrente della statistica
     */
    public int getStatistica(StatType tipo) {
        return statistiche.get(tipo);
    }

    /**
     * @return la mappa, in sola lettura, delle statistiche correnti del personaggio
     */
    public Map<StatType, Integer> getStatistiche() {
        return Collections.unmodifiableMap(statistiche);
    }
}
