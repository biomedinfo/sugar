package org.csml.tommo.sugar.sequence;

public class SubtileCoordinates extends TileCoordinates {

	private static final String JSON_ATTR_TILE = "subtile";

	protected Integer x;
	protected Integer y;
	
	public SubtileCoordinates(String flowCell, Integer lane, Integer tile, Integer x, Integer y){
		super(flowCell, lane, tile);
		this.x = x;
		this.y = y;
	}
	
	public SubtileCoordinates(LaneCoordinates laneCoordinates, Integer tile, Integer x, Integer y){
		super(laneCoordinates, tile);
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubtileCoordinates other = (SubtileCoordinates) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SubtileCoordinates [flowCell=" + flowCellID + ", lane=" + lane
				+ ", tile=" + tile + ", x=" + x + ", y=" + y + "]";
	}

	public TileCoordinates getTileCoordinates(){
		return new TileCoordinates(getFlowCell(), getLane(), getTile());
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}
}
