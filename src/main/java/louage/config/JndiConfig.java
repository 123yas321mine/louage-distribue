package louage.config;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Accès JNDI : lit jndi.properties (dans le classpath) et fournit
 * la ConnectionFactory et les destinations (Topic / Queue).
 */
public class JndiConfig {

    private static InitialContext ctx;

    static {
        try {
            ctx = new InitialContext();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static ConnectionFactory connectionFactory() throws NamingException {
        return (ConnectionFactory) ctx.lookup("ConnectionFactory");
    }

    /** nom = "LouageTopic" ou "ConfirmationQueue" */
    public static Destination destination(String nom) throws NamingException {
        return (Destination) ctx.lookup(nom);
    }
}
