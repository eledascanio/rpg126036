# 📌 Camerino Files

**Camerino Files** è un gioco di ruolo investigativo 2D (vista dall'alto) realizzato in JavaFX.
Il giocatore veste i panni di uno studente che, nella notte di una festa universitaria, indaga
sull'omicidio del compagno Antonio: esplora le ambientazioni, dialoga con i personaggi, raccoglie
indizi e prove e risolve enigmi sfruttando le statistiche del proprio personaggio, capitolo dopo
capitolo, prima dell'arrivo della polizia.

---

## 🚀 Come eseguire il progetto

### Prerequisiti
- Java 25 (LTS)
- Gradle (è incluso il *wrapper* `./gradlew`, quindi un'installazione separata non è necessaria)

### Istruzioni

```bash
git clone <url-del-repository>
cd rpg126036
```

### Build del progetto
```bash
./gradlew build
```

### Esecuzione
```bash
./gradlew run
```

> Su Windows, in alternativa a `./gradlew`, usare `gradlew.bat`.
> I salvataggi vengono creati automaticamente a fine capitolo nella cartella `saves/` (esclusa dal versionamento).

---

## 🤖 Uso di strumenti di AI

Gli strumenti di AI sono stati impiegati esclusivamente come **supporto** allo sviluppo: l'intera
architettura, il codice e le scelte progettuali sono state **comprese, adattate e verificate
personalmente**. L'AI non ha sostituito la progettazione né l'implementazione, ma ha aiutato in
attività puntuali, sempre seguite da revisione e test manuali.

* **Stitch** (AI generativa di immagini) per:
  * la creazione degli **sprite dei personaggi**;
  * la generazione degli **sfondi delle stanze/ambientazioni**.

* **ChatGPT** per:
  * **validare le scelte architetturali** alla luce dei principi **SOLID** (responsabilità delle classi,
    separazione tra modello, motore di gioco, persistenza e interfaccia grafica);
  * chiarire dubbi puntuali e confrontare possibili alternative di design, poi valutate e decise manualmente.

In tutti i casi il materiale prodotto dall'AI (immagini e suggerimenti) è stato **rielaborato e integrato
manualmente** nel progetto: gli asset grafici sono stati selezionati e adattati, mentre i suggerimenti
architetturali sono stati applicati solo dopo averne compreso le motivazioni e verificato la coerenza
con il resto del codice.

📌 Per una descrizione più dettagliata dell'uso dell'AI, consultare la **Wiki del repository**.
