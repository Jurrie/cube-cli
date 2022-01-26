package org.jurr.cube3d.cubecli.sender.cube;

public enum CubeType {
	CUBE_1("Cube 1st gen"), CUBE_2("Cube 2nd gen");

	private final String description;

	CubeType(final String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}