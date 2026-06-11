package it.unicam.cs.mpgc.rpg126036.persistence;

/**
 * Errore durante la lettura o la scrittura di un salvataggio.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }

    public PersistenceException(String messaggio) {
        super(messaggio);
    }
}
