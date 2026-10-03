# messaging-lib

Librairie interne de messagerie instantanée : salons/groupes, persistance,
accusés de réception, scalabilité horizontale, notifications hors ligne.

Architecture **Controller / Service / Repository** : les entités JPA sont
directement annotées (pas de domaine séparé de la persistance), les
services appellent les repositories directement (pas d'interface
intermédiaire de type "port").

## Structure

```
messaging-lib-parent/            # pom.xml aggrégateur, gère les versions communes
├── messaging-lib/                # module principal
│   └── .../company/messaging/
│       ├── domain/               # entités JPA : Room, RoomMember, Message, MessageStatus (+ enums)
│       ├── repository/           # Spring Data JPA : RoomRepository, MessageRepository, MessageStatusRepository...
│       ├── service/               # logique métier (à venir)
│       ├── controller/            # @MessageMapping STOMP + endpoints REST (à venir)
│       ├── security/              # AuthTokenValidator, StompAuthChannelInterceptor (à venir)
│       └── config/                # WebSocket/STOMP + relais RabbitMQ (à venir)
└── messaging-notifications/      # module séparé : Web Push (VAPID), tire web-push + BouncyCastle
    └── .../messaging/notifications/{channel,push,consumer,controller}
```

**Pourquoi seulement 2 modules et pas plus** : en architecture C-S-R, le
Service appelle le Repository directement, donc scinder domaine et
persistance n'a plus d'utilité. `messaging-notifications` reste séparé
car c'est la seule vraie frontière de réutilisation : une app qui n'a pas
besoin du Web Push peut ne pas en dépendre du tout.

## Ce qui est déjà en place

- Les 3 enums du domaine : `RoomType`, `RoomMemberRole`, `MessageStatusType`.
- Les 4 entités JPA : `Room`, `RoomMember`, `Message`, `MessageStatus`
  (avec validation métier dans les factory methods, et la logique
  anti-régression des statuts directement sur `MessageStatus#advanceTo`).
- Les repositories Spring Data JPA correspondants.
- La migration Flyway `V100__create_messaging_schema.sql`.

## Vérifier que ça compile

```bash
mvn compile
```

## Prochaine étape

Le package `service/` : `ChatService` (création de message + écriture de
l'événement outbox dans la même transaction) et `MessageStatusService`
(gestion des accusés de réception avec `advanceTo`).
