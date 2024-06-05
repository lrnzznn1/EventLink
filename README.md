# EventLink

![EventLink Logo](Logo.png)

## Indice
1. [Introduzione](#introduzione)
2. [Contesto e Motivazione](#contesto-e-motivazione)
3. [Analisi della Concorrenza](#analisi-della-concorrenza)
4. [Tecnologie Utilizzate](#tecnologie-utilizzate)
5. [Implementazione del Codice: Aspetti Fondamentali](#implementazione-del-codice-aspetti-fondamentali)
    - [other](#other)
    - [pages + MainActivity](#pages--mainactivity)
    - [res](#res)
    - [Firestore Database](#firestore-database)
    - [Server Storage](#server-storage)
    - [Server Functions](#server-functions)
6. [Test e Valutazione dei Risultati](#test-e-valutazione-dei-risultati)
7. [Schermate dell'applicazione](#schermate-dellapplicazione)
8. [Conclusioni e Prospettive Future](#conclusioni-e-prospettive-future)

## Introduzione

Questa repository contiene il codice sorgente di **EventLink**, un'applicazione sviluppata da Lorenzo Masè e Lorenzo Zanini per il progetto di Programmazione Mobile 2024, tenuto dal Professore Roberto Battiti dell'Università degli Studi di Trento.

## Contesto e Motivazione

L'idea di EventLink nasce dal bisogno delle persone di informarsi sugli eventi disponibili in modo rapido e sicuro, offrendo un'alternativa più mirata rispetto ai social media. L'app è pensata per persone di tutte le età e mira a facilitare la partecipazione agli eventi, offrendo informazioni chiare e accessibili.

## Analisi della Concorrenza

Il settore delle app per eventi è già molto saturo. Abbiamo esaminato app come Dice e EventBrite e identificato vari problemi, come bug e informazioni poco chiare. EventLink punta a risolvere questi problemi offrendo una mappa geografica chiara e dettagliata, simile a PrezziBenzina, con una visualizzazione immediata degli eventi.

## Tecnologie Utilizzate

- **Linguaggi**: Kotlin, XML, Java
- **IDE**: Android Studio
- **Database**: Firebase Firestore Database, Java (locale)
- **API**: Google Maps
- **Server**: Firebase Functions (Node.js)
- **Automazione**: Script Python
- **Versionamento**: GitHub
- **Storage**: Firebase Storage
- **Email**: Gmail
- **Grafica**: XML, .png files

## Implementazione del Codice: Aspetti Fondamentali

### other

- **Cluster**: Gestione marker sulla mappa (MyClusterItem e CustomClusterRenderer).
- **EventoLocale e DAOEventoLocale**: Strutturazione dell'entità evento nel database locale.
- **Funzioni**: Raccolta di funzioni globali, es. rawJSON per inviare JSON al server Node.js.
- **Evento**: Creazione di una lista eventi per evitare accessi ripetuti al database.

### pages + MainActivity

- **MainActivity**: Gestione della mappa e delle principali pagine dell'app (Menu, Preferiti, Lista).
- **Evento**: Dettagli evento, gestione preferiti e prenotazioni.
- **PaginaLogin, PaginaSignIn, PaginaDimenticata**: Gestione accesso e registrazione utenti.
- **PaginaProfilo**: Gestione prenotazioni e impostazioni utente.

### res

Contiene i file XML delle pagine, le animazioni, le immagini e le stringhe utilizzate nell'applicazione.

### Firestore Database

- **Eventi**: Istanze degli eventi inseriti dalle aziende.
- **Utenti**: Dati degli utenti registrati.
- **Prenotazioni**: Relazione Utente-Evento.

![Database](Screenshots/db.png)

### Server Storage

Gestione delle immagini degli eventi tramite Firebase Storage e la classe Glide.

### Server Functions

Gestione delle richieste client (registrazione, cambio password, prenotazioni) tramite metodi POST al server Node.js.

## Test e Valutazione dei Risultati

Il testing è stato effettuato su vari dispositivi, sia virtualizzati che fisici, con riscontri positivi. Le email vengono inviate correttamente, e i database locale e Firebase gestiscono adeguatamente le operazioni di aggiornamento.


## Conclusioni e Prospettive Future

EventLink è funzionante e sicura. Per il futuro, si prevede di creare una pagina web per le aziende per l'inserimento degli eventi, consolidare la base di eventi e aumentare la partecipazione degli utenti. L'obiettivo è garantire una base stabile di eventi ricorrenti per facilitare l'adozione dell'app sul mercato.

Ringraziamo i lettori per l'attenzione.

---

Lorenzo Zanini & Lorenzo Masè
