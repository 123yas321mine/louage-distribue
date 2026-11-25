package louage.rmi;



import louage.jms.NotificationProducer;
import louage.model.Reservation;
import louage.model.Trajet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReservationImpl extends UnicastRemoteObject implements IReservation {

    private final Map<Integer, Trajet> trajets = new LinkedHashMap<>();
    private final Map<Integer, List<Reservation>> reservations = new LinkedHashMap<>();
    private final NotificationProducer producer = new NotificationProducer();
    private int compteur = 0;

    public ReservationImpl() throws RemoteException {
        super();
        ajouterTrajet(new Trajet(1, "Sfax", "Tunis", 8));
        ajouterTrajet(new Trajet(2, "Sfax", "Sousse", 8));
        ajouterTrajet(new Trajet(3, "Djerba", "Sfax", 4));
    }

    private void ajouterTrajet(Trajet t) {
        trajets.put(t.getId(), t);
        reservations.put(t.getId(), new ArrayList<>());
    }

    private int libres(Trajet t) {
        return t.getPlacesTotales() - reservations.get(t.getId()).size();
    }

    @Override
    public synchronized List<Trajet> rechercherTrajets() throws RemoteException {
        return new ArrayList<>(trajets.values());
    }

    @Override
    public synchronized int placesLibres(int trajetId) throws RemoteException {
        Trajet t = trajets.get(trajetId);
        if (t == null) throw new RemoteException("Trajet inconnu : " + trajetId);
        return libres(t);
    }

    @Override
    public synchronized int reserverPlace(int trajetId, String passager) throws RemoteException {
        Trajet t = trajets.get(trajetId);
        if (t == null) throw new RemoteException("Trajet inconnu : " + trajetId);

        List<Reservation> liste = reservations.get(trajetId);
        if (liste.size() >= t.getPlacesTotales()) return -1; // complet

        Reservation r = new Reservation(++compteur, passager, trajetId);
        liste.add(r);

        producer.confirmer(passager, "Réservation n°" + r.getId() + " confirmée ("
                + t.getDepart() + " -> " + t.getArrivee() + ")");
        producer.publierTopic("Trajet " + trajetId + " : " + libres(t) + " place(s) libre(s)");

        // Fonction innovante : louage complet -> départ
        if (liste.size() == t.getPlacesTotales()) {
            producer.publierTopic("🚐 Louage " + t.getDepart() + " → " + t.getArrivee()
                    + " COMPLET : départ imminent !");
        }
        return r.getId();
    }

    @Override
    public synchronized boolean annulerReservation(int reservationId) throws RemoteException {
        for (Map.Entry<Integer, List<Reservation>> e : reservations.entrySet()) {
            Iterator<Reservation> it = e.getValue().iterator();
            while (it.hasNext()) {
                Reservation r = it.next();
                if (r.getId() == reservationId) {
                    it.remove();
                    Trajet t = trajets.get(r.getTrajetId());
                    producer.confirmer(r.getPassager(), "Réservation n°" + reservationId + " annulée");
                    producer.publierTopic("Trajet " + t.getId() + " : " + libres(t) + " place(s) libre(s)");
                    return true;
                }
            }
        }
        return false;
    }
}