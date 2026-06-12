package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.NpcCatalog;
import it.unicam.cs.mpgc.rpg126036.model.PcVittimaPuzzle;
import it.unicam.cs.mpgc.rpg126036.model.PortaLaboratorioPuzzle;
import it.unicam.cs.mpgc.rpg126036.model.Puzzle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Risolve gli identificativi dei contenuti di una scena
 * ({@link it.unicam.cs.mpgc.rpg126036.persistence.SceneContents}) nelle istanze
 * concrete del model: NPC, oggetti ed enigmi. Colma il ponte tra la definizione
 * dichiarativa della campagna (id nell'XML) e gli oggetti di gioco prodotti dai
 * cataloghi, che la vista di esplorazione posiziona sulla mappa.
 *
 * <p>Definisce inoltre l'associazione, non presente nel model, tra un NPC
 * <i>utile</i> e l'{@link Clue indizio} che ne sblocca il dialogo: parlare con
 * l'NPC, quando il suo dialogo e' accessibile, registra l'indizio nel diario.</p>
 */
public class ContentResolver {

    /** Flag di progresso che segnala il dialogo avvenuto con l'assistente di laboratorio. */
    public static final String FLAG_ASSISTENTE = "ha_parlato_con_assistente";

    private final Map<String, Npc> npcPerId = new LinkedHashMap<>();
    private final Map<String, Item> itemPerId = new LinkedHashMap<>();
    private final Map<String, Clue> indiziPerId = new LinkedHashMap<>();
    private final Map<String, String> indizioPerNpc = new LinkedHashMap<>();

    /**
     * Costruisce il risolutore indicizzando per id i contenuti dei cataloghi.
     */
    public ContentResolver() {
        // NPC: gli NPC di contorno e gli NPC chiave di tutti i capitoli, per id.
        Stream.concat(NpcCatalog.capitoloUno().stream(),
                        Stream.of(NpcCatalog.tecnicoLaboratorio(), NpcCatalog.addettoPulizie()))
                .forEach(npc -> npcPerId.put(npc.getId(), npc));

        // Oggetti raccoglibili, per id.
        Item chiave = ItemCatalog.chiaveCapitoloUno();
        itemPerId.put(chiave.id(), chiave);

        // Indizi di tutti i capitoli, per id.
        Stream.of(ClueCatalog.capitoloUno(), ClueCatalog.capitoloDue(), ClueCatalog.capitoloTre())
                .flatMap(List::stream)
                .forEach(indizio -> indiziPerId.put(indizio.id(), indizio));

        // Legame NPC utile -> indizio sbloccato quando il dialogo e' accessibile.
        indizioPerNpc.put("informatore", ClueCatalog.ID_ALEX_KAUR);
        indizioPerNpc.put("tecnico_laboratorio", ClueCatalog.ID_PASSWORD_PC);
        indizioPerNpc.put("addetto_pulizie", ClueCatalog.ID_FUGA_STUDENTE);
    }

    /**
     * @param id identificativo dell'NPC
     * @return l'NPC corrispondente, se esiste
     */
    public Optional<Npc> npc(String id) {
        return Optional.ofNullable(npcPerId.get(id));
    }

    /**
     * @param id identificativo dell'oggetto
     * @return l'oggetto corrispondente, se esiste
     */
    public Optional<Item> item(String id) {
        return Optional.ofNullable(itemPerId.get(id));
    }

    /**
     * Crea una nuova istanza dell'enigma indicato. Gli enigmi sono stateful
     * (memorizzano se sono risolti e l'eventuale stato interno): chi assembla la
     * scena ne crea una sola istanza e la conserva finche' resta nella scena.
     *
     * @param id    identificativo dell'enigma
     * @param stato lo stato di gioco, usato per condizionare l'enigma (ad es. il
     *              dialogo con l'assistente per il PC della vittima)
     * @return una nuova istanza dell'enigma, se l'id e' noto
     */
    public Optional<Puzzle> creaEnigma(String id, GameState stato) {
        Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        return switch (id) {
            case "porta_laboratorio" -> Optional.of(new PortaLaboratorioPuzzle());
            case "pc_vittima" -> Optional.of(new PcVittimaPuzzle(stato.hasFlag(FLAG_ASSISTENTE)));
            default -> Optional.empty();
        };
    }

    /**
     * Restituisce l'indizio che l'NPC indicato sblocca, se ne sblocca uno.
     *
     * @param idNpc identificativo dell'NPC
     * @return l'indizio associato, se l'NPC e' una fonte di indizi
     */
    public Optional<Clue> indizioDi(String idNpc) {
        return Optional.ofNullable(indizioPerNpc.get(idNpc)).map(indiziPerId::get);
    }
}
