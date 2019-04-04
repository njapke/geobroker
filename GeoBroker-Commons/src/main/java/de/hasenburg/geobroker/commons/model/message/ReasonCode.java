package de.hasenburg.geobroker.commons.model.message;

@SuppressWarnings("SpellCheckingInspection")
public enum ReasonCode {

	NormalDisconnection,
	ProtocolError,
	NotConnected,
	GrantedQoS0,
	Success,
	NoMatchingSubscribers,

	// New Reason Codes
	LocationUpdated,
	WrongBroker
}
