package it.unicam.cs.mpgc.rpg126036.model;

import java.util.List;

/**
 * Catalogo degli indizi del gioco, organizzati per capitolo. Raccoglie in un
 * unico punto i testi delle informazioni che il giocatore puo' scoprire durante
 * l'indagine, distinte dagli oggetti fisici di {@link ItemCatalog}.
 */
public final class ClueCatalog {

    // --- Identificativi degli indizi (riferibili da dialoghi, enigmi e scene) ---
    public static final String ID_ALEX_KAUR = "indizio_alex_kaur";
    public static final String ID_PASSWORD_PC = "indizio_password_pc";
    public static final String ID_EMAIL_MITTENTE = "indizio_email_mittente";
    public static final String ID_FUGA_STUDENTE = "indizio_fuga_studente";
    public static final String ID_OROLOGIO = "indizio_orologio";
    public static final String ID_TESSUTO = "indizio_tessuto";

    private ClueCatalog() {
        // Classe di sole utilita' statiche: non istanziabile.
    }

    // ----------------------------------------------------------------------
    // Capitolo 1
    // ----------------------------------------------------------------------

    /**
     * Indizio ottenuto dallo studente ubriaco scegliendo "chiedi dettagli": il
     * nome di chi stava litigando con Antonio.
     *
     * @return l'indizio sul nome del litigante
     */
    public static Clue alexKaur() {
        return new Clue(ID_ALEX_KAUR, "Il nome del litigante",
                "Antonio stava discutendo animatamente con un certo Alex Kaur.");
    }

    // ----------------------------------------------------------------------
    // Capitolo 2
    // ----------------------------------------------------------------------

    /**
     * Indizio ottenuto dall'assistente di laboratorio (Carisma &gt; 2): la
     * password del PC della vittima, che verra' poi inserita automaticamente.
     *
     * @return l'indizio sulla password del PC
     */
    public static Clue passwordPc() {
        return new Clue(ID_PASSWORD_PC, "La password del PC",
                "L'assistente di laboratorio ti ha confidato la password del PC di Antonio.");
    }

    /**
     * Indizio trovato sul PC sbloccato: la mail minatoria e il suo mittente, con
     * iniziali che non corrispondono ad Alex Kaur.
     *
     * @return l'indizio sul mittente della minaccia
     */
    public static Clue emailMittente() {
        return new Clue(ID_EMAIL_MITTENTE, "Il mittente della minaccia",
                "Una mail minatoria inviata ad Antonio fissava un incontro in Aula B alle 00:30. "
                        + "Il mittente era em.informatica@gmail.com: due iniziali (E. M.) seguite da "
                        + "\"Informatica\". Non corrispondono ad Alex Kaur.");
    }

    // ----------------------------------------------------------------------
    // Capitolo 3
    // ----------------------------------------------------------------------

    /**
     * Indizio della via del Carisma (+3): l'addetto alle pulizie ha visto fuggire
     * uno studente sconvolto verso il Polo di Matematica.
     *
     * @return l'indizio sulla fuga dello studente
     */
    public static Clue fugaStudente() {
        return new Clue(ID_FUGA_STUDENTE, "La fuga dello studente",
                "L'addetto alle pulizie ha visto uscire di corsa uno studente sconvolto e in lacrime, "
                        + "diretto verso le scale che portano al Polo di Matematica.");
    }

    /**
     * Indizio della via dell'Investigazione (+3): un orologio rotto inciso con
     * due lettere, le stesse iniziali della mail.
     *
     * @return l'indizio sull'orologio inciso
     */
    public static Clue orologio() {
        return new Clue(ID_OROLOGIO, "L'orologio inciso",
                "Un orologio da polso rotto, con il cinturino strappato, inciso sul retro con due "
                        + "lettere: le stesse iniziali (E. M.) del mittente della mail.");
    }

    /**
     * Indizio della via dell'Intuizione (+3): un pezzo di tessuto strappato
     * impigliato nella porta, forse della maglia dell'assassino in fuga.
     *
     * @return l'indizio sul tessuto strappato
     */
    public static Clue tessuto() {
        return new Clue(ID_TESSUTO, "Il tessuto strappato",
                "Un pezzo di tessuto strappato impigliato nella porta: forse appartiene alla maglia "
                        + "dell'assassino, fuggito proprio da qui.");
    }

    // ----------------------------------------------------------------------
    // Raggruppamenti per capitolo
    // ----------------------------------------------------------------------

    /**
     * @return gli indizi ottenibili nel capitolo 1
     */
    public static List<Clue> capitoloUno() {
        return List.of(alexKaur());
    }

    /**
     * @return gli indizi ottenibili nel capitolo 2
     */
    public static List<Clue> capitoloDue() {
        return List.of(passwordPc(), emailMittente());
    }

    /**
     * @return gli indizi ottenibili nel capitolo 3 (uno per ciascuna via)
     */
    public static List<Clue> capitoloTre() {
        return List.of(fugaStudente(), orologio(), tessuto());
    }
}
