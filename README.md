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

## 🏗️ Architettura

Il progetto è organizzato a **strati con responsabilità separate**, secondo i principi **SOLID**:
l'interfaccia grafica dipende dal motore e dal modello, mai il contrario. Tutti i package vivono
sotto `it.unicam.cs.mpgc.rpg126036`.

| Package | Responsabilità |
|---|---|
| `model` | Entità di dominio: `Player`, `Scene`, `Chapter`, `Item`, `Npc`, `Clue`, `Puzzle`, `StatType`, … |
| `interaction` | Interazioni polimorfiche (`Interaction` e i sottotipi `Dialogue`, `Item`, `StatCheck`, `Simple`) con il loro esito |
| `engine` | Motore di gioco: `GameEngine` mantiene lo stato (`GameState`) e notifica i `GameListener` |
| `persistence` | Caricamento della campagna (`CampaignLoader`, `ChapterLoader`) e salvataggi (`SaveRepository`/`XmlSaveRepository`, 3 slot) |
| `achievement` | Traguardi globali del giocatore (`AchievementManager` e relativi listener) |
| `app` | Composizione e avvio: `Main`, `GameSession`, `GameSessionFactory`, `ContentResolver` |
| `view` | Interfaccia grafica JavaFX (schermate e componenti) |

### Motore ed eventi (Observer)

`GameEngine` non conosce l'interfaccia grafica: pubblica gli eventi di gioco (cambio scena,
avanzamento di capitolo, game over, completamento) ai `GameListener`. Vi reagiscono — disaccoppiati —
la schermata di esplorazione, l'**autosalvataggio** di fine capitolo e la pulizia dei salvataggi al
game over.

### La schermata di esplorazione

La schermata di gioco è scomposta in **collaboratori coesi**, ognuno con una sola responsabilità.
Per invertire la dipendenza (**DIP**), i componenti di trama dipendono dal contratto
`RegiaEsplorazione` e non dalla classe concreta `ExplorationView`, che ne resta il coordinatore.

| Componente | Responsabilità |
|---|---|
| `ExplorationView` | Coordinatore: reagisce agli eventi del motore e delega ai collaboratori |
| `RegiaEsplorazione` | Contratto dei servizi di scena offerti ai componenti di trama |
| `Personaggio` | Avatar giocante: sprite, posizione, ciclo di gioco, input e prossimità |
| `MappaCollisioni` | Rilevamento delle collisioni del personaggio con i muri (logica pura) |
| `DisposizioneScena` | Geometria del layout: colloca gli elementi su slot o a fasce |
| `GestoreOverlay` | Pannelli modali: dialoghi, messaggi, pausa, schermate di fine capitolo |
| `HudEsplorazione` | Barra di stato: energia, XP e statistiche |
| `EffettoTorcia` | Velo e cono di luce dell'Aula B al buio |
| `Porte`, `AulaB`, `PcVittima`, `DialoghiNpc`, `Pensieri` | Trama dei capitoli, ciascun componente per la propria porzione |

Aggiungere o modificare un capitolo agisce sul componente di trama che lo riguarda, lasciando la
schermata **chiusa alle modifiche e aperta all'estensione** (**OCP**).

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
