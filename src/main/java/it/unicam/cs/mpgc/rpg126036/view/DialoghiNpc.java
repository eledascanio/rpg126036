package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.NpcCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatType;

import java.util.List;
import java.util.Objects;

/**
 * Dialoghi con gli NPC: la battuta standard (con eventuale indizio nel diario) e le
 * interazioni su misura che alcuni personaggi richiedono —
 * <ul>
 *     <li>il <b>tecnico di laboratorio</b>: con Carisma sufficiente uno scambio a più
 *         battute che sblocca la password del PC della vittima;</li>
 *     <li>l'<b>addetto alle pulizie</b> (capitolo 3): con Carisma &ge; 2 apre la porta
 *         laterale dell'Aula B, assegna XP e rivela l'indizio;</li>
 *     <li>lo <b>studente ubriaco</b>: col Carisma recupera l'energia spesa e, su scelta,
 *         rivela il litigante.</li>
 * </ul>
 *
 * <p>Estratti dalla {@link ExplorationView} per isolare questa porzione di trama: si
 * appoggiano alla schermata (collaboratore) per il dialog box, l'HUD, il
 * potenziamento e il pensiero d'indagine, senza duplicarne l'infrastruttura.</p>
 */
final class DialoghiNpc {

    // XP guadagnati convincendo l'addetto ad aprire la porta dell'Aula B.
    private static final int ADDETTO_XP = 60;

    private final ExplorationView view;
    private final GameState stato;
    private final GameEngine engine;
    private final ContentResolver resolver;

    /**
     * @param view     la schermata di esplorazione che offre i servizi di scena
     * @param stato    lo stato di gioco (giocatore, flag)
     * @param engine   il motore di gioco (indizi, game over)
     * @param resolver il risolutore dei contenuti (indizi associati agli NPC)
     */
    DialoghiNpc(ExplorationView view, GameState stato, GameEngine engine, ContentResolver resolver) {
        this.view = Objects.requireNonNull(view, "La schermata non puo' essere nulla.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.engine = Objects.requireNonNull(engine, "Il motore non puo' essere nullo.");
        this.resolver = Objects.requireNonNull(resolver, "Il resolver non puo' essere nullo.");
    }

    /** Dialogo standard: battuta dell'NPC e, se accessibile, indizio nel diario. */
    void conNpc(Npc npc) {
        Player player = stato.getPlayer();
        // Parlare con un NPC costa energia; se si esaurisce, scatta il game over.
        player.riduciEnergia(ExplorationView.COSTO_ENERGIA_DIALOGO);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        String battuta = npc.parla(player);

        StringBuilder testo = new StringBuilder(battuta);
        if (npc.getDialogo().isAccessibile(player)) {
            resolver.indizioDi(npc.getId()).ifPresent(indizio -> {
                if (engine.trovaIndizio(indizio)) {
                    testo.append("\n\n🔎 Nuovo indizio nel diario: ").append(indizio.titolo());
                }
            });
        }
        view.mostraDialogo(npc.getNome(), testo.toString());
    }

    /**
     * Interazione su misura con il tecnico di laboratorio. Costa energia come ogni
     * dialogo; con Carisma sufficiente lo scambio si svolge in tre battute scorrevoli
     * (tecnico, giocatore, tecnico) e sblocca la password del PC. Sotto soglia il
     * tecnico resta sotto shock e non rivela nulla.
     */
    void conTecnico(Npc npc) {
        Player player = stato.getPlayer();
        player.riduciEnergia(ExplorationView.COSTO_ENERGIA_DIALOGO);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        if (!npc.getDialogo().isAccessibile(player)) {
            view.mostraDialogo(npc.getNome(), npc.parla(player));
            return;
        }
        // L'aver parlato con l'assistente abilita la via "Carisma" del PC della vittima.
        stato.setFlag(ContentResolver.FLAG_ASSISTENTE);
        List<String[]> battute = List.of(
                new String[]{npc.getNome(), NpcCatalog.TECNICO_BATTUTA_1},
                new String[]{player.getNome(), NpcCatalog.TECNICO_BATTUTA_GIOCATORE_1},
                new String[]{player.getNome(), NpcCatalog.TECNICO_BATTUTA_GIOCATORE_2},
                new String[]{npc.getNome(), NpcCatalog.TECNICO_BATTUTA_3});
        mostraSequenza(battute, 0, () -> concludiTecnico(npc));
    }

    /**
     * Mostra in sequenza le battute (nome, testo): premendo E si passa alla
     * successiva; dopo l'ultima si esegue {@code alTermine}.
     */
    private void mostraSequenza(List<String[]> battute, int indice, Runnable alTermine) {
        String[] battuta = battute.get(indice);
        boolean ultima = indice >= battute.size() - 1;
        view.mostraDialogo(battuta[0], battuta[1], () -> {
            if (ultima) {
                alTermine.run();
            } else {
                mostraSequenza(battute, indice + 1, alTermine);
            }
        }, List.of());
    }

    /**
     * Conclusione del dialogo con il tecnico: registra l'indizio sulla password e,
     * se è nuovo, lo annuncia in un'ultima battuta; altrimenti chiude.
     */
    private void concludiTecnico(Npc npc) {
        var indizio = resolver.indizioDi(npc.getId());
        if (indizio.isPresent() && engine.trovaIndizio(indizio.get())) {
            view.mostraDialogo("", "🔎 Nuovo indizio nel diario: " + indizio.get().titolo());
        } else {
            view.chiudiOverlay();
        }
    }

    /**
     * Interazione su misura con l'addetto alle pulizie nel cortile (capitolo 3).
     * L'addetto respinge tutti; con Carisma &ge; 2 il giocatore lo convince in una
     * sequenza di battute (rifiuto, replica, accettazione): l'addetto apre la porta
     * laterale dell'Aula B, assegna {@value #ADDETTO_XP} XP e rivela l'indizio. Sotto
     * soglia il rifiuto costa {@link ExplorationView#COSTO_ENERGIA_DIALOGO} di energia.
     * Una volta aperta la porta, ulteriori interazioni si limitano a sollecitare.
     */
    void conAddetto(Npc npc) {
        if (stato.hasFlag(ContentResolver.FLAG_PORTA_AULA_B)) {
            view.mostraDialogo(npc.getNome(), "Sbrigati, ti ho già aperto la porta laterale.");
            return;
        }
        Player player = stato.getPlayer();
        if (npc.getDialogo().isAccessibile(player)) {
            List<String[]> battute = List.of(
                    new String[]{npc.getNome(), NpcCatalog.ADDETTO_RIFIUTO},
                    new String[]{player.getNome(), NpcCatalog.ADDETTO_GIOCATORE},
                    new String[]{npc.getNome(), NpcCatalog.ADDETTO_ACCETTA});
            mostraSequenza(battute, 0, () -> concludiAddetto(npc));
            return;
        }
        // Carisma insufficiente: rifiuto secco, a costo di energia.
        player.riduciEnergia(ExplorationView.COSTO_ENERGIA_DIALOGO);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        view.mostraDialogo(npc.getNome(), NpcCatalog.ADDETTO_RIFIUTO);
    }

    /**
     * Conclusione del dialogo riuscito con l'addetto: sblocca la porta dell'Aula B,
     * assegna gli XP e registra (annunciandolo) l'indizio sullo studente in fuga.
     */
    private void concludiAddetto(Npc npc) {
        stato.setFlag(ContentResolver.FLAG_PORTA_AULA_B);
        stato.getPlayer().aggiungiXp(ADDETTO_XP);
        view.aggiornaHud();
        var indizio = resolver.indizioDi(npc.getId());
        if (indizio.isPresent() && engine.trovaIndizio(indizio.get())) {
            view.mostraDialogo("", "🔎 Nuovo indizio nel diario: " + indizio.get().titolo(),
                    () -> view.mostraPotenziamentoSeDovuto(view::chiudiOverlay), List.of());
        } else {
            view.mostraPotenziamentoSeDovuto(view::chiudiOverlay);
        }
    }

    /**
     * Interazione su misura con lo studente ubriaco. Come ogni dialogo costa
     * {@link ExplorationView#COSTO_ENERGIA_DIALOGO} di energia; se il giocatore ha
     * Carisma &gt; 0 (ha scelto il rappresentante) l'NPC lo riconosce e gli offre un
     * sorso, restituendo l'energia appena spesa quando il giocatore passa la battuta
     * (premendo E). In ogni caso prosegue con la battuta comune a scelte.
     */
    void conStudenteUbriaco(Npc npc) {
        Player player = stato.getPlayer();
        player.riduciEnergia(ExplorationView.COSTO_ENERGIA_DIALOGO);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        if (player.getStatistica(StatType.CARISMA) > 0) {
            String saluto = "Ehi, ma tu sei il rappresentante! Tieni, fatti un goccio... "
                    + "alla salute di Antonio, povero ragazzo.";
            // Al passaggio della battuta si recupera l'energia spesa per parlargli.
            view.mostraDialogo(npc.getNome(), saluto, () -> {
                player.ripristinaEnergia(ExplorationView.COSTO_ENERGIA_DIALOGO);
                view.aggiornaHud();
                mostraUbriacoConScelte(npc);
            }, List.of());
        } else {
            mostraUbriacoConScelte(npc);
        }
    }

    /**
     * Battuta comune dello studente ubriaco (mostrata a tutti i giocatori) con le
     * due scelte: chiedere dettagli per ottenere l'indizio o lasciar perdere.
     */
    private void mostraUbriacoConScelte(Npc npc) {
        String battuta = "Ho visto Antonio discutere con qualcuno prima... mi pare.";
        view.mostraDialogo(npc.getNome(), battuta, view::chiudiOverlay, List.of(
                new OpzioneDialogo("Chiedi dettagli", () -> chiediDettagliUbriaco(npc)),
                new OpzioneDialogo("Lascia stare", view::chiudiOverlay)));
    }

    /**
     * Esito della scelta "chiedi dettagli": la prima volta assegna +20 XP e
     * registra nel diario l'indizio sul litigante; poi mostra la rivelazione.
     */
    private void chiediDettagliUbriaco(Npc npc) {
        Player player = stato.getPlayer();
        StringBuilder testo = new StringBuilder("Mi sembrava di averlo visto litigare con Alex Kaur.");
        resolver.indizioDi(npc.getId()).ifPresent(indizio -> {
            if (engine.trovaIndizio(indizio)) {
                player.aggiungiXp(20);
                testo.append("\n\n🔎 Nuovo indizio nel diario: ").append(indizio.titolo());
            }
        });
        view.aggiornaHud();
        // Alla chiusura della battuta, se ora il giocatore ha sia la chiave sia
        // l'indizio, parte il pensiero che lo indirizza verso il PC di Antonio.
        view.mostraDialogo(npc.getNome(), testo.toString(),
                () -> view.mostraPotenziamentoSeDovuto(() -> {
                    view.chiudiOverlay();
                    view.forsePensieroIndagine();
                }), List.of());
    }
}
