package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
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
 * {@link GameState} (giocatore con inventario e diario degli indizi, flag e
 * capitolo corrente con le sue scene) in un file {@code <nome>.xml} nella cartella
 * dei salvataggi, di default la cartella {@code saves/} del progetto (esclusa da
 * git). Il salvataggio e' autosufficiente: il caricamento non dipende da altre
 * componenti del gioco.
 *
 * <p>La serializzazione e la deserializzazione delle collezioni del giocatore
 * (inventario, diario) sono isolate in metodi privati dedicati e simmetrici, in
 * modo che ogni struttura dati abbia un unico punto di mappatura verso l'XML.</p>
 */
public class XmlSaveRepository implements SaveRepository {

    private static final String CARTELLA_DEFAULT = "saves";

    private final Path directory;

    /**
     * Crea il repository nella cartella {@code saves/} del progetto (relativa alla
     * directory di lavoro). I file di salvataggio sono esclusi dal versionamento.
     */
    public XmlSaveRepository() {
        this(Path.of(CARTELLA_DEFAULT));
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

        Player giocatore = stato.getPlayer();
        Element player = doc.createElement("player");
        player.setAttribute("nome", giocatore.getNome());
        player.setAttribute("classe", giocatore.getClasse().name());
        player.setAttribute("energia", Integer.toString(giocatore.getEnergia()));
        player.setAttribute("xp", Integer.toString(giocatore.getXp()));
        Element statistiche = doc.createElement("statistiche");
        for (StatType tipo : StatType.values()) {
            Element stat = doc.createElement("stat");
            stat.setAttribute("tipo", tipo.name());
            stat.setAttribute("valore", Integer.toString(giocatore.getStatistica(tipo)));
            statistiche.appendChild(stat);
        }
        player.appendChild(statistiche);
        player.appendChild(serializzaInventario(doc, giocatore));
        player.appendChild(serializzaDiario(doc, giocatore));
        root.appendChild(player);

        Element flag = doc.createElement("flag");
        for (String nomeFlag : stato.getFlag()) {
            Element elementoFlag = doc.createElement("f");
            elementoFlag.setTextContent(nomeFlag);
            flag.appendChild(elementoFlag);
        }
        root.appendChild(flag);

        stato.getCapitoloCorrente().ifPresent(capitolo -> root.appendChild(serializzaCapitolo(doc, capitolo)));
        return root;
    }

    private Element serializzaCapitolo(Document doc, Chapter capitolo) {
        Element elementoCapitolo = doc.createElement("capitolo");
        elementoCapitolo.setAttribute("id", capitolo.getId());
        elementoCapitolo.setAttribute("titolo", capitolo.getTitolo());
        elementoCapitolo.setAttribute("scenaCorrente", capitolo.getScenaCorrente().getId());
        for (Scene scena : capitolo.getScene()) {
            Element elementoScena = doc.createElement("scena");
            elementoScena.setAttribute("id", scena.getId());
            elementoScena.setAttribute("titolo", scena.getTitolo());
            elementoScena.setAttribute("descrizione", scena.getDescrizione());
            elementoScena.setAttribute("completata", Boolean.toString(scena.isCompletata()));
            for (Transition transizione : scena.getTransizioni()) {
                Element elementoTransizione = doc.createElement("transizione");
                elementoTransizione.setAttribute("etichetta", transizione.etichetta());
                elementoTransizione.setAttribute("destinazione", transizione.idDestinazione());
                elementoScena.appendChild(elementoTransizione);
            }
            elementoCapitolo.appendChild(elementoScena);
        }
        return elementoCapitolo;
    }

    /**
     * Serializza l'inventario del giocatore in un elemento {@code <inventario>}
     * con un {@code <item>} (id, nome, descrizione) per ogni oggetto.
     */
    private Element serializzaInventario(Document doc, Player giocatore) {
        Element inventario = doc.createElement("inventario");
        for (Item item : giocatore.getInventario().getOggetti()) {
            Element elementoItem = doc.createElement("item");
            elementoItem.setAttribute("id", item.id());
            elementoItem.setAttribute("nome", item.nome());
            elementoItem.setAttribute("descrizione", item.descrizione());
            inventario.appendChild(elementoItem);
        }
        return inventario;
    }

    /**
     * Serializza il diario degli indizi in un elemento {@code <diario>} con un
     * {@code <indizio>} (id, titolo, testo) per ogni indizio scoperto.
     */
    private Element serializzaDiario(Document doc, Player giocatore) {
        Element diario = doc.createElement("diario");
        for (Clue indizio : giocatore.getDiario().getIndizi()) {
            Element elementoIndizio = doc.createElement("indizio");
            elementoIndizio.setAttribute("id", indizio.id());
            elementoIndizio.setAttribute("titolo", indizio.titolo());
            elementoIndizio.setAttribute("testo", indizio.testo());
            diario.appendChild(elementoIndizio);
        }
        return diario;
    }

    // ----------------------------------------------------------------------
    // Deserializzazione
    // ----------------------------------------------------------------------

    private GameState deserializzaGameState(Element root) {
        Element elementoPlayer = primoElemento(root, "player");
        Player giocatore = deserializzaPlayer(elementoPlayer);

        GameState stato;
        Element elementoCapitolo = primoElemento(root, "capitolo");
        if (elementoCapitolo != null) {
            stato = new GameState(giocatore, deserializzaCapitolo(elementoCapitolo));
        } else {
            stato = new GameState(giocatore);
        }

        Element elementoFlag = primoElemento(root, "flag");
        if (elementoFlag != null) {
            NodeList flagSalvati = elementoFlag.getElementsByTagName("f");
            for (int i = 0; i < flagSalvati.getLength(); i++) {
                stato.setFlag(flagSalvati.item(i).getTextContent());
            }
        }
        return stato;
    }

    private Player deserializzaPlayer(Element elementoPlayer) {
        String nome = elementoPlayer.getAttribute("nome");
        CharacterClass classe = CharacterClass.valueOf(elementoPlayer.getAttribute("classe"));
        Player giocatore = new Player(nome, classe);

        // Energia: il Player parte al massimo, si riduce al valore salvato.
        int energia = Integer.parseInt(elementoPlayer.getAttribute("energia"));
        int delta = Player.ENERGIA_MASSIMA - energia;
        if (delta > 0) {
            giocatore.riduciEnergia(delta);
        }
        // XP: si parte da zero e si accumula il valore salvato.
        giocatore.aggiungiXp(Integer.parseInt(elementoPlayer.getAttribute("xp")));
        // Statistiche: si parte dai valori iniziali della classe e si aggiunge la differenza.
        Element statistiche = primoElemento(elementoPlayer, "statistiche");
        if (statistiche != null) {
            NodeList statSalvate = statistiche.getElementsByTagName("stat");
            for (int i = 0; i < statSalvate.getLength(); i++) {
                Element elementoStat = (Element) statSalvate.item(i);
                StatType tipo = StatType.valueOf(elementoStat.getAttribute("tipo"));
                int valore = Integer.parseInt(elementoStat.getAttribute("valore"));
                int differenza = valore - classe.statisticaIniziale(tipo);
                if (differenza > 0) {
                    giocatore.aumentaStatistica(tipo, differenza);
                }
            }
        }
        deserializzaInventario(elementoPlayer, giocatore);
        deserializzaDiario(elementoPlayer, giocatore);
        return giocatore;
    }

    /**
     * Ripopola l'inventario del giocatore dagli elementi {@code <item>} contenuti
     * nel {@code <inventario>}, se presente.
     */
    private void deserializzaInventario(Element elementoPlayer, Player giocatore) {
        Element inventario = primoElemento(elementoPlayer, "inventario");
        if (inventario == null) {
            return;
        }
        NodeList itemSalvati = inventario.getElementsByTagName("item");
        for (int i = 0; i < itemSalvati.getLength(); i++) {
            Element elementoItem = (Element) itemSalvati.item(i);
            giocatore.raccogli(new Item(elementoItem.getAttribute("id"), elementoItem.getAttribute("nome"),
                    elementoItem.getAttribute("descrizione")));
        }
    }

    /**
     * Ripopola il diario del giocatore dagli elementi {@code <indizio>} contenuti
     * nel {@code <diario>}, se presente.
     */
    private void deserializzaDiario(Element elementoPlayer, Player giocatore) {
        Element diario = primoElemento(elementoPlayer, "diario");
        if (diario == null) {
            return;
        }
        NodeList indiziSalvati = diario.getElementsByTagName("indizio");
        for (int i = 0; i < indiziSalvati.getLength(); i++) {
            Element elementoIndizio = (Element) indiziSalvati.item(i);
            giocatore.scopriIndizio(new Clue(elementoIndizio.getAttribute("id"),
                    elementoIndizio.getAttribute("titolo"), elementoIndizio.getAttribute("testo")));
        }
    }

    private Chapter deserializzaCapitolo(Element elementoCapitolo) {
        Chapter capitolo = new Chapter(elementoCapitolo.getAttribute("id"),
                elementoCapitolo.getAttribute("titolo"));
        NodeList scene = elementoCapitolo.getElementsByTagName("scena");
        List<Element> sceneCompletate = new ArrayList<>();
        for (int i = 0; i < scene.getLength(); i++) {
            Element elementoScena = (Element) scene.item(i);
            Scene scena = new Scene(elementoScena.getAttribute("id"), elementoScena.getAttribute("titolo"),
                    elementoScena.getAttribute("descrizione"));
            NodeList transizioni = elementoScena.getElementsByTagName("transizione");
            for (int j = 0; j < transizioni.getLength(); j++) {
                Element elementoTransizione = (Element) transizioni.item(j);
                scena.aggiungiTransizione(elementoTransizione.getAttribute("etichetta"),
                        elementoTransizione.getAttribute("destinazione"));
            }
            capitolo.aggiungiScena(scena);
            if (Boolean.parseBoolean(elementoScena.getAttribute("completata"))) {
                sceneCompletate.add(elementoScena);
            }
        }
        // Ripristino dei flag di completamento e della scena corrente.
        for (Element elementoScena : sceneCompletate) {
            capitolo.getScena(elementoScena.getAttribute("id")).ifPresent(Scene::segnaCompletata);
        }
        capitolo.setScenaIniziale(elementoCapitolo.getAttribute("scenaCorrente"));
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
