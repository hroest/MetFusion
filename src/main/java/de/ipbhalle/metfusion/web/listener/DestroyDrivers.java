package de.ipbhalle.metfusion.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;

public class DestroyDrivers implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
        System.out.println("Finishing drivers...");
		while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
             try {
                DriverManager.deregisterDriver(driver);
                System.out.println(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                System.err.println(String.format("Error deregistering driver %s", driver));
            }
        }
        System.out.println("Done...");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}

