# EventLink

![EventLink Logo](Logo.png)

EventLink è un progetto sviluppato per l'esame di Mobile Programming. L'applicazione consente agli utenti di visualizzare eventi in varie città italiane su una mappa interattiva e di ottenere informazioni dettagliate sugli eventi.

## Funzionalità

- Visualizzazione degli eventi su una mappa interattiva.
- Informazioni dettagliate sugli eventi, inclusi titolo, descrizione e posizione.
- Possibilità di aggiungere nuovi eventi tramite indirizzo.
- Personalizzazione della finestra di informazioni del marker con titolo, descrizione e bottoni per azioni aggiuntive.

## Tecnologie Utilizzate

- Linguaggio di programmazione: Kotlin
- Framework: Android SDK
- Librerie aggiuntive: Google Maps Android API

## Autori

Questo progetto è stato creato da Lorenzo Zanini e Lorenzo Mase come parte del loro esame di Mobile Programming. Grazie per la visione.

### Pagina Login

`PaginaLogin` è un'attività che gestisce l'accesso degli utenti all'applicazione.

#### Funzionalità

- Validazione dei campi Email e Password
- Controllo dell'autenticazione tramite Firebase Firestore
- Reindirizzamento alla pagina del profilo in caso di autenticazione riuscita

#### Codice Principale

`PaginaLogin` gestisce l'interfaccia utente e le operazioni di login.

- **onCreate()**: Inizializza gli elementi UI e imposta i click listener per i pulsanti.
- **passwordCheck()**: Funzione sospesa per controllare se la password fornita corrisponde a quella memorizzata nel database.

#### Validazione dei Campi

La pagina richiede l'inserimento di un'email e di una password. Se uno o entrambi i campi sono vuoti, viene visualizzato un messaggio di avviso.

#### Controllo dell'Autenticazione

La funzione `passwordCheck()` controlla se l'email e la password inserite corrispondono a quelle memorizzate nel database Firestore.

#### Reindirizzamento

Se l'autenticazione ha successo, l'utente viene reindirizzato alla pagina del profilo.

#### Click Listeners

Vengono impostati click listener per i pulsanti di registrazione, accesso e navigazione.

## Pagina Sign-In

`PaginaSignIn` è un'attività che gestisce la registrazione degli utenti nell'applicazione.

### Funzionalità

- Validazione dei campi di registrazione
- Controllo dell'esistenza di un account con la stessa email nel database
- Creazione di un nuovo documento utente in Firestore
- Invio di una email di conferma di registrazione

### Codice Principale

`PaginaSignIn` gestisce l'interfaccia utente e le operazioni di registrazione.

- **onCreate()**: Inizializza gli elementi UI e imposta i click listener per il pulsante di registrazione.
- **generateRandomPassword()**: Genera una password casuale per il nuovo utente.
- **existsInDB()**: Controlla se un documento esiste nella collezione specificata di Firestore.
- **rawJSON()**: Invia un payload JSON grezzo a un URL specificato tramite una richiesta POST.

### Validazione dei Campi

La pagina richiede l'inserimento di nome, cognome, email e numero di telefono. Se uno o più campi sono vuoti, viene visualizzato un messaggio di avviso.

### Controllo dell'Esistenza dell'Account

Prima di procedere con la registrazione, viene controllato se esiste già un account con la stessa email nel database. Se esiste, viene visualizzato un messaggio di errore.

### Creazione dell'Account

Se l'utente è nuovo, viene creato un nuovo documento utente in Firestore con le informazioni fornite e viene inviata una email di conferma di registrazione.

### Click Listener

Viene impostato un click listener per il pulsante di registrazione.

## Contributi

I contributi sono benvenuti! Sentiti libero di fare fork del progetto e inviare pull request.

## Licenza

Distribuito sotto licenza MIT. Vedi `LICENSE` per più informazioni.

Se hai bisogno di ulteriori modifiche o integrazioni, sono qui per aiutarti!
