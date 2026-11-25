package louage.model;

import java.io.Serializable;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String passager;
    private final int trajetId;

    public Reservation(int id, String passager, int trajetId) {
        this.id = id;
        this.passager = passager;
        this.trajetId = trajetId;
    }

    public int getId() { return id; }
    public String getPassager() { return passager; }
    public int getTrajetId() { return trajetId; }

    @Override
    public String toString() {
        return "Reservation n°" + id + " (" + passager + ", trajet " + trajetId + ")";
    }
}
