package it.unicam.cs.mpgc.rpg126036.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Predicate;

/**
 * Componente d'interfaccia riutilizzabile: un tastierino numerico con display,
 * cifre 0-9, tasto di cancellazione e conferma. E' una vista pura, senza stato di
 * gioco: comunica con il chiamante solo tramite il predicato di conferma.
 */
final class TastierinoNumerico {

    private TastierinoNumerico() {
    }

    /**
     * Crea un pannello con display e tastierino numerico (al più {@code maxCifre}
     * cifre). Premendo OK invoca {@code conferma} con le cifre digitate: se torna
     * {@code false} (codice errato) il display viene azzerato per ritentare.
     *
     * @param maxCifre numero massimo di cifre digitabili
     * @param conferma valutazione del codice inserito: {@code true} se accettato
     * @return il pannello pronto da inserire in un overlay
     */
    static VBox crea(int maxCifre, Predicate<String> conferma) {
        StringBuilder codice = new StringBuilder();
        Label display = new Label();
        display.getStyleClass().add("keypad-display");
        Runnable aggiornaDisplay = () -> {
            StringBuilder mostrato = new StringBuilder();
            for (int i = 0; i < maxCifre; i++) {
                mostrato.append(i < codice.length() ? codice.charAt(i) : '_');
                if (i < maxCifre - 1) {
                    mostrato.append(' ');
                }
            }
            display.setText(mostrato.toString());
        };
        aggiornaDisplay.run();

        GridPane griglia = new GridPane();
        griglia.setHgap(8);
        griglia.setVgap(8);
        griglia.setAlignment(Pos.CENTER);
        for (int n = 1; n <= 9; n++) {
            griglia.add(tastoCifra(String.valueOf(n), codice, aggiornaDisplay, maxCifre), (n - 1) % 3, (n - 1) / 3);
        }
        Button cancella = tastoSpeciale("←");
        cancella.setOnAction(e -> {
            if (codice.length() > 0) {
                codice.deleteCharAt(codice.length() - 1);
                aggiornaDisplay.run();
            }
        });
        Button ok = tastoSpeciale("OK");
        ok.setOnAction(e -> {
            if (!conferma.test(codice.toString())) {
                codice.setLength(0);
                aggiornaDisplay.run();
            }
        });
        griglia.add(cancella, 0, 3);
        griglia.add(tastoCifra("0", codice, aggiornaDisplay, maxCifre), 1, 3);
        griglia.add(ok, 2, 3);

        VBox box = new VBox(12, display, griglia);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /** Crea un tasto-cifra del tastierino: aggiunge la cifra al codice (max {@code maxCifre}). */
    private static Button tastoCifra(String cifra, StringBuilder codice, Runnable aggiornaDisplay, int maxCifre) {
        Button b = tastoSpeciale(cifra);
        b.setOnAction(e -> {
            if (codice.length() < maxCifre) {
                codice.append(cifra);
                aggiornaDisplay.run();
            }
        });
        return b;
    }

    /** Crea un tasto quadrato del tastierino con l'etichetta indicata. */
    private static Button tastoSpeciale(String etichetta) {
        Button b = new Button(etichetta);
        b.getStyleClass().addAll("game-button", "keypad-key");
        b.setMinSize(60, 56);
        return b;
    }
}
