package louage.rmi;

import louage.model.Trajet;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IReservation extends Remote {

    List<Trajet> rechercherTrajets() throws RemoteException;

    int placesLibres(int trajetId) throws RemoteException;

    /** Retourne l'id de la réservation, ou -1 si le louage est complet. */
    int reserverPlace(int trajetId, String passager) throws RemoteException;

    /** Retourne true si la réservation existait et a été annulée. */
    boolean annulerReservation(int reservationId) throws RemoteException;
}
