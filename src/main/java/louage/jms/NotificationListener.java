package louage.jms;

import louage.config.JndiConfig;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.function.Consumer;

/** Côté client : reçoit les messages du Topic et ses confirmations (Queue). */
public class NotificationListener {

    private final String nomPassager;
    private final Consumer<String> sortie;   // où afficher (console ou interface graphique)
    private Connection connection;

    /** Version console */
    public NotificationListener(String nomPassager) {
        this(nomPassager, texte -> System.out.println("\n" + texte));
    }

    /** Version générique : la sortie est fournie par l'appelant (ex. interface Swing) */
    public NotificationListener(String nomPassager, Consumer<String> sortie) {
        this.nomPassager = nomPassager;
        this.sortie = sortie;
    }

    public void demarrer() throws Exception {
        ConnectionFactory cf = JndiConfig.connectionFactory();
        Destination topic = JndiConfig.destination("LouageTopic");
        Destination queue = JndiConfig.destination("ConfirmationQueue");

        connection = cf.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        MessageConsumer cTopic = session.createConsumer(topic);
        // Le sélecteur : ce passager ne reçoit que ses propres confirmations
        MessageConsumer cQueue = session.createConsumer(queue, "passager = '" + nomPassager + "'");

        cTopic.setMessageListener(m -> afficher("[INFO]", m));
        cQueue.setMessageListener(m -> afficher("[CONFIRMATION]", m));

        connection.start();
    }

    private void afficher(String etiquette, Message m) {
        try {
            if (m instanceof TextMessage) {
                sortie.accept(etiquette + " " + ((TextMessage) m).getText());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void fermer() {
        try {
            if (connection != null) connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}