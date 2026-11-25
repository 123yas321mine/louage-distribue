package louage.jms;

import org.apache.activemq.broker.BrokerService;

/** Broker ActiveMQ embarqué (port 61616). */
public class EmbededActiveMQ {

    private static BrokerService broker;

    public static void demarrer() throws Exception {
        if (broker != null) return;
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector("tcp://0.0.0.0:61616");
        broker.start();
        System.out.println("Broker ActiveMQ démarré sur tcp://0.0.0.0:61616");
    }

    // Permet aussi de le lancer seul
    public static void main(String[] args) throws Exception {
        demarrer();
        broker.waitUntilStopped();
    }
}
