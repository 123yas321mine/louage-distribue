package louage.model;

import java.io.Serializable;

public class Trajet implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String depart;
    private final String arrivee;
    private final int placesTotales;

    public Trajet(int id, String depart, String arrivee, int placesTotales) {
        this.id = id;
        this.depart = depart;
        this.arrivee = arrivee;
        this.placesTotales = placesTotales;
    }

    public int getId() { return id; }
    public String getDepart() { return depart; }
    public String getArrivee() { return arrivee; }
    public int getPlacesTotales() { return placesTotales; }

    @Override
    public String toString() {
        return "[" + id + "] " + depart + " -> " + arrivee + " (" + placesTotales + " places)";
    }
}
