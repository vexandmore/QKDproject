package QKDproject;

/**
 * Encapsulates the current encryption parameters between two users.
 * @author Marc
 */
public class EncryptionParameters {
	public enum EncryptionType {
		QKD,
		QKA
	}
	
	public final EncryptionType type;
	public final boolean eavesdropped;
	public final double security;

	public EncryptionParameters(EncryptionType type, boolean eavesdropped, double security) {
		this.type = type;
		this.eavesdropped = eavesdropped;
		this.security = security;
	}
	
	public Protocol[] makeProtocols() {
		Protocol[] out = new Protocol[2];
		if (this.type == EncryptionType.QKD) {
			QKDChannel channel = new QKDChannel(eavesdropped);
			QKDBob2 bob = new QKDBob2();
			QKDAlice2 alice = new QKDAlice2((int)security);
			channel.setup(alice, bob);
			out[0] = alice;
			out[1] = bob;
			return out;
		} else {
			throw new IllegalStateException("QKA not implemented");
		}
	}
}