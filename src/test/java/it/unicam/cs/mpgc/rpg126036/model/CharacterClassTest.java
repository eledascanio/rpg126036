package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica il modello delle statistiche iniziali: ogni classe parte da 0 e
 * assegna un unico bonus di +1 alla statistica caratterizzante.
 */
class CharacterClassTest {

    /** Somma delle statistiche iniziali della classe. */
    private int totale(CharacterClass classe) {
        return classe.statisticaIniziale(StatType.INVESTIGAZIONE)
                + classe.statisticaIniziale(StatType.CARISMA)
                + classe.statisticaIniziale(StatType.INTUIZIONE);
    }

    @Test
    void ogniClasseAssegnaUnSoloPuntoTotale() {
        for (CharacterClass classe : CharacterClass.values()) {
            assertEquals(1, totale(classe),
                    "La classe " + classe + " deve assegnare un solo punto in totale.");
        }
    }

    @Test
    void ilBonusEAssegnatoAllaStatisticaCaratterizzante() {
        assertEquals(1, CharacterClass.STUDENTE_MODELLO.statisticaIniziale(StatType.INVESTIGAZIONE));
        assertEquals(1, CharacterClass.RAPPRESENTANTE_STUDENTI.statisticaIniziale(StatType.CARISMA));
        assertEquals(1, CharacterClass.ORGANIZZATORE_EVENTI.statisticaIniziale(StatType.INTUIZIONE));
    }

    @Test
    void leAltreStatistichePartonoDaZero() {
        assertEquals(0, CharacterClass.STUDENTE_MODELLO.statisticaIniziale(StatType.CARISMA));
        assertEquals(0, CharacterClass.STUDENTE_MODELLO.statisticaIniziale(StatType.INTUIZIONE));
        assertEquals(0, CharacterClass.RAPPRESENTANTE_STUDENTI.statisticaIniziale(StatType.INVESTIGAZIONE));
        assertEquals(0, CharacterClass.ORGANIZZATORE_EVENTI.statisticaIniziale(StatType.INVESTIGAZIONE));
    }

    @Test
    void ilNomeVisualizzatoEQuelloDellaSpecifica() {
        assertEquals("Studente modello", CharacterClass.STUDENTE_MODELLO.getNomeVisualizzato());
        assertEquals("Rappresentante degli studenti", CharacterClass.RAPPRESENTANTE_STUDENTI.getNomeVisualizzato());
        assertEquals("Organizzatore di eventi", CharacterClass.ORGANIZZATORE_EVENTI.getNomeVisualizzato());
    }

    @Test
    void laStatisticaPrincipaleEQuellaConIlBonus() {
        assertEquals(StatType.INVESTIGAZIONE, CharacterClass.STUDENTE_MODELLO.statisticaPrincipale());
        assertEquals(StatType.CARISMA, CharacterClass.RAPPRESENTANTE_STUDENTI.statisticaPrincipale());
        assertEquals(StatType.INTUIZIONE, CharacterClass.ORGANIZZATORE_EVENTI.statisticaPrincipale());
    }
}
