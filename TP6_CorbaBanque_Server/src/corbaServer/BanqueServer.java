package corbaServer;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import service.BanqueImpl;

public class BanqueServer {

    public static void main(String[] args) {
        try {
            // a. Initialiser l'ORB
            ORB orb = ORB.init(args, null);

            // b. RootPOA + activation du POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // c. Configurer l'annuaire JNDI
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
            env.put(Context.PROVIDER_URL, "iiop://localhost:900");
            env.put("java.naming.corba.orb", orb);
            Context ctx = new InitialContext(env);

            // d. Creer le servant
            BanqueImpl servant = new BanqueImpl();

            // e. Enregistrer sa reference
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);
            ctx.rebind("BanqueService", ref);
            System.out.println("Serveur Banque pret : objet 'BanqueService' enregistre.");

            // f. Attendre les requetes des clients
            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}