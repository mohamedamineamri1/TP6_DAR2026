package corbaClient;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import Banque.Compte;
import Banque.IBanqueRemote;
import Banque.IBanqueRemoteHelper;

public class BanqueClient {

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);

            // a. Proprietes de l'annuaire JNDI
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
            env.put(Context.PROVIDER_URL, "iiop://localhost:900");
            env.put("java.naming.corba.orb", orb);
            Context ctx = new InitialContext(env);

            // b. Reference de l'objet distant
            org.omg.CORBA.Object ref = (org.omg.CORBA.Object) ctx.lookup("BanqueService");

            // c. narrow -> creation du Stub
            IBanqueRemote stub = IBanqueRemoteHelper.narrow(ref);

            // d. Test de chaque methode
            System.out.println("=== Creation de comptes ===");
            stub.creerCompte(new Compte(1, 1000f));
            stub.creerCompte(new Compte(2, 500f));
            stub.creerCompte(new Compte(3, 250f));

            System.out.println("=== Versement de 300 sur le compte 1 ===");
            stub.verser(300f, 1);
            afficher(stub.getCompte(1));

            System.out.println("=== Retrait de 200 sur le compte 2 ===");
            stub.retirer(200f, 2);
            afficher(stub.getCompte(2));

            System.out.println("=== Retrait refuse (solde insuffisant) sur le compte 3 ===");
            stub.retirer(1000f, 3);
            afficher(stub.getCompte(3));

            System.out.println("=== Compte inexistant ===");
            Compte inconnu = stub.getCompte(99);
            if (inconnu.code == -1) System.out.println("Compte 99 introuvable.");

            System.out.println("=== Liste de tous les comptes ===");
            for (Compte c : stub.getComptes()) afficher(c);

            System.out.println("=== Conversion ===");
            float eur = 100f;
            System.out.println(eur + " EUR = " + stub.conversion(eur) + " DT");

        } catch (Exception e) {
            System.err.println("Erreur client : " + e);
            e.printStackTrace();
        }
    }

    private static void afficher(Compte c) {
        System.out.println("Compte [code=" + c.code + ", solde=" + c.solde + "]");
    }
}