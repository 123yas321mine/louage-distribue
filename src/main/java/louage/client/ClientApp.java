package louage.client;

import louage.jms.NotificationListener;
import louage.model.Trajet;
import louage.rmi.IReservation;
import java.rmi.Naming;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Votre nom : ");
            String nom = sc.nextLine().trim();

            // RMI
            IReservation service = (IReservation) Naming.lookup("rmi://localhost:1099/Reservation");

            // JMS (Topic + Queue via JNDI)
            NotificationListener listener = new NotificationListener(nom);listener.demarrer();

            int choix = -1;
            while (choix != 0) {
                System.out.println("\n--- MENU ---");
                System.out.println("1. Liste des trajets");
                System.out.println("2. Places libres");
                System.out.println("3. Réserver une place");
                System.out.println("4. Annuler une réservation");
                System.out.println("0. Quitter");
                System.out.print("Choix : ");

                try {
                    choix = Integer.parseInt(sc.nextLine().trim());
                    switch (choix) {
                        case 1:
                            for (Trajet t : service.rechercherTrajets()) System.out.println(t);
                            break;
                        case 2:
                            System.out.print("Id du trajet : ");
                            int id = Integer.parseInt(sc.nextLine().trim());
                            System.out.println("Places libres : " + service.placesLibres(id));
                            break;
                        case 3:
                            System.out.print("Id du trajet : ");
                            int tid = Integer.parseInt(sc.nextLine().trim());
                            int res = service.reserverPlace(tid, nom);
                            System.out.println(res == -1 ? "Louage complet !" : "Réservation n°" + res);
                            break;
                        case 4:
                            System.out.print("N° de réservation : ");
                            int rid = Integer.parseInt(sc.nextLine().trim());
                            System.out.println(service.annulerReservation(rid) ? "Annulée." : "Réservation introuvable.");
                            break;
                        case 0:
                            break;
                        default:
                            System.out.println("Choix invalide.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrez un nombre.");
                }
            }
            listener.fermer();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
