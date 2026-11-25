package louage.jms;

import louage.config.JndiConfig;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/** Côté serveur : publie sur le Topic (tous) et la Queue (un passager). */
public class NotificationProducer {

    private Session session;
    private MessageProducer topicProducer;
    private MessageProducer queueProducer;

    public NotificationProducer() {
        try {
            ConnectionFactory cf = JndiConfig.connectionFactory();
            Destination topic = JndiConfig.destination("LouageTopic");
            Destination queue = JndiConfig.destination("ConfirmationQueue");

            Connection connection = cf.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            topicProducer = session.createProducer(topic);
            queueProducer = session.createProducer(queue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Diffusion à tous les clients abonnés. */
    public void publierTopic(String texte) {
        try {
            topicProducer.send(session.createTextMessage(texte));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /** Message destiné à un passager précis (propriété "passager"). */
    public void confirmer(String passager, String texte) {
        try {
            TextMessage m = session.createTextMessage(texte);
            m.setStringProperty("passager", passager);
            queueProducer.send(m);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}