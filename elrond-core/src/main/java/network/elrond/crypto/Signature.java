package network.elrond.crypto;

import java.io.Serializable;
import java.math.BigInteger;

public class Signature implements Serializable {
	private final byte[] signature;
	private final byte[] commitment;
	private final byte[] challenge;

	public Signature() {
		this.signature = new byte[0];
		this.commitment = new byte[0];
		this.challenge = new byte[0];
	}

	private Signature(byte[] signature, byte[] commitment, byte[] challenge) {
		this.signature = signature.clone();
		this.commitment = commitment.clone();
		this.challenge = challenge.clone();
	}
	
	public static SignatureBuilder builder() {
		return new SignatureBuilder();
	}

	public byte[] getSignature() {
		byte[] result = signature;

		if (0 != signature.length) {
			result = signature.clone();
		}

		return result;
	}

	public byte[] getCommitment() {
		byte[] result = commitment;

		if (0 != commitment.length) {
			result = commitment.clone();
		}
		return result;
	}

	public byte[] getChallenge() {
		byte[] result = challenge;

		if (0 != challenge.length) {
			result = challenge.clone();
		}

		return result;
	}

	public static class SignatureBuilder {
		private byte[] signature = null;
		private byte[] commitment = null;
		private byte[] challenge = null;

		public SignatureBuilder setSignature(byte[] signature) {

			if (0 == signature.length || (new BigInteger(signature)).equals(BigInteger.ZERO)) {
				this.signature = new byte[0];
			}

			this.signature = signature.clone();

			return this;
		}

		public SignatureBuilder setCommitment(byte[] commitment) {

			if (0 == commitment.length || (new BigInteger(commitment)).equals(BigInteger.ZERO)) {
				this.commitment = new byte[0];
			}

			this.commitment = commitment.clone();

			return this;
		}

		public SignatureBuilder setChallenge(byte[] challenge) {
			if (0 == challenge.length || (new BigInteger(challenge)).equals(BigInteger.ZERO)) {
				this.challenge = new byte[0];
			}

			this.challenge = challenge.clone();

			return this;
		}
		
		public Signature build() {
			if (signature == null) {
				signature = new byte[0];
			}
			if (commitment == null) {
				commitment = new byte[0];
			}
			if (challenge == null) {
				challenge = new byte[0];
			}
			return new Signature(signature, commitment, challenge);
		}

	}
}
