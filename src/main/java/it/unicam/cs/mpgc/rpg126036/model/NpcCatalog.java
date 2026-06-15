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
     * Carisma &ge; 1, altrimenti resta sotto shock.
     *
     * @return l'NPC tecnico di laboratorio
     */
    public static Npc tecnicoLaboratorio() {
        return new Npc("tecnico_laboratorio", "Tecnico di laboratorio", true,
                Dialogue.condizionato(
                        new StatRequirement(StatType.CARISMA, 1),
                        TECNICO_BATTUTA_1,
                        "Che tragedia..."));
    }

    // Scambio con l'addetto alle pulizie nel cortile (capitolo 3). All'inizio
    // respinge tutti; con Carisma >= 2 il giocatore lo convince e l'addetto apre la
    // porta laterale dell'Aula B, rivelando anche dove è scappato lo studente.
    public static final String ADDETTO_RIFIUTO =
            "Levati da qui, non hai saputo cos'è successo? Non è posto per dei ragazzini "
                    + "che vogliono fare i guardoni.";
    public static final String ADDETTO_GIOCATORE =
            "Mi scusi... la prego, un secondo. So benissimo cosa è successo fuori. La vittima "
                    + "è Antonio, uno dei miei più cari amici. La polizia bloccherà tutto e temo "
                    + "che le prove andranno perse. Mi lasci solo dare un'occhiata veloce all'Aula B, "
                    + "per favore... lo devo ad Antonio.";
    public static final String ADDETTO_ACCETTA =
            "Accidenti... era un tuo amico? Che tragedia, quel povero ragazzo... Va bene, senti, "
                    + "facciamo in fretta. Ti apro la porta laterale. Tra l'altro, poco fa ho visto "
                    + "uscire di corsa uno studente sconvolto, piangeva... andava verso le scale che "
                    + "portano al Polo di Matematica.";

    /**
     * Capitolo 3: l'addetto alle pulizie, fermo nel cortile accanto al Polo B. Con
     * Carisma &ge; 2 si lascia convincere ad aprire la porta laterale dell'Aula B e
     * fornisce l'indizio sul percorso dello studente; altrimenti respinge il giocatore.
     * Lo scambio su misura (battute scorrevoli, XP, sblocco porta) è gestito dalla vista.
     *
     * @return l'NPC addetto alle pulizie
     */
    public static Npc addettoPulizie() {
        return new Npc("addetto_pulizie", "Addetto alle pulizie", true,
                Dialogue.condizionato(
                        new StatRequirement(StatType.CARISMA, 2),
                        ADDETTO_ACCETTA,
                        ADDETTO_RIFIUTO));
    }
}
