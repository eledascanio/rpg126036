package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.CampaignLoader;

/**
 * Punto di ingresso dell'applicazione <i>Camerino Files</i>.
 *
 * <p>Carica la campagna dai capitoli XML come prova del corretto avvio del
 * bootstrap. L'interfaccia grafica JavaFX verra' agganciata qui: a partire dalla
 * {@link Campaign} caricata, la GUI creera' le sessioni tramite
 * {@link GameSessionFactory}.</p>
 */
public class Main {

    public static void main(String[] args) {
        Campaign campagna = new CampaignLoader().caricaStandard();
        System.out.println("Camerino Files - campagna caricata (" + campagna.size() + " capitoli):");
        for (Chapter capitolo : campagna.getCapitoli()) {
            System.out.println("  - " + capitolo.getTitolo());
        }
        System.out.println("Bootstrap completato. L'interfaccia grafica verra' avviata qui.");
    }
}
