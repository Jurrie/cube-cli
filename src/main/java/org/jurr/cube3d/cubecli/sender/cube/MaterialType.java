package org.jurr.cube3d.cubecli.sender.cube;

public enum MaterialType {
	ABS("ABS"), NONE("no cartridge detected"), PLA("PLA"), PLA_ABS("PLA/ABS");

	private final String description;

	MaterialType(final String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}