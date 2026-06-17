package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.achievement.AchievementManager;
import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.PcVittimaPuzzle;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.PuzzleOutcome;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Objects;

/**
 * Trama dell'enigma del PC della vittima (capitolo 2). Nel laboratorio ci sono sei
 * computer indistinguibili: solo l'ultimo è quello di Antonio. Esaminato il PC
 * giusto, lo sblocco segue la via più adatta al giocatore — password ottenuta dal
 * tecnico, enigma binario (Investigazione), ricerca del foglietto (Intuizione) o
 * forza bruta — e apre la mail minatoria, che chiude il capitolo. Gli altri cinque
 * danno "PC sbagliato" e una penalità di energia.
 *
 * <p>Le regole numeriche (binario, soluzione, costi, XP) vivono nel
 * {@link PcVittimaPuzzle}: questa classe vi applica gli effetti senza duplicarli.
 * Estratta dalla {@link ExplorationView} per isolare la trama, si appoggia alla
 * schermata (collaboratore) per overlay, dialoghi, HUD, traguardi e avanzamento.</p>
 */
final class PcVittima {

    private final RegiaEsplorazione view;
    private final GameState stato;
    private final GameEngine engine;
    private final AchievementManager achievementManager;
    private final double larghezzaMappa;
    private final double altezzaMappa;

    // Regole dell'enigma del PC (binario, costi, XP) e stato di risoluzione: la trama
    // ne applica gli effetti invece di duplicarli. Impostato alla creazione dei PC.
    private PcVittimaPuzzle pcPuzzle;
    // Diventa true non appena si esamina un PC sbagliato: serve a sbloccare il traguardo
    // "Cercatore d'oro" solo se il PC giusto è il primissimo a essere cliccato.
    private boolean pcSbagliatoToccato;

    /**
     * @param view               la schermata di esplorazione che offre i servizi di scena
     * @param stato              lo stato di gioco (giocatore, flag)
     * @param engine             il motore di gioco (indizi, game over, avanzamento)
     * @param achievementManager il gestore dei traguardi ("Cercatore d'oro")
     * @param larghezzaMappa     larghezza della mappa, per posizionare i PC
     * @param altezzaMappa       altezza della mappa, per posizionare i PC
     */
    PcVittima(RegiaEsplorazione view, GameState stato, GameEngine engine,
              AchievementManager achievementManager, double larghezzaMappa, double altezzaMappa) {
        this.view = Objects.requireNonNull(view, "La schermata non puo' essere nulla.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.engine = Objects.requireNonNull(engine, "Il motore non puo' essere nullo.");
        this.achievementManager =
                Objects.requireNonNull(achievementManager, "Il gestore dei traguardi non puo' essere nullo.");
        this.larghezzaMappa = larghezzaMappa;
        this.altezzaMappa = altezzaMappa;
    }

    /**
     * @return {@code true} se nell'aula della mail (aula LA1 del capitolo 2) le uscite
     *         non vanno rese come elementi raggiungibili: si avanza aprendo la mail sul
     *         PC della vittima, che la presenta come overlay
     */
    boolean nascondeUscite() {
        return "aula_la1".equals(engine.getScenaCorrente().getId())
                && "capitolo2".equals(engine.getCapitoloCorrente().getId());
    }

    /**
     * Dispone i sei PC del laboratorio (due per banco) come punti interattivi
     * invisibili e indistinguibili. Solo l'ultimo a destra, verso il muro, è il PC
     * della vittima: interagendovi la password si inserisce da sé e si apre la mail.
     * Sui restanti cinque si ottiene "PC sbagliato" e una penalità di energia. Sono
     * a posizione fissa, in corrispondenza dei monitor.
     */
    void aggiungi(PcVittimaPuzzle puzzle) {
        // L'enigma del PC porta con sé le proprie regole: lo conserviamo per applicarne
        // gli effetti (energia, XP) nelle varie vie di risoluzione.
        this.pcPuzzle = puzzle;
        // Nuova disposizione dei PC: azzera la traccia degli errori per il traguardo
        // "Cercatore d'oro" (PC giusto al primissimo click).
        pcSbagliatoToccato = false;
        double[][] posizioni = {
                {0.14, 0.50}, {0.25, 0.50},   // banco di sinistra
                {0.45, 0.50}, {0.56, 0.50},   // banco centrale
                {0.75, 0.50}, {0.86, 0.50}    // banco di destra: l'ultimo è il PC della vittima
        };
        int indiceCorretto = posizioni.length - 1;
        for (int i = 0; i < posizioni.length; i++) {
            boolean corretto = (i == indiceCorretto);
            ElementoScena e = new ElementoScena(TipoElemento.PORTA, "", "Esamina il PC", Color.web("#9b59b6"));
            e.posizioneFissa = true;
            e.azione = corretto ? () -> apriPc(e) : this::pcSbagliato;
            e.setVisibile(false);
            view.registra(e);
            e.posiziona(posizioni[i][0] * larghezzaMappa, posizioni[i][1] * altezzaMappa);
        }
    }

    /**
     * Interazione con il PC della vittima. Le vie, in ordine di priorità:
     * <ul>
     *     <li>password ottenuta dal tecnico (Carisma + dialogo): inserimento diretto;</li>
     *     <li>Investigazione e Intuizione &ge; 1: scelta tra enigma binario e ricerca indizi;</li>
     *     <li>solo Investigazione &ge; 1: enigma binario;</li>
     *     <li>solo Intuizione &ge; 1: ricerca del foglietto con la password;</li>
     *     <li>nessuna: solo forza bruta.</li>
     * </ul>
     * In ogni caso lo sblocco assegna {@value PcVittimaPuzzle#XP} XP, applicati dalla
     * via di risoluzione del {@link PcVittimaPuzzle}.
     */
    private void apriPc(ElementoScena elemento) {
        // Traguardo "Cercatore d'oro": il PC di Antonio è il primissimo computer
        // esaminato (nessuno dei cinque sbagliati toccato prima). Lo si annuncia col
        // dialog box prima di aprire il terminale.
        if (!pcSbagliatoToccato) {
            achievementManager.segnalaPcVittimaAlPrimoColpo();
        }
        view.annunciaTraguardoSePresente(() -> procediApertura(elemento));
    }

    /** Apertura vera e propria del terminale, secondo le vie disponibili. */
    private void procediApertura(ElementoScena elemento) {
        Player player = stato.getPlayer();
        if (stato.hasFlag(ContentResolver.FLAG_ASSISTENTE)) {
            pcPuzzle.usaPasswordAssistente(player);
            view.aggiornaHud();
            view.mostraDialogo("", "Hai inserito la password.", () -> risolto(elemento), List.of());
            return;
        }
        boolean investigatore = player.getStatistica(StatType.INVESTIGAZIONE) >= 1;
        boolean intuitivo = player.getStatistica(StatType.INTUIZIONE) >= 1;
        if (investigatore && intuitivo) {
            mostraSceltaVie(elemento);
        } else if (investigatore) {
            mostraEnigmaBinario(elemento);
        } else if (intuitivo) {
            cercaIndizi(elemento);
        } else {
            mostraForzaBruta(elemento);
        }
    }

    /**
     * Con Investigazione e Intuizione il giocatore sceglie la via: analizzare il
     * terminale (enigma binario) o cercare indizi (il foglietto con la password).
     * Resta comunque disponibile la forza bruta.
     */
    private void mostraSceltaVie(ElementoScena elemento) {
        VBox pannello = pannello("Il terminale è protetto. Come procedi?");

        Button analizza = new Button("Analizza il terminale");
        analizza.getStyleClass().add("game-button");
        analizza.setMaxWidth(380);
        analizza.setOnAction(e -> mostraEnigmaBinario(elemento));

        Button cerca = new Button("Cerca indizi intorno alla postazione");
        cerca.getStyleClass().add("game-button");
        cerca.setMaxWidth(380);
        cerca.setOnAction(e -> cercaIndizi(elemento));

        pannello.getChildren().addAll(analizza, cerca, bottoneForzaBruta(elemento), bottoneAnnulla());
        view.mostraOverlay(view.velo(pannello), true);
    }

    /**
     * Enigma binario (via Investigazione): converti il binario {@link PcVittimaPuzzle#BINARIO}
     * in decimale. La valutazione del tentativo (sblocco o penalita' di energia) e' delegata
     * a {@link PcVittimaPuzzle#tenta(Player, String)}; resta disponibile la forza bruta.
     */
    private void mostraEnigmaBinario(ElementoScena elemento) {
        Player player = stato.getPlayer();
        VBox pannello = pannello(pcPuzzle.testoEnigmaBinario());

        Label esito = new Label();
        esito.getStyleClass().add("overlay-subtitle");
        esito.setWrapText(true);
        esito.setMaxWidth(460);
        esito.setTextAlignment(TextAlignment.CENTER);

        VBox tastierino = TastierinoNumerico.crea(2, codice -> {
            PuzzleOutcome risultato = pcPuzzle.tenta(player, codice);
            view.aggiornaHud();
            if (risultato.risolto()) {
                view.mostraDialogo("", "Hai inserito la password.", () -> risolto(elemento), List.of());
                return true;
            }
            if (engine.verificaGameOver()) {
                return true;
            }
            esito.setText(risultato.messaggio());
            return false;
        });

        pannello.getChildren().addAll(tastierino, bottoneForzaBruta(elemento), bottoneAnnulla(), esito);
        view.mostraOverlay(view.velo(pannello), true);
    }

    /**
     * Via Intuizione: il sesto senso del personaggio trova il foglietto con la
     * password sotto la tastiera. Nessun costo di energia; lo sblocco è immediato.
     */
    private void cercaIndizi(ElementoScena elemento) {
        PuzzleOutcome risultato = pcPuzzle.cercaIndizi(stato.getPlayer());
        view.aggiornaHud();
        view.mostraDialogo("", risultato.messaggio(), () -> risolto(elemento), List.of());
    }

    /**
     * Schermata per chi non ha vie agevolate: resta solo la forza bruta.
     */
    private void mostraForzaBruta(ElementoScena elemento) {
        VBox pannello = pannello("Il terminale è protetto da una password che non conosci.");
        pannello.getChildren().addAll(bottoneForzaBruta(elemento), bottoneAnnulla());
        view.mostraOverlay(view.velo(pannello), true);
    }

    /** Pannello base (titolo + testo) condiviso dalle schermate del PC della vittima. */
    private VBox pannello(String descrizione) {
        Label titolo = new Label("Terminale di Antonio");
        titolo.getStyleClass().add("scene-title");
        titolo.setAlignment(Pos.CENTER);
        Label testo = new Label(descrizione);
        testo.getStyleClass().add("overlay-subtitle");
        testo.setWrapText(true);
        testo.setMaxWidth(460);
        testo.setTextAlignment(TextAlignment.CENTER);
        // Senza questo il testo, in un VBox fillWidth, resterebbe ancorato a sinistra.
        testo.setAlignment(Pos.CENTER);
        testo.setMaxWidth(Double.MAX_VALUE);

        VBox pannello = new VBox(16, titolo, testo);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(480);
        pannello.getStyleClass().add("pixel-font");
        return pannello;
    }

    /**
     * Pulsante "Annulla" del PC: chiude il terminale senza alcun costo, così il
     * giocatore può tornare indietro (ad esempio per parlare prima col tecnico) invece
     * di essere costretto alla forza bruta.
     */
    private Button bottoneAnnulla() {
        Button annulla = new Button("Annulla");
        annulla.getStyleClass().add("game-button");
        annulla.setMaxWidth(380);
        annulla.setOnAction(e -> view.chiudiOverlay());
        return annulla;
    }

    /** Pulsante "Forza bruta" del PC: penalità di energia, poi sblocco e mail. */
    private Button bottoneForzaBruta(ElementoScena elemento) {
        Button forza = new Button("Forza bruta (perdi energia)");
        forza.getStyleClass().add("game-button");
        forza.setOnAction(e -> {
            pcPuzzle.forzaBruta(stato.getPlayer());
            view.aggiornaHud();
            if (engine.verificaGameOver()) {
                return;
            }
            view.mostraDialogo("", "Dopo svariati tentativi il terminale cede.",
                    () -> risolto(elemento), List.of());
        });
        return forza;
    }

    /**
     * Interazione con un PC sbagliato: penalità di energia e avviso. Se l'energia
     * si esaurisce scatta il game over.
     */
    private void pcSbagliato() {
        // Un PC sbagliato esclude definitivamente il traguardo "Cercatore d'oro".
        pcSbagliatoToccato = true;
        Player player = stato.getPlayer();
        player.riduciEnergia(RegiaEsplorazione.COSTO_ENERGIA_DIALOGO);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        view.mostraDialogo("", "PC sbagliato.");
    }

    /**
     * Esito dello sblocco del PC della vittima: il contenuto (la mail) si mostra
     * come overlay sopra l'aula, senza cambiare scena, così alla chiusura si torna
     * all'aula con personaggio e NPC invariati.
     */
    private void risolto(ElementoScena elemento) {
        // Gli XP dello sblocco sono già stati assegnati dalla via di risoluzione
        // (PcVittimaPuzzle), qualunque essa sia stata.
        view.rimuoviElemento(elemento);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        mostraEmail();
    }

    /**
     * Mostra a tutto schermo la mail minatoria trovata sul PC: mittente in alto,
     * corpo al centro. Registra l'indizio sul mittente e, alla chiusura, fa partire
     * il pensiero del giocatore sulle iniziali.
     */
    private void mostraEmail() {
        engine.trovaIndizio(ClueCatalog.emailMittente());

        Label mittente = new Label("Mittente: em.informatica@gmail.com");
        mittente.getStyleClass().add("scene-title");
        Label corpo = new Label("Pensavi non me ne accorgessi? Dobbiamo parlare. Vediamoci in Aula B a "
                + "00:30, o stavolta finisce malissimo, non scherzo.");
        corpo.getStyleClass().add("overlay-subtitle");
        corpo.setWrapText(true);
        corpo.setMaxWidth(640);

        Button chiudi = new Button("Chiudi");
        chiudi.getStyleClass().add("game-button");
        chiudi.setOnAction(e -> {
            view.chiudiOverlay();
            mostraPensieroEmail();
        });

        // Mittente ancorato in alto, corpo al centro, pulsante in basso: come una mail.
        VBox intestazione = new VBox(mittente);
        intestazione.setAlignment(Pos.TOP_LEFT);
        BorderPane mail = new BorderPane();
        mail.setTop(intestazione);
        mail.setCenter(corpo);
        mail.setBottom(chiudi);
        BorderPane.setAlignment(chiudi, Pos.CENTER);
        BorderPane.setMargin(corpo, new Insets(32, 0, 32, 0));
        mail.setPadding(new Insets(48));
        // Riempie lo schermo, così il mittente resta ancorato in alto.
        mail.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mail.getStyleClass().add("pixel-font");
        view.mostraOverlay(view.velo(mail), false);
    }

    /**
     * Pensiero del giocatore dopo aver letto la mail: le iniziali del mittente non
     * combaciano con Alex. Alla chiusura conclude il capitolo.
     */
    private void mostraPensieroEmail() {
        view.mostraDialogo(stato.getPlayer().getNome(),
                "Mmhhh… EM... delle iniziali? Non corrispondono però al nome di Alex con cui era "
                        + "stato visto litigare prima.",
                this::concludiCapitolo, List.of());
    }

    /**
     * Fine del capitolo 2: completa formalmente il capitolo spostando il motore
     * sulla scena terminale dell'email (la transizione era stata mostrata come
     * overlay), poi propone la scelta del potenziamento. Confermandola, il motore
     * avanza al capitolo 3 con la consueta sequenza di cartelli e il pensiero che
     * indirizza il giocatore verso il Polo B.
     */
    private void concludiCapitolo() {
        // Completa il capitolo sulla scena terminale dell'email senza ricostruire la
        // vista: lo sfondo resta l'aula LA1 già presente sotto gli overlay.
        view.avanzaSenzaRicostruire("email_vittima");
        // Prima l'eventuale potenziamento a 100 XP (es. dallo sblocco del PC), poi la
        // scelta di fine capitolo (potenziamento gratuito prima del capitolo 3).
        view.mostraPotenziamentoSeDovuto(() -> view.mostraSceltaUpgrade("Capitolo completato",
                "Scegli una statistica da potenziare prima del prossimo capitolo:",
                engine::concludiCapitolo));
    }
}
