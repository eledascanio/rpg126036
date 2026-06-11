package it.unicam.cs.mpgc.rpg126036.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enigma della porta del laboratorio (livello 2): una combinazione di quattro
 * cifre che corrisponde all'anno di fondazione di Unicam ({@value #SOLUZIONE}).
 *
 * <p>Con una statistica pari almeno a {@value #SOGLIA_STAT} il giocatore ottiene
 * un indizio (Investigazione, Intuizione o Carisma). In assenza di indizi resta
 * la forza bruta, che apre comunque la porta consumando energia. In ogni caso la
 * risoluzione assegna {@value #XP} XP.</p>
 */
public class PortaLaboratorioPuzzle implements Puzzle {

    /** Combinazione corretta: l'anno di fondazione di Unicam. */
    public static final String SOLUZIONE = "1336";

    /** Soglia di statistica necessaria a ottenere un indizio. */
    public static final int SOGLIA_STAT = 2;

    /** Energia persa tentando combinazioni a caso (forza bruta). */
    public static final int COSTO_FORZA_BRUTA = 40;

    /** XP assegnati alla risoluzione, in qualunque modo avvenga. */
    public static final int XP = 40;

    private static final String INDIZIO_INVESTIGAZIONE =
            "Noti che i tasti 1, 3, 6 sono visibilmente più consumati e scoloriti rispetto agli altri. "
                    + "Qualcuno digita molto spesso questa combinazione. Sono quattro cifre... e i tasti sono "
                    + "1, 3 e 6. Quindi un numero si ripete. Che sia una data importante del Polo?";

    private static final String INDIZIO_INTUIZIONE =
            "Ti ricordi che l'amministratore del sistema è un docente fissato con la storia dell'Ateneo. "
                    + "Ha persino organizzato il finto compleanno dell'Università l'anno scorso. La password "
                    + "deve essere l'anno di fondazione di Unicam...";

    private static final String INDIZIO_CARISMA =
            "Aspetta, il mese scorso i ragazzi del sindacato si lamentavano che la password del laboratorio "
                    + "era ridicola e che la sapevano tutti perché era l'anno di fondazione dell'Unicam. "
                    + "Qual era? Ah, sì, il 1336!";

    private static final String TESTO_FORZA_BRUTA =
            "Non ho proprio idea di quale sia il codice… tentiamo la sorte?";

    private boolean risolto;

    @Override
    public String getId() {
        return "porta_laboratorio";
    }

    @Override
    public String getTesto() {
        return "La porta dell'aula è bloccata da una combinazione di quattro numeri.";
    }

    @Override
    public boolean isRisolto() {
        return risolto;
    }

    @Override
    public List<String> suggerimentiPer(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        List<String> indizi = new ArrayList<>();
        if (giocatore.getStatistica(StatType.INVESTIGAZIONE) >= SOGLIA_STAT) {
            indizi.add(INDIZIO_INVESTIGAZIONE);
        }
        if (giocatore.getStatistica(StatType.INTUIZIONE) >= SOGLIA_STAT) {
            indizi.add(INDIZIO_INTUIZIONE);
        }
        if (giocatore.getStatistica(StatType.CARISMA) >= SOGLIA_STAT) {
            indizi.add(INDIZIO_CARISMA);
        }
        return List.copyOf(indizi);
    }

    @Override
    public PuzzleOutcome tenta(Player giocatore, String tentativo) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "La porta è già aperta.");
        }
        if (SOLUZIONE.equals(tentativo == null ? null : tentativo.trim())) {
            risolto = true;
            giocatore.aggiungiXp(XP);
            return new PuzzleOutcome(true, 0, XP, "Combinazione corretta! La porta si apre.");
        }
        return new PuzzleOutcome(false, 0, 0, "Combinazione errata. La porta resta bloccata.");
    }

    @Override
    public PuzzleOutcome forzaBruta(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (risolto) {
            return new PuzzleOutcome(true, 0, 0, "La porta è già aperta.");
        }
        giocatore.riduciEnergia(COSTO_FORZA_BRUTA);
        giocatore.aggiungiXp(XP);
        risolto = true;
        return new PuzzleOutcome(true, COSTO_FORZA_BRUTA, XP,
                TESTO_FORZA_BRUTA + " Dopo svariati tentativi la porta finalmente cede.");
    }
}
