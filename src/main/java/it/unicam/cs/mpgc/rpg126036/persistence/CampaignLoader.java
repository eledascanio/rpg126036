package it.unicam.cs.mpgc.rpg126036.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Carica la {@link Campaign} del gioco dai file XML dei capitoli sul classpath,
 * delegando il parsing del singolo capitolo al {@link ChapterLoader}.
 *
 * <p>L'elenco delle risorse e' configurabile: estendere la campagna con nuovi
 * capitoli richiede solo aggiungere il relativo XML e il suo percorso, senza
 * toccare la logica di caricamento.</p>
 */
public class CampaignLoader {

    /** Percorsi, in ordine, dei capitoli della campagna standard. */
    public static final List<String> RISORSE_STANDARD = List.of(
            "/capitoli/capitolo1.xml",
            "/capitoli/capitolo2.xml",
            "/capitoli/capitolo3.xml");

    private final ChapterLoader chapterLoader;

    /**
     * Crea il loader con un {@link ChapterLoader} di default.
     */
    public CampaignLoader() {
        this(new ChapterLoader());
    }

    /**
     * Crea il loader con un {@link ChapterLoader} specifico (utile per i test).
     *
     * @param chapterLoader il loader dei singoli capitoli (non nullo)
     */
    public CampaignLoader(ChapterLoader chapterLoader) {
        this.chapterLoader = Objects.requireNonNull(chapterLoader, "Il chapter loader non puo' essere nullo.");
    }

    /**
     * Carica la campagna standard a tre capitoli.
     *
     * @return la campagna caricata
     */
    public Campaign caricaStandard() {
        return caricaDa(RISORSE_STANDARD);
    }

    /**
     * Carica una campagna dai percorsi di risorsa indicati, nell'ordine dato.
     *
     * @param risorse i percorsi classpath dei capitoli (non nulli, non vuoti)
     * @return la campagna caricata
     */
    public Campaign caricaDa(List<String> risorse) {
        Objects.requireNonNull(risorse, "L'elenco delle risorse non puo' essere nullo.");
        List<ChapterDefinition> definizioni = new ArrayList<>();
        for (String risorsa : risorse) {
            definizioni.add(chapterLoader.loadFromResource(risorsa));
        }
        return new Campaign(definizioni);
    }
}
