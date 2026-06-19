package it.unicam.cs.mpgc.rpg126036.view;

/**
 * Strategia di allestimento di un enigma sulla mappa della scena corrente. Ogni
 * enigma del gioco si manifesta in modo diverso (la porta del laboratorio sulla
 * facciata del Polo A, i sei PC del laboratorio della vittima): un allestitore
 * incapsula sia la creazione del relativo {@link it.unicam.cs.mpgc.rpg126036.model.Puzzle}
 * col suo tipo concreto, sia la disposizione degli elementi interattivi.
 *
 * <p>La schermata di esplorazione associa a ciascun id di enigma il proprio
 * allestitore (registro): così non conosce i tipi concreti degli enigmi né effettua
 * conversioni di tipo, e aggiungere un nuovo enigma non richiede di modificarne la
 * logica di ricostruzione della scena (Open/Closed).</p>
 */
@FunctionalInterface
interface AllestitoreEnigma {

    /** Crea l'enigma e ne dispone gli elementi interattivi sulla mappa della scena. */
    void allestisci();
}
