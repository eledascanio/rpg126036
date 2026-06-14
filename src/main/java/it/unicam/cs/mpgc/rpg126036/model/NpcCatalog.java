package it.unicam.cs.mpgc.rpg126036.model;

import java.util.List;

/**
 * Catalogo degli NPC del gioco con i relativi dati di dialogo, organizzati per
 * livello/capitolo. Raccoglie in un unico punto i testi narrativi.
 *
 * <p>NOTA: alcune battute degli NPG di contorno (panico, esame fallito) sono
 * testi provvisori personalizzabili; i testi degli NPC chiave seguono fedelmente
 * la traccia.</p>
 */
public final class NpcCatalog {

    private NpcCatalog() {
        // Classe di sole utilita' statiche: non istanziabile.
    }

    /**
     * Capitolo 1: lo studente ubriaco (informatore utile) e tre NPC di contorno.
     *
     * @return gli NPC presenti nel primo capitolo
     */
    public static List<Npc> capitoloUno() {
        return List.of(
                // NPC utile: l'interazione (riconoscimento col Carisma, scelta "chiedi
                // dettagli" con XP e indizio) e' gestita su misura dalla vista; qui basta
                // la battuta comune e il nome mostrato nel dialog box.
                new Npc("studente_ubriaco", "Studente ubriaco", true,
                        Dialogue.semplice("Ho visto Antonio discutere con qualcuno prima... mi pare.")),
                // NPC di contorno: in preda al panico.
                new Npc("studente_panico", "Studentessa in lacrime", false,
                        Dialogue.semplice("Non riesco a smettere di piangere... è tutto un incubo!")),
                // Due NPC di contorno che commentano un esame fallito.
                new Npc("studente_esame_1", "Studente sconsolato", false,
                        Dialogue.semplice("Hai saputo? Ho fallito di nuovo quell'esame...")),
                new Npc("studente_esame_2", "Studentessa comprensiva", false,
                        Dialogue.semplice("Dai, ci riproverai alla prossima sessione."))
        );
    }

    // Scambio a tre battute (tecnico -> giocatore -> tecnico) sbloccato dal Carisma,
    // mostrato dalla vista come sequenza di dialoghi scorrevoli con E.
    public static final String TECNICO_BATTUTA_1 =
            "Che tragedia... Antonio... non posso crederci. Perché è successo proprio a lui? "
                    + "La polizia mi farà un sacco di domande, sono l'ultimo che ha gestito il laboratorio "
                    + "oggi... non voglio finire nei guai!";
    // La battuta del giocatore è divisa in due schermate per stare nel dialog box.
    public static final String TECNICO_BATTUTA_GIOCATORE_1 =
            "Ehi, calmati. Mi serve solo la password del PC di Antonio per dare un'occhiata. "
                    + "Sono il rappresentante degli studenti, mi conoscono tutti qui. La polizia ci metterà "
                    + "dei giorni a decifrare quel PC e l'assassino nel frattempo scapperà da Camerino.";
    public static final String TECNICO_BATTUTA_GIOCATORE_2 =
            "Se troviamo una pista adesso, ripuliamo anche il tuo nome prima che ti interroghino. "
                    + "Fidati di me, copro io le tue spalle con i docenti.";
    public static final String TECNICO_BATTUTA_3 =
            "D'accordo, mi fido di te. In fondo sei il rappresentante, sai come gestire "
                    + "queste cose con l'amministrazione.";

    /**
     * Livello 2: il tecnico di laboratorio. Fornisce la password del PC solo con
     * Carisma &ge; 2, altrimenti resta sotto shock.
     *
     * @return l'NPC tecnico di laboratorio
     */
    public static Npc tecnicoLaboratorio() {
        return new Npc("tecnico_laboratorio", "Tecnico di laboratorio", true,
                Dialogue.condizionato(
                        new StatRequirement(StatType.CARISMA, 2),
                        TECNICO_BATTUTA_1,
                        "Che tragedia..."));
    }

    /**
     * Livello 3: l'addetto alle pulizie. Aiuta a entrare e fornisce l'indizio sul
     * percorso dello studente solo con Carisma &ge; 3, altrimenti respinge il giocatore.
     *
     * @return l'NPC addetto alle pulizie
     */
    public static Npc addettoPulizie() {
        return new Npc("addetto_pulizie", "Addetto alle pulizie", true,
                Dialogue.condizionato(
                        new StatRequirement(StatType.CARISMA, 3),
                        "Poco fa ho visto uscire di corsa uno studente sconvolto, piangeva... "
                                + "andava verso le scale che portano al Polo di Matematica",
                        "Levati da qui, non è posto per dei ragazzini"));
    }
}
