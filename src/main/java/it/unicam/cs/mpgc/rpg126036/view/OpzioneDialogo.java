package it.unicam.cs.mpgc.rpg126036.view;

/**
 * Una scelta proposta al termine di un dialog box: l'etichetta del pulsante e
 * l'azione eseguita alla selezione.
 *
 * @param etichetta il testo del pulsante
 * @param azione    l'azione eseguita quando la scelta viene selezionata
 */
record OpzioneDialogo(String etichetta, Runnable azione) {
}
