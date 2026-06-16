package it.unicam.cs.mpgc.rpg126036.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Enigma del PC della vittima (livello 2): un terminale bloccato che puo' essere
 * superato per piu' vie, non mutuamente esclusive, a seconda delle statistiche
 * del giocatore. In ogni caso la risoluzione assegna {@value #XP} XP.
 *
 * <ul>
 *     <li><b>Investigazione &ge; {@value #SOGLIA_STAT}</b>: opzione "Analizza il
 *     terminale", un mini-gioco di conversione binario (6 cifre)&rarr;decimale.
 *     Entro {@value #MAX_TENTATIVI} tentativi sblocca senza costi; esaurendoli si
 *     sblocca comunque ma con una penalita' di {@value #PENALITA_FALLIMENTO} energia.</li>
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
    public static final int SOGLIA_STAT = 2;

    /** XP assegnati alla risoluzione, in qualunque modo avvenga. */
    public static final int XP = 30;

    /** Energia persa con la forza bruta. */
    public static final int COSTO_FORZA_BRUTA = 30;

    /** Energia persa fallendo il mini-gioco di Investigazione. */
    public static final int PENALITA_FALLIMENTO = 20;

    /** Tentativi disponibili nel mini-gioco binario. */
    public static final int MAX_TENTATIVI = 3;

    private static final String TESTO_INDIZIO_INTUIZIONE =
            "Esamini attentamente la postazione. Sotto la tastiera noti un minuscolo pezzo di carta "
                    + "strappato. C'è scritto qualcosa a matita: è la password personale della vittima, "
                    + "scritta per paura di dimenticarla!";

    private static final String TESTO_PASSWORD_ASSISTENTE = "Hai inserito la password ricevuta.";

    private final Random rng;

    private boolean haParlatoConAssistente;
    private boolean risolto;

    // Stato del mini-gioco di Investigazione.
    private int numeroDaConvertire = -1;
    private int tentativiRimasti;

    /**
     * Crea l'enigma con un generatore casuale di default.
     *
     * @param haParlatoConAssistente {@code true} se il giocatore ha gia' parlato con l'assistente
     */
    public PcVittimaPuzzle(boolean haParlatoConAssistente) {
        this(haParlatoConAssistente, new Random());
    }

    /**
     * Crea l'enigma con un generatore casuale specifico (utile per i test).
     *
     * @param haParlatoConAssistente {@code true} se il giocatore ha gia' parlato con l'assistente
     * @param rng                    generatore casuale per il mini-gioco binario (non nullo)
     */
    public PcVittimaPuzzle(boolean haParlatoConAssistente, Random rng) {
        this.haParlatoConAssistente = haParlatoConAssistente;
        this.rng = Objects.requireNonNull(rng, "Il generatore casuale non puo' essere nullo.");
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
     * Avvia il mini-gioco di Investigazione generando il numero da convertire.
     *
     * @param giocatore il giocatore (deve avere Investigazione &ge; {@value #SOGLIA_STAT})
     * @return il testo dell'enigma binario, oppure un messaggio se non disponibile
     */
    public String analizzaTerminale(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (giocatore.getStatistica(StatType.INVESTIGAZIONE) < SOGLIA_STAT) {
            return "Non hai l'occhio per analizzare il terminale.";
        }
        // Numero a 6 cifre binarie: decimale tra 32 (100000) e 63 (111111).
        numeroDaConvertire = 32 + rng.nextInt(32);
        tentativiRimasti = MAX_TENTATIVI;
        return "Converti in decimale il numero binario " + binario6(numeroDaConvertire)
                + ". Hai " + MAX_TENTATIVI + " tentativi.";
    }

    /**
     * Valuta un tentativo del mini-gioco binario avviato con {@link #analizzaTerminale(Player)}.
     */
    @Override
    public PuzzleOutcome tenta(Player giocatore, String tentativo) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "Il PC è già sbloccato.");
        }
        if (numeroDaConvertire < 0) {
            return new PuzzleOutcome(false, 0, 0, "Analizza prima il terminale.");
        }
        String atteso = String.valueOf(numeroDaConvertire);
        if (atteso.equals(tentativo == null ? null : tentativo.trim())) {
            risolto = true;
            giocatore.aggiungiXp(XP);
            return new PuzzleOutcome(true, 0, XP, "Conversione corretta! Il PC si sblocca.");
        }
        tentativiRimasti--;
        if (tentativiRimasti > 0) {
            return new PuzzleOutcome(false, 0, 0,
                    "Conversione errata. Tentativi rimasti: " + tentativiRimasti + ".");
        }
        // Tentativi esauriti: blocco di sicurezza, penalita' di energia, poi sblocco garantito.
        giocatore.riduciEnergia(PENALITA_FALLIMENTO);
        giocatore.aggiungiXp(XP);
        risolto = true;
        return new PuzzleOutcome(true, PENALITA_FALLIMENTO, XP,
                "Il sistema si blocca per sicurezza. Dopo lo stress dei tentativi falliti il terminale si riattiva e cede.");
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
     * @param giocatore il giocatore (deve avere Carisma &ge; {@value #SOGLIA_STAT} e aver parlato con l'assistente)
     * @return l'esito dell'inserimento
     */
    public PuzzleOutcome usaPasswordAssistente(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "Il PC è già sbloccato.");
        }
        if (giocatore.getStatistica(StatType.CARISMA) < SOGLIA_STAT || !haParlatoConAssistente) {
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

    /**
     * @param numero valore decimale da rappresentare
     * @return la rappresentazione binaria del numero su 6 cifre
     */
    private static String binario6(int numero) {
        String grezzo = Integer.toBinaryString(numero);
        return "000000".substring(grezzo.length()) + grezzo;
    }
}
