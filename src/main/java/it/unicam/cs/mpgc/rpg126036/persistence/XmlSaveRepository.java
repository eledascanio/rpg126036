package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementazione XML di {@link SaveRepository}. Serializza l'intero
 * {@link GameState} (giocatore, flag e capitolo corrente con le sue scene) in un
 * file {@code <nome>.xml} nella cartella dei salvataggi, di default sotto la home
 * dell'utente. Il salvataggio e' autosufficiente: il caricamento non dipende da
 * altre componenti del gioco.
 */
public class XmlSaveRepository implements SaveRepository {

    private static final String CARTELLA_DEFAULT = "rpg126036-saves";

    private final Path directory;

    /**
     * Crea il repository nella cartella di default sotto la home dell'utente.
     */
    public XmlSaveRepository() {
        this(Path.of(System.getProperty("user.home"), CARTELLA_DEFAULT));
    }

    /**
     * Crea il repository in una cartella specifica (utile per i test).
     *
     * @param directory la cartella dei salvataggi (non nulla)
     */
    public XmlSaveRepository(Path directory) {
        this.directory = Objects.requireNonNull(directory, "La cartella non puo' essere nulla.");
    }

    @Override
    public void save(GameState stato) {
        Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        String nome = stato.getPlayer().getNome();
        if (!exists(nome) && isFull()) {
            throw new PersistenceException("Slot pieni: eliminane uno prima di salvare una nuova partita.");
        }
        try {
            Files.createDirectories(directory);
            Document doc = creaDocumento();
            doc.appendChild(serializzaGameState(doc, stato));
            scriviSuFile(doc, percorsoSlot(nome));
        } catch (IOException e) {
            throw new PersistenceException("Impossibile salvare la partita di " + nome, e);
        }
    }

    @Override
    public List<SaveSlot> listSlots() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        List<SaveSlot> slot = new ArrayList<>();
        try (Stream<Path> file = Files.list(directory)) {
            for (Path p : (Iterable<Path>) file.filter(f -> f.toString().endsWith(".xml"))::iterator) {
                Element root = leggiRadice(p);
                Element player = primoElemento(root, "player");
                Element capitolo = primoElemento(root, "capitolo");
                slot.add(new SaveSlot(
                        player.getAttribute("nome"),
                        player.getAttribute("classe"),
                        capitolo != null ? capitolo.getAttribute("id") : "",
                        capitolo != null ? capitolo.getAttribute("scenaCorrente") : ""));
            }
        } catch (IOException e) {
            throw new PersistenceException("Impossibile elencare gli slot", e);
        }
        return slot;
    }

    @Override
    public Optional<GameState> load(String nomePersonaggio) {
        Path file = percorsoSlot(nomePersonaggio);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        Element root = leggiRadice(file);
        return Optional.of(deserializzaGameState(root));
    }

    @Override
    public boolean delete(String nomePersonaggio) {
        try {
            return Files.deleteIfExists(percorsoSlot(nomePersonaggio));
        } catch (IOException e) {
            throw new PersistenceException("Impossibile eliminare lo slot " + nomePersonaggio, e);
        }
    }

    @Override
    public boolean exists(String nomePersonaggio) {
        return Files.isRegularFile(percorsoSlot(nomePersonaggio));
    }

    // ----------------------------------------------------------------------
    // Serializzazione
    // ----------------------------------------------------------------------

    private Element serializzaGameState(Document doc, GameState stato) {
        Element root = doc.createElement("gameState");

        Player p = stato.getPlayer();
        Element player = doc.createElement("player");
        player.setAttribute("nome", p.getNome());
        player.setAttribute("classe", p.getClasse().name());
        player.setAttribute("energia", Integer.toString(p.getEnergia()));
        player.setAttribute("xp", Integer.toString(p.getXp()));
        Element statistiche = doc.createElement("statistiche");
        for (StatType tipo : StatType.values()) {
            Element stat = doc.createElement("stat");
            stat.setAttribute("tipo", tipo.name());
            stat.setAttribute("valore", Integer.toString(p.getStatistica(tipo)));
            statistiche.appendChild(stat);
        }
        player.appendChild(statistiche);
        Element inventario = doc.createElement("inventario");
        for (Item item : p.getInventario().getOggetti()) {
            Element el = doc.createElement("item");
            el.setAttribute("id", item.id());
            el.setAttribute("nome", item.nome());
            el.setAttribute("descrizione", item.descrizione());
            inventario.appendChild(el);
        }
        player.appendChild(inventario);
        root.appendChild(player);

        Element flag = doc.createElement("flag");
        for (String f : stato.getFlag()) {
            Element el = doc.createElement("f");
            el.setTextContent(f);
            flag.appendChild(el);
        }
        root.appendChild(flag);

        stato.getCapitoloCorrente().ifPresent(cap -> root.appendChild(serializzaCapitolo(doc, cap)));
        return root;
    }

    private Element serializzaCapitolo(Document doc, Chapter cap) {
        Element capitolo = doc.createElement("capitolo");
        capitolo.setAttribute("id", cap.getId());
        capitolo.setAttribute("titolo", cap.getTitolo());
        capitolo.setAttribute("scenaCorrente", cap.getScenaCorrente().getId());
        for (Scene scena : cap.getScene()) {
            Element el = doc.createElement("scena");
            el.setAttribute("id", scena.getId());
            el.setAttribute("titolo", scena.getTitolo());
            el.setAttribute("descrizione", scena.getDescrizione());
            el.setAttribute("completata", Boolean.toString(scena.isCompletata()));
            for (Transition t : scena.getTransizioni()) {
                Element tr = doc.createElement("transizione");
                tr.setAttribute("etichetta", t.etichetta());
                tr.setAttribute("destinazione", t.idDestinazione());
                el.appendChild(tr);
            }
            capitolo.appendChild(el);
        }
        return capitolo;
    }

    // ----------------------------------------------------------------------
    // Deserializzazione
    // ----------------------------------------------------------------------

    private GameState deserializzaGameState(Element root) {
        Element playerEl = primoElemento(root, "player");
        Player player = deserializzaPlayer(playerEl);

        GameState stato;
        Element capitoloEl = primoElemento(root, "capitolo");
        if (capitoloEl != null) {
            stato = new GameState(player, deserializzaCapitolo(capitoloEl));
        } else {
            stato = new GameState(player);
        }

        Element flagEl = primoElemento(root, "flag");
        if (flagEl != null) {
            NodeList flags = flagEl.getElementsByTagName("f");
            for (int i = 0; i < flags.getLength(); i++) {
                stato.setFlag(flags.item(i).getTextContent());
            }
        }
        return stato;
    }

    private Player deserializzaPlayer(Element playerEl) {
        String nome = playerEl.getAttribute("nome");
        CharacterClass classe = CharacterClass.valueOf(playerEl.getAttribute("classe"));
        Player player = new Player(nome, classe);

        // Energia: il Player parte al massimo, si riduce al valore salvato.
        int energia = Integer.parseInt(playerEl.getAttribute("energia"));
        int delta = Player.ENERGIA_MASSIMA - energia;
        if (delta > 0) {
            player.riduciEnergia(delta);
        }
        // XP: si parte da zero e si accumula il valore salvato.
        player.aggiungiXp(Integer.parseInt(playerEl.getAttribute("xp")));
        // Statistiche: si parte dai valori iniziali della classe e si aggiunge la differenza.
        Element statistiche = primoElemento(playerEl, "statistiche");
        if (statistiche != null) {
            NodeList stats = statistiche.getElementsByTagName("stat");
            for (int i = 0; i < stats.getLength(); i++) {
                Element stat = (Element) stats.item(i);
                StatType tipo = StatType.valueOf(stat.getAttribute("tipo"));
                int valore = Integer.parseInt(stat.getAttribute("valore"));
                int diff = valore - classe.statisticaIniziale(tipo);
                if (diff > 0) {
                    player.aumentaStatistica(tipo, diff);
                }
            }
        }
        // Inventario.
        Element inventario = primoElemento(playerEl, "inventario");
        if (inventario != null) {
            NodeList items = inventario.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Element it = (Element) items.item(i);
                player.raccogli(new Item(it.getAttribute("id"), it.getAttribute("nome"),
                        it.getAttribute("descrizione")));
            }
        }
        return player;
    }

    private Chapter deserializzaCapitolo(Element capitoloEl) {
        Chapter capitolo = new Chapter(capitoloEl.getAttribute("id"), capitoloEl.getAttribute("titolo"));
        NodeList scene = capitoloEl.getElementsByTagName("scena");
        List<Element> completate = new ArrayList<>();
        for (int i = 0; i < scene.getLength(); i++) {
            Element scenaEl = (Element) scene.item(i);
            Scene scena = new Scene(scenaEl.getAttribute("id"), scenaEl.getAttribute("titolo"),
                    scenaEl.getAttribute("descrizione"));
            NodeList transizioni = scenaEl.getElementsByTagName("transizione");
            for (int j = 0; j < transizioni.getLength(); j++) {
                Element tr = (Element) transizioni.item(j);
                scena.aggiungiTransizione(tr.getAttribute("etichetta"), tr.getAttribute("destinazione"));
            }
            capitolo.aggiungiScena(scena);
            if (Boolean.parseBoolean(scenaEl.getAttribute("completata"))) {
                completate.add(scenaEl);
            }
        }
        // Ripristino dei flag di completamento e della scena corrente.
        for (Element scenaEl : completate) {
            capitolo.getScena(scenaEl.getAttribute("id")).ifPresent(Scene::segnaCompletata);
        }
        capitolo.setScenaIniziale(capitoloEl.getAttribute("scenaCorrente"));
        return capitolo;
    }

    // ----------------------------------------------------------------------
    // Utilita' XML e filesystem
    // ----------------------------------------------------------------------

    private Path percorsoSlot(String nomePersonaggio) {
        Objects.requireNonNull(nomePersonaggio, "Il nome non puo' essere nullo.");
        String fileSicuro = nomePersonaggio.replaceAll("[^a-zA-Z0-9-_]", "_");
        return directory.resolve(fileSicuro + ".xml");
    }

    private Document creaDocumento() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.newDocument();
        } catch (Exception e) {
            throw new PersistenceException("Impossibile creare il documento XML", e);
        }
    }

    private void scriviSuFile(Document doc, Path file) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            try (var out = Files.newOutputStream(file)) {
                transformer.transform(new DOMSource(doc), new StreamResult(out));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new PersistenceException("Impossibile scrivere il file " + file, e);
        }
    }

    private Element leggiRadice(Path file) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try (var in = Files.newInputStream(file)) {
                Document doc = builder.parse(in);
                doc.getDocumentElement().normalize();
                return doc.getDocumentElement();
            }
        } catch (Exception e) {
            throw new PersistenceException("Impossibile leggere il file " + file, e);
        }
    }

    /**
     * @return il primo elemento figlio diretto con il nome indicato, o {@code null}
     */
    private Element primoElemento(Element padre, String nome) {
        NodeList figli = padre.getElementsByTagName(nome);
        for (int i = 0; i < figli.getLength(); i++) {
            if (figli.item(i).getParentNode() == padre) {
                return (Element) figli.item(i);
            }
        }
        return null;
    }
}
