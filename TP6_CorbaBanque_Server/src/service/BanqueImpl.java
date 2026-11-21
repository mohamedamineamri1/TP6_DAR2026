package service;
import java.util.LinkedHashMap;
import java.util.Map;
import Banque.Compte;
import Banque.IBanqueRemotePOA;

/**
 * Servant CORBA : implémentation métier du service bancaire.
 * Hérite de IBanqueRemotePOA (généré par idlj) qui implémente
 * IBanqueRemoteOperations.
 */
public class BanqueImpl extends IBanqueRemotePOA {

    // 1 EUR = 3.4 DT (taux indicatif, à adapter)
    private static final double TAUX_EUR_DT = 3.4;
    private final Map<Integer, Compte> comptes = new LinkedHashMap<Integer, Compte>();
    @Override
    public synchronized void creerCompte(Compte cpte) {
        if (comptes.containsKey(cpte.code)) {
            System.out.println("[Serveur] Le compte " + cpte.code + " existe deja.");
            return;
        }
        comptes.put(cpte.code, new Compte(cpte.code, cpte.solde));
        System.out.println("[Serveur] Compte cree : code=" + cpte.code + ", solde=" + cpte.solde);
    }

    @Override
    public synchronized void verser(float mt, int code) {
        Compte c = comptes.get(code);
        if (c == null || mt <= 0) {
            System.out.println("[Serveur] Versement refuse (compte inconnu ou montant invalide).");
            return;
        }
        c.solde += mt;
        System.out.println("[Serveur] Versement de " + mt + " sur le compte " + code);
    }
    @Override
    public synchronized void retirer(float mt, int code) {
        Compte c = comptes.get(code);
        if (c == null || mt <= 0) {
            System.out.println("[Serveur] Retrait refuse (compte inconnu ou montant invalide).");
            return;
        }
        if (c.solde < mt) {
            System.out.println("[Serveur] Retrait refuse : solde insuffisant.");
            return;
        }
        c.solde -= mt;
        System.out.println("[Serveur] Retrait de " + mt + " sur le compte " + code);
    }
    @Override
    public synchronized Compte getCompte(int code) {
        Compte c = comptes.get(code);
        if (c == null) {
            // Une struct IDL ne peut pas etre null : on renvoie un compte "vide" (code -1)
            return new Compte(-1, 0f);
        }
        return new Compte(c.code, c.solde);
    }
    @Override
    public synchronized Compte[] getComptes() {
        Compte[] tab = new Compte[comptes.size()];
        int i = 0;
        for (Compte c : comptes.values()) {
            tab[i++] = new Compte(c.code, c.solde);
        }
        return tab;
    }
    @Override
    public double conversion(float mt) {
        return mt * TAUX_EUR_DT;
    }
}
