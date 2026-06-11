package it.unicam.cs.mpgc.rpg126036.persistence;

/**
 * Metadati di uno slot di salvataggio, usati per mostrare l'elenco delle partite
 * senza caricare l'intero stato.
 *
 * @param nomePersonaggio nome del personaggio (e dello slot)
 * @param classe          classe del personaggio
 * @param idCapitolo      id del capitolo corrente al momento del salvataggio
 * @param idScena         id della scena corrente al momento del salvataggio
 */
public record SaveSlot(String nomePersonaggio, String classe, String idCapitolo, String idScena) {
}
