package it.unicam.cs.mpgc.rpg126036.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enigma del PC della vittima (livello 2): un terminale bloccato che puo' essere
 * superato per piu' vie, non mutuamente esclusive, a seconda delle statistiche
 * del giocatore. In ogni caso la risoluzione assegna {@value #XP} XP.
 *
 * <p>Questa classe e' l'<b>unica fonte</b> delle regole numeriche dell'enigma
 * (binario da convertire, costi di energia, XP): la vista applica gli effetti
 * invocando i metodi di questo enigma, senza ridefinire le proprie costanti.</p>
 *
 * <ul>
 *     <li><b>Investigazione &ge; {@value #SOGLIA_STAT}</b>: opzione "Analizza il
 *     terminale", un mini-gioco di conversione del binario {@value #BINARIO}
 *     ({@value #SOLUZIONE} in decimale). Ogni tentativo errato costa
 *     {@value #COSTO_TENTATIVO_ERRATO} energia; resta sempre disponibile la forza bruta.</li>
 *     <li><b>Intuizione &ge; {@value #SOGLIA_STAT}</b>: opzione "Cerca indizi
 *     intorno alla postazione", trova il biglietto con la password.</li>
 *     <li><b>Carisma &ge; {@value #SOGLIA_STAT}</b> e dialogo con l'assistente gia'
 *     avvenuto: inserimento diretto della password ricevuta.</li>
 *     <li><b>Forza bruta</b>: sempre disponibile, sblocca a costo di
 *     {@value #COSTO_FORZA_BRUTA} energia.</li>
 * </ul>
 */
public class PcVittimaPuzzle implements Puzzle {

    /** Soglia di statistica necessaria ad abilitare i percorsi agevolati. */
    public static final int SOGLIA_STAT = 1;

    /** XP assegnati alla risoluzione, in qualunque modo avvenga. */
    public static final int XP = 30;

    /** Energia persa con la forza bruta. */
    public static final int COSTO_FORZA_BRUTA = 30;

    /** Energia persa a ogni tentativo errato del mini-gioco di Investigazione. */
    public static final int COSTO_TENTATIVO_ERRATO = 10;

    /** Numero binario presentato dal mini-gioco di Investigazione. */
    public static final String BINARIO = "11010";

    /** Soluzione (decimale) del mini-gioco di Investigazione: {@value #BINARIO} = 26. */
    public static final String SOLUZIONE = "26";

    private static final String TESTO_INDIZIO_INTUIZIONE =
            "Esamini attentamente la postazione. Sotto la tastiera noti un minuscolo pezzo di carta "
                    + "strappato. C'è scritto qualcosa a matita: è la password personale della vittima, "
                    + "scritta per paura di dimenticarla!";

    private static final String TESTO_PASSWORD_ASSISTENTE = "Hai inserito la password ricevuta.";

    private boolean haParlatoConAssistente;
    private boolean risolto;

    /**
     * Crea l'enigma.
     *
     * @param haParlatoConAssistente {@code true} se il giocatore ha gia' parlato con l'assistente
     */
    public PcVittimaPuzzle(boolean haParlatoConAssistente) {
        this.haParlatoConAssistente = haParlatoConAssistente;
    }

    /**
     * Registra che il giocatore ha (o non ha) parlato con l'assistente di laboratorio.
     *
     * @param haParlato {@code true} se il dialogo e' avvenuto
     */
    public void setHaParlatoConAssistente(boolean haParlato) {
        this.haParlatoConAssistente = haParlato;
    }

    @Override
    public String getId() {
        return "pc_vittima";
    }

    @Override
    public String getTesto() {
        return "Il computer della vittima è acceso, ma il terminale è bloccato da una password.";
    }

    @Override
    public boolean isRisolto() {
        return risolto;
    }

    /**
     * @param giocatore il giocatore che affronta l'enigma (non nullo)
     * @return le opzioni (pulsanti) disponibili in base a statistiche e contesto
     */
    public List<String> opzioniDisponibili(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        List<String> opzioni = new ArrayList<>();
        if (giocatore.getStatistica(StatType.INVESTIGAZIONE) >= SOGLIA_STAT) {
            opzioni.add("Analizza il terminale");
        }
        if (giocatore.getStatistica(StatType.INTUIZIONE) >= SOGLIA_STAT) {
            opzioni.add("Cerca indizi intorno alla postazione");
        }
        if (giocatore.getStatistica(StatType.CARISMA) >= SOGLIA_STAT && haParlatoConAssistente) {
            opzioni.add("Inserisci la password ricevuta dall'assistente");
        }
        opzioni.add("Forza Bruta");
        return List.copyOf(opzioni);
    }

    @Override
    public List<String> suggerimentiPer(Player giocatore) {
        return opzioniDisponibili(giocatore);
    }

    /**
     * @return il testo di presentazione del mini-gioco di Investigazione (conversione
     *         del binario {@value #BINARIO} in decimale)
     */
    public String testoEnigmaBinario() {
        return "Il terminale è protetto. Converti in decimale il numero binario "
                + BINARIO + " per ricavare la password.";
    }

    /**
     * Valuta un tentativo di conversione del mini-gioco di Investigazione: se
     * corretto sblocca il PC assegnando {@value #XP} XP, altrimenti applica una
     * penalita' di {@value #COSTO_TENTATIVO_ERRATO} energia e lascia ritentare.
     */
    @Override
    public PuzzleOutcome tenta(Player giocatore, String tentativo) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "Il PC è già sbloccato.");
        }
        if (SOLUZIONE.equals(tentativo == null ? null : tentativo.trim())) {
            risolto = true;
            giocatore.aggiungiXp(XP);
            return new PuzzleOutcome(true, 0, XP, "Conversione corretta! Il PC si sblocca.");
        }
        giocatore.riduciEnergia(COSTO_TENTATIVO_ERRATO);
        return new PuzzleOutcome(false, COSTO_TENTATIVO_ERRATO, 0,
                "Conversione errata. Riprova. (-" + COSTO_TENTATIVO_ERRATO + " energia)");
    }

    /**
     * Percorso Intuizione: cerca il biglietto con la password sotto la tastiera.
     *
     * @param giocatore il giocatore (deve avere Intuizione &ge; {@value #SOGLIA_STAT})
     * @return l'esito della ricerca
     */
    public PuzzleOutcome cercaIndizi(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "Il PC è già sbloccato.");
        }
        if (giocatore.getStatistica(StatType.INTUIZIONE) < SOGLIA_STAT) {
            return new PuzzleOutcome(false, 0, 0, "Non noti nulla di utile intorno alla postazione.");
        }
        risolto = true;
        giocatore.aggiungiXp(XP);
        return new PuzzleOutcome(true, 0, XP, TESTO_INDIZIO_INTUIZIONE);
    }

    /**
     * Percorso Carisma: inserisce la password ottenuta parlando con l'assistente.
     *
     * @param giocatore il giocatore (deve aver parlato con l'assistente)
     * @return l'esito dell'inserimento
     */
    public PuzzleOutcome usaPasswordAssistente(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "Il PC è già sbloccato.");
        }
        if (!haParlatoConAssistente) {
            return new PuzzleOutcome(false, 0, 0, "Non hai (ancora) ottenuto la password dall'assistente.");
        }
        risolto = true;
        giocatore.aggiungiXp(XP);
        return new PuzzleOutcome(true, 0, XP, TESTO_PASSWORD_ASSISTENTE);
    }

    @Override
    public PuzzleOutcome forzaBruta(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "Il PC è già sbloccato.");
        }
        giocatore.riduciEnergia(COSTO_FORZA_BRUTA);
        giocatore.aggiungiXp(XP);
        risolto = true;
        return new PuzzleOutcome(true, COSTO_FORZA_BRUTA, XP,
                "Tenti password a caso fino allo sfinimento, ma alla fine il terminale cede.");
    }
}
