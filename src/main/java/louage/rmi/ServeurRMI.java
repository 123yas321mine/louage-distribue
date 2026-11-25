package louage.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServeurRMI {

    public static void main(String[] args) {
        try {
            // 1. Broker JMS embarqué (doit démarrer avant ReservationImpl)
            //  EmbededActiveMQ.demarrer();

            // 2. Registre RMI
            LocateRegistry.createRegistry(1099);

            // 3. Publication de l'objet distant
            Naming.rebind("rmi://localhost:1099/Reservation", new ReservationImpl());
            System.out.println("Serveur RMI démarré...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
