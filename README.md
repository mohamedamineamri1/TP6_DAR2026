# TP 6 — Service Banque avec CORBA (Common Object Request Broker Architecture)
Matière : Développement d'applications réparties Auditoire : LSI 3 — Faculté des Sciences de Sfax — 2026/2027

Enseignante des TPs : Mme Zeineb DAOUD

Étudiant : Mohamed Amine AMRI

# Sommaire
TP6_CorbaBanque_Server/	Serveur : IDL, servant BanqueImpl, serveur BanqueServer

TP6_CorbaBanque_Client/	Client : stub CORBA et classe BanqueClient

# Fonctionnement détaillé
Principe de CORBA

CORBA est un middleware qui permet à un client d'appeler les méthodes d'un objet distant comme s'il était local.
L'interface du service est décrite dans un fichier IDL, indépendant du langage.
Le compilateur idlj génère à partir de l'IDL le Stub (côté client) et le Skeleton/POA (côté serveur).
L'ORB assure le transport des requêtes (protocole IIOP) et la sérialisation des paramètres.
Le Naming Service (tnameserv) joue le rôle d'annuaire : le serveur y publie la référence de l'objet, le client la retrouve par son nom.

Opérations du service bancaire : créer un compte, verser, retirer, consulter un compte, lister les comptes, convertir Euro → Dinar Tunisien.

# Étape 1 — Préparation de l'espace de travail

Création du dossier de travail, lancement d'Eclipse avec ce dossier comme Workspace,
vérification du JDK 8 (CORBA, idlj et tnameserv n'existent plus à partir du JDK 11).

# Étape 2 — Développement du serveur

# Interface IDL (Banque.idl)

Module `corbaBanque` contenant :
- la structure `Compte` (code : long, solde : float) ;
- le type `tabComptes` (sequence de Compte) ;
- l'interface `IBanqueRemote` : creerCompte, verser, retirer, getCompte, getComptes, conversion.

Compilation : `idlj -fall -v Banque.idl` dans le répertoire src.
Le package `corbaBanque` est généré (Compte, IBanqueRemote, IBanqueRemotePOA, _IBanqueRemoteStub, Helpers, Holders).

# Servant (service.BanqueImpl)

La classe hérite de `IBanqueRemotePOA` et implémente toutes les méthodes de `IBanqueRemoteOperations`.
Les comptes sont stockés dans une `Map<Integer, Compte>`, et les méthodes sont `synchronized`.
La conversion utilise un taux fixe de 1 € = 3,4 DT.

# Serveur (corbaServer.BanqueServer)

ORB.init() initialise l'ORB.
resolve_initial_references("RootPOA") récupère le RootPOA, puis the_POAManager().activate() l'active.
Le contexte JNDI est configuré avec CNCtxFactory et l'URL iiop://localhost:900 (fichier jndi.properties).
Le servant BanqueImpl est créé, sa référence est enregistrée dans l'annuaire avec rebind("BanqueService", ref).
orb.run() met le serveur en attente des requêtes.

# Étape 3 — Développement du client

Création du projet client, copie de Banque.idl et jndi.properties dans src, puis recompilation de l'IDL avec idlj.

# Client (corbaClient.BanqueClient)

Récupération des propriétés JNDI, puis lookup("BanqueService") pour obtenir la référence distante.
IBanqueRemoteHelper.narrow(ref) convertit la référence en Stub.
Le client appelle ensuite chaque méthode du service via le stub.


# Étape 4 — Exécution

L'ordre est obligatoire : annuaire, serveur, client.

1. Annuaire : `tnameserv` (port 900 par défaut)
2. Serveur : exécution de BanqueServer
3. Client : exécution de BanqueClient

<img width="1188" height="297" alt="Capture d&#39;écran 2026-11-21 213252" src="https://github.com/user-attachments/assets/edbaf3ce-5b8e-4e24-9345-7a91c66eacd1" />
<img width="772" height="291" alt="Capture d&#39;écran 2026-11-21 213642" src="https://github.com/user-attachments/assets/621e347a-42bf-45d0-bcec-6119bc7dc403" />
<img width="962" height="465" alt="Capture d&#39;écran 2026-11-21 213657" src="https://github.com/user-attachments/assets/544c6a38-5b92-4f20-b44c-d4f2267d8d48" />


# Synthèse

L'IDL définit le contrat du service indépendamment du langage, idlj en génère le Stub et le Skeleton.
Le client manipule un stub comme un objet local : c'est la transparence de localisation.
Le Naming Service découple client et serveur : le client n'a besoin que du nom de l'objet, pas de son adresse.
Le POA gère le cycle de vie du servant, l'ORB gère le transport (IIOP) et le marshalling des paramètres.
Une struct IDL ne peut pas être null : getCompte renvoie un compte de code -1 si le compte n'existe pas.
Les méthodes void (verser, retirer, creerCompte) refusent silencieusement les opérations invalides (compte inconnu, solde insuffisant) et le signalent côté serveur.
Limites : les comptes sont en mémoire et perdus à l'arrêt du serveur, il n'y a pas d'authentification et le taux de conversion est fixe.
Améliorations possibles : exceptions IDL (CompteInexistant, SoldeInsuffisant), persistance dans une base de données, taux de change en temps réel, interface graphique.
