package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;

/**
 * Personaggio non giocante. Possiede un'identita' (identificativo e nome
 * visualizzato) e un {@link Dialogue} che ne governa le battute.
 *
 * <p>Il flag {@code utile} distingue gli NPC che forniscono informazioni
 * rilevanti per l'indagine da quelli puramente di contorno.</p>
 */
public class Npc {

    private final String id;
    private final String nome;
    private final boolean utile;
    private final Dialogue dialogo;

    /**
     * @param id      identificativo univoco dell'NPC (non nullo)
     * @param nome    nome visualizzato (non nullo)
     * @param utile   {@code true} se l'NPC fornisce informazioni rilevanti
     * @param dialogo i dati di dialogo dell'NPC (non nulli)
     */
    public Npc(String id, String nome, boolean utile, Dialogue dialogo) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.nome = Objects.requireNonNull(nome, "Il nome non puo' essere nullo.");
        this.utile = utile;
        this.dialogo = Objects.requireNonNull(dialogo, "Il dialogo non puo' essere nullo.");
    }

    /**
     * Fa parlare l'NPC con il personaggio, applicando le condizioni del dialogo.
     *
     * @param player il personaggio che interagisce (non nullo)
     * @return la battuta pronunciata dall'NPC
     */
    public String parla(Player player) {
        return dialogo.parlaCon(player);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public boolean isUtile() {
        return utile;
    }

    public Dialogue getDialogo() {
        return dialogo;
    }
}
