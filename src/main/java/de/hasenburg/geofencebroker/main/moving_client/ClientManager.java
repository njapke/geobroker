package de.hasenburg.geofencebroker.main.moving_client;

import de.hasenburg.geofencebroker.communication.ZMQProcessManager;
import de.hasenburg.geofencebroker.main.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientManager {

	private static final Logger logger = LogManager.getLogger();

	private Date startupTime;
	private String configurationName;
	private String managerName;
	private GeolifeDatasetHelper gdh;
	private Map<Integer, GeolifeClient> geolifeClients = new HashMap<>(); //index -> client
	private ZMQProcessManager processManager;

	public ClientManager(String managerName, Date startupTime, String configurationName) {
		this.managerName = managerName;
		this.startupTime = startupTime;
		this.configurationName = configurationName;
	}

	public void start() throws IOException {
		// create configuration
		Configuration c = Configuration.readConfigurationFromS3(configurationName, managerName);
		gdh = new GeolifeDatasetHelper();
		gdh.prepare();

		// create ZMQProcessManager
		processManager = new ZMQProcessManager();

		// download required files from S3
		Map<Integer, Route> routes = downloadRequiredFiles(c.getIndex(), c.getCount());

		// create necessary clients
		for (int i = c.getIndex(); i <= c.getCount(); i++) {
			geolifeClients.put(i, new GeolifeClient(c, i, routes.get(i), processManager));
		}

		// calculate how much time to wait for startup
		Date now = new Date();
		//in milliseconds
		long diff = (startupTime.getTime() - now.getTime());

		if (diff < 0) {
			logger.error("Startup time seems to be {} seconds in the past", diff / -1000);
			System.exit(1);
		}
		try {
			logger.info("Waiting {} seconds for startup", diff / 1000);
			Thread.sleep(diff);
		} catch (InterruptedException e) {
			logger.fatal("Interrupted while waiting for startup", e);
			System.exit(1);
		}

		logger.info("Starting up!");
		for (int i = c.getIndex(); i <= c.getCount(); i++) {
			geolifeClients.get(i).start();
			Utility.sleepNoLog(c.getOffset() * 1000, 0);
		}

		// calculate how much time to wait for shutdown
		Date endTime = new Date(startupTime.getTime() + c.getRuntime() * 1000 * 60);
		now = new Date();
		long timeUntilEnd = (endTime.getTime() - now.getTime());

		try {
			logger.info("Waiting {} seconds until shutdown", timeUntilEnd / 1000);
			if (timeUntilEnd > 0) {
				Thread.sleep(timeUntilEnd);
			}
		} catch (InterruptedException e) {
			logger.fatal("Interrupted while waiting for shutdown", e);
			System.exit(1);
		}

		for (int i = c.getIndex(); i <= c.getCount(); i++) {
			geolifeClients.get(i).stop();
		}
		Utility.sleepNoLog(5000, 0);

		processManager.tearDown(3000);

		logger.info("Experiments stopped");
		System.exit(0);
	}

	private Map<Integer, Route> downloadRequiredFiles(int index, int count) {
		Map<Integer, Route> routes = new HashMap<>();
		for (int i = index; i <= count; i++) {
			logger.trace("Creating route for file at index {}", i);
			List<String> lines = gdh.getLinesOfRouteFileAtIndex(i);
			routes.put(i, Route.createRoute(lines));
		}
		if (routes.size() == 0) {
			logger.fatal("No routes were created");
			System.exit(1);
		}

		logger.info("Successfully created {} routes", routes.size());
		return routes;
	}

	/**
	 *
	 * Requires two args:
	 * args[0] = startup time in format yyyy-MM-dd_HH:mm
	 * args[1] = configuration name
	 *
	 * @param args - the args
	 */
	public static void main (String[] args) throws IOException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		Date startupTime = null;
		try {
			startupTime = format.parse(args[0]);
		} catch (ParseException e) {
			logger.fatal("Invalid startup time");
			System.exit(1);
		}
		String configurationName = args[1];

		// there should be a file in ./name whose name is the managerName
		File f = new File("./name");
		String managerName = Objects.requireNonNull(f.listFiles())[0].getName();

		logger.info("Starting up {} at {} based on configuration {}", managerName, format.format(startupTime), configurationName);
		ClientManager clientManager = new ClientManager(managerName, startupTime, configurationName);
		clientManager.start();
	}

}
