package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Carica la definizione di un capitolo (scene, transizioni e riferimenti ai
 * contenuti) da un file XML, producendo un {@link ChapterDefinition}.
 *
 * <p>Formato atteso:</p>
 * <pre>
 * &lt;capitolo id="c1" titolo="..." scenaIniziale="ingresso"&gt;
 *   &lt;scena id="ingresso" titolo="..." descrizione="..."&gt;
 *     &lt;transizione etichetta="..." destinazione="corridoio"/&gt;
 *   &lt;/scena&gt;
 *   &lt;scena id="corridoio" titolo="..." descrizione="..."&gt;
 *     &lt;npc ref="studente_ubriaco"/&gt;
 *     &lt;oggetto ref="chiave_capitolo1"/&gt;
 *     &lt;enigma ref="..."/&gt;
 *   &lt;/scena&gt;
 * &lt;/capitolo&gt;
 * </pre>
 */
public class ChapterLoader {

    /**
     * Carica un capitolo da una risorsa sul classpath (es. "/capitoli/capitolo1.xml").
     *
     * @param percorso percorso della risorsa (non nullo)
     * @return la definizione del capitolo
     */
    public ChapterDefinition loadFromResource(String percorso) {
        Objects.requireNonNull(percorso, "Il percorso non puo' essere nullo.");
        try (InputStream in = getClass().getResourceAsStream(percorso)) {
            if (in == null) {
                throw new PersistenceException("Risorsa non trovata: " + percorso);
            }
            return load(in);
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException("Impossibile caricare la risorsa " + percorso, e);
        }
    }

    /**
     * Carica un capitolo da un file.
     *
     * @param file percorso del file (non nullo)
     * @return la definizione del capitolo
     */
    public ChapterDefinition load(Path file) {
        Objects.requireNonNull(file, "Il file non puo' essere nullo.");
        try (InputStream in = Files.newInputStream(file)) {
            return load(in);
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException("Impossibile caricare il file " + file, e);
        }
    }

    /**
     * Carica un capitolo da uno stream XML.
     *
     * @param in lo stream da cui leggere (non nullo)
     * @return la definizione del capitolo
     */
    public ChapterDefinition load(InputStream in) {
        Objects.requireNonNull(in, "Lo stream non puo' essere nullo.");
        Element root = leggiRadice(in);

        Chapter capitolo = new Chapter(root.getAttribute("id"), root.getAttribute("titolo"));
        Map<String, SceneContents> contenuti = new LinkedHashMap<>();

        NodeList scene = root.getElementsByTagName("scena");
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
            contenuti.put(scena.getId(), new SceneContents(
                    riferimenti(scenaEl, "npc"),
                    riferimenti(scenaEl, "oggetto"),
                    riferimenti(scenaEl, "enigma")));
        }

        // Scena iniziale esplicita, se indicata.
        String scenaIniziale = root.getAttribute("scenaIniziale");
        if (!scenaIniziale.isBlank()) {
            capitolo.setScenaIniziale(scenaIniziale);
        }
        return new ChapterDefinition(capitolo, contenuti);
    }

    /**
     * Raccoglie gli attributi {@code ref} degli elementi figli con il nome indicato.
     */
    private List<String> riferimenti(Element scenaEl, String nomeElemento) {
        List<String> ref = new ArrayList<>();
        NodeList nodi = scenaEl.getElementsByTagName(nomeElemento);
        for (int i = 0; i < nodi.getLength(); i++) {
            ref.add(((Element) nodi.item(i)).getAttribute("ref"));
        }
        return ref;
    }

    private Element leggiRadice(InputStream in) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in);
            doc.getDocumentElement().normalize();
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new PersistenceException("XML del capitolo non valido", e);
        }
    }
}
