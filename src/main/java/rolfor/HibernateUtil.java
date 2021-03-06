package rolfor;


import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import rolfor.model.container.ContainerImpl;
import rolfor.model.message.MessageImpl;
import rolfor.model.news.NewsImpl;
import rolfor.model.token.TokenImpl;
import rolfor.model.user.UserImpl;


public final class HibernateUtil {
	private static final SessionFactory sessionFactory = buildSessionFactory();
	
	private HibernateUtil() {
	}
	
	private static SessionFactory buildSessionFactory() {
		try {
			Configuration configuration = new Configuration();
			configuration.configure();
			
			configuration.setProperty("hibernate.connection.url", System.getenv("DB_URL"));
			configuration.setProperty("hibernate.connection.username", System.getenv("DB_USER"));
			configuration.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
			configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
			configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");
			configuration.setProperty("hibernate.show_sql", "true");
			configuration.setProperty("hibernate.format_sql", "true");
			configuration.setProperty("hibernate.connection.pool_size", "4");
			configuration.setProperty("hibernate.current_session_context_class", "thread");
			
			configuration.addAnnotatedClass(ContainerImpl.class);
			configuration.addAnnotatedClass(MessageImpl.class);
			configuration.addAnnotatedClass(NewsImpl.class);
			configuration.addAnnotatedClass(UserImpl.class);
			configuration.addAnnotatedClass(TokenImpl.class);
			
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(configuration.getProperties()).build();
			return configuration.buildSessionFactory(serviceRegistry);
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static void shutdown() {
		getSessionFactory().close();
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
