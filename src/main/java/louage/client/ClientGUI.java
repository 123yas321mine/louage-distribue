package louage.client;

import louage.jms.NotificationListener;
import louage.model.Trajet;
import louage.rmi.IReservation;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.Naming;

/** Interface graphique Swing du client (remplace le menu console). */
public class ClientGUI extends JFrame {

    private final IReservation service;
    private final String nom;
    private NotificationListener listener;

    private final DefaultTableModel modele = new DefaultTableModel(
            new String[]{"Id", "Départ", "Arrivée", "Places totales", "Places libres"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(modele);
    private final DefaultListModel<Integer> mesReservations = new DefaultListModel<>();
    private final JList<Integer> listeReservations = new JList<>(mesReservations);
    private final JTextArea notifications = new JTextArea();

    public ClientGUI(IReservation service, String nom) {
        super("Louage - passager : " + nom);
        this.service = service;
        this.nom = nom;

        construireInterface();
        rafraichir();
        demarrerNotifications();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (listener != null) listener.fermer();
            }
        });
        setSize(850, 550);
        setLocationRelativeTo(null);
    }

    private void construireInterface() {
        setLayout(new BorderLayout(8, 8));

        // --- Haut : tableau des trajets ---
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        JPanel haut = new JPanel(new BorderLayout());
        haut.setBorder(BorderFactory.createTitledBorder("Trajets disponibles"));
        haut.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel boutonsTrajets = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnReserver = new JButton("Réserver une place");
        JButton btnActualiser = new JButton("Actualiser");
        boutonsTrajets.add(btnReserver);
        boutonsTrajets.add(btnActualiser);
        haut.add(boutonsTrajets, BorderLayout.SOUTH);

        // --- Bas gauche : mes réservations ---
        JPanel panneauRes = new JPanel(new BorderLayout());
        panneauRes.setBorder(BorderFactory.createTitledBorder("Mes réservations"));
        panneauRes.add(new JScrollPane(listeReservations), BorderLayout.CENTER);
        JButton btnAnnuler = new JButton("Annuler la réservation");
        panneauRes.add(btnAnnuler, BorderLayout.SOUTH);
        panneauRes.setPreferredSize(new Dimension(260, 200));

        // --- Bas droite : notifications JMS ---
        notifications.setEditable(false);
        JPanel panneauNotif = new JPanel(new BorderLayout());
        panneauNotif.setBorder(BorderFactory.createTitledBorder("Notifications (JMS)"));
        panneauNotif.add(new JScrollPane(notifications), BorderLayout.CENTER);

        JSplitPane bas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauRes, panneauNotif);
        bas.setDividerLocation(260);

        add(new JLabel("  Connecté en tant que : " + nom), BorderLayout.NORTH);
        JSplitPane principal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
        principal.setDividerLocation(250);
        add(principal, BorderLayout.CENTER);

        // --- Actions ---
        btnActualiser.addActionListener(e -> rafraichir());
        btnReserver.addActionListener(e -> reserver());
        btnAnnuler.addActionListener(e -> annuler());
    }

    private void demarrerNotifications() {
        try {
            listener = new NotificationListener(nom, texte -> SwingUtilities.invokeLater(() -> {
                notifications.append(texte + "\n");
                notifications.setCaretPosition(notifications.getDocument().getLength());
                rafraichir();   // les places libres changent après chaque message
            }));
            listener.demarrer();
        } catch (Exception e) {
            erreur("Connexion JMS impossible (ActiveMQ est-il lancé ?)", e);
        }
    }

    private void rafraichir() {
        try {
            int selection = table.getSelectedRow();
            modele.setRowCount(0);
            for (Trajet t : service.rechercherTrajets()) {
                modele.addRow(new Object[]{
                        t.getId(), t.getDepart(), t.getArrivee(),
                        t.getPlacesTotales(), service.placesLibres(t.getId())});
            }
            if (selection >= 0 && selection < modele.getRowCount()) {
                table.setRowSelectionInterval(selection, selection);
            }
        } catch (Exception e) {
            erreur("Serveur RMI indisponible", e);
        }
    }

    private void reserver() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez d'abord un trajet.");
            return;
        }
        int trajetId = (int) modele.getValueAt(ligne, 0);
        try {
            int idRes = service.reserverPlace(trajetId, nom);
            if (idRes == -1) {
                JOptionPane.showMessageDialog(this, "Louage complet !", "Réservation", JOptionPane.WARNING_MESSAGE);
            } else {
                mesReservations.addElement(idRes);
            }
            rafraichir();
        } catch (Exception e) {
            erreur("Réservation impossible", e);
        }
    }

    private void annuler() {
        Integer idRes = listeReservations.getSelectedValue();
        if (idRes == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une réservation à annuler.");
            return;
        }
        try {
            if (service.annulerReservation(idRes)) {
                mesReservations.removeElement(idRes);
            }
            rafraichir();
        } catch (Exception e) {
            erreur("Annulation impossible", e);
        }
    }

    private void erreur(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + "\n" + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        String nom = JOptionPane.showInputDialog(null, "Votre nom :", "Connexion", JOptionPane.QUESTION_MESSAGE);
        if (nom == null || nom.trim().isEmpty()) return;
        try {
            IReservation service = (IReservation) Naming.lookup("rmi://localhost:1099/Reservation");
            SwingUtilities.invokeLater(() -> new ClientGUI(service, nom.trim()).setVisible(true));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Serveur RMI introuvable. Lancez ServeurRMI d'abord.\n" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}