package de.hasenburg.geobroker.communication;

@SuppressWarnings("SpellCheckingInspection")
public enum ControlPacketType {

	Reserved,
	CONNECT,
	CONNACK,
	PUBLISH,
	PUBACK,
	PUBREC,
	// PUBREL,
	// PUBCOMP,
	SUBSCRIBE,
	SUBACK,
	UNSUBSCRIBE,
	UNSUBACK,
	PINGREQ,
	PINGRESP,
	DISCONNECT,
	AUTH

}