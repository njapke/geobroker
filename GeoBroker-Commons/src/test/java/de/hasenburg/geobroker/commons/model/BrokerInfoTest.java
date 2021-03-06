package de.hasenburg.geobroker.commons.model;

import de.hasenburg.geobroker.commons.model.BrokerInfo;
import de.hasenburg.geobroker.commons.model.KryoSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
public class BrokerInfoTest {

	private static final Logger logger = LogManager.getLogger();

	@Test
	public void testSerialize() {
		BrokerInfo brokerInfo1 = new BrokerInfo("brokerId", "address", 1000);
		KryoSerializer kryo = new KryoSerializer();
		byte[] arr = kryo.write(brokerInfo1);
		logger.info(arr);
		BrokerInfo brokerInfo2 = kryo.read(arr, BrokerInfo.class);
		assertEquals(brokerInfo1, brokerInfo2);
	}
}
