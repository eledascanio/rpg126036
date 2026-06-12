package it.unicam.cs.mpgc.rpg126036.achievement;

/**
 * Osservatore dello sblocco dei traguardi. Consente alla vista di reagire
 * (ad es. mostrando una notifica) quando l' {@link AchievementManager} sblocca
 * un {@link Achievement}.
 */
@FunctionalInterface
public interface AchievementListener {

    /**
     * Invocato quando un traguardo viene sbloccato per la prima volta.
     *
     * @param achievement il traguardo appena sbloccato
     */
    void onAchievementSbloccato(Achievement achievement);
}
