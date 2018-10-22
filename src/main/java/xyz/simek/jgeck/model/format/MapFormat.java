package xyz.simek.jgeck.model.format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFormat {

	private int version; // F1 = 19, F2 = 20
	private String name;
	private int playerPos;
	private int defaultElevation;
	private int playerOrientation;
	private int numLocalVars;
	private int scriptId; // Value of -1 means no map. Text string is found in MSG file scrname.msg at index [id + 101].
	private int elevations = 0;
	/*
	 	If (flag & 0x1) == 0 then ?? unknown.
		If (flag & 0x2) == 0 then the map has an defaultElevation at level 0.
		If (flag & 0x4) == 0 then the map has an defaultElevation at level 1.
		If (flag & 0x8) == 0 then the map has an defaultElevation at level 2.
	 */
	private int darkness; // UNUSED
	private int numGlobalVars;
	private int mapId;
	/*
	 	Fallout 1: Map filename found in map.msg
		Fallout 2: Map details found in data/maps.txt in section [Map id]
	*/
	private int timeSinceEpoch;

	private Map<Integer,List<Short>> tiles = new HashMap<>();
	
	
	public Map<Integer, List<Short>> getTiles() {
		return tiles;
	}

	public void setTiles(Map<Integer, List<Short>> tiles) {
		this.tiles = tiles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPlayerPos() {
		return playerPos;
	}

	public void setPlayerPos(int playerPos) {
		this.playerPos = playerPos;
	}

	public int getDefaultElevation() {
		return defaultElevation;
	}

	public void setDefaultElevation(int defaultElevation) {
		this.defaultElevation = defaultElevation;
	}

	public int getPlayerOrientation() {
		return playerOrientation;
	}

	public void setPlayerOrientation(int playerOrientation) {
		this.playerOrientation = playerOrientation;
	}

	public int getNumLocalVars() {
		return numLocalVars;
	}

	public void setNumLocalVars(int numLocalVars) {
		this.numLocalVars = numLocalVars;
	}

	public int getScriptId() {
		return scriptId;
	}

	public void setScriptId(int scriptId) {
		this.scriptId = scriptId;
	}

	public int getElevations() {
		return elevations;
	}

	public void setElevations(int elevations) {
		this.elevations = elevations;
	}

	public int getNumGlobalVars() {
		return numGlobalVars;
	}

	public void setNumGlobalVars(int numGlobalVars) {
		this.numGlobalVars = numGlobalVars;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getTimeSinceEpoch() {
		return timeSinceEpoch;
	}

	public void setTimeSinceEpoch(int timeSinceEpoch) {
		this.timeSinceEpoch = timeSinceEpoch;
	}

	public void read(ByteBuffer buff) {

		version = buff.getInt();
		if(version == 19) {
			System.err.println("Fallout 1 maps are not supported yet");
			return;
		}
		if(version != 20) {
			System.err.println("Unknown map version: " + version);
			return;
		}
		
		byte[] nameBytes = new byte[16];
		buff.get(nameBytes);
		name = new String(nameBytes);
		System.out.println("Map name: " + name);
		
		playerPos = buff.getInt();
		defaultElevation = buff.getInt();
		playerOrientation = buff.getInt();
		numLocalVars = buff.getInt();
		scriptId = buff.getInt();
		
		int elevationFlags = buff.getInt();
		if((elevationFlags & 2) == 0) elevations++;
		if((elevationFlags & 4) == 0) elevations++;
		if((elevationFlags & 8) == 0) elevations++;
		
		darkness = buff.getInt(); // Unused
		
		numGlobalVars = buff.getInt();
		mapId = buff.getInt();
		timeSinceEpoch = buff.getInt();
		
		buff.position(buff.position() + 4*44); // Unknown
		
		// TODO global vars
		buff.position(buff.position() + 4*numGlobalVars);
		// TODO: local vars
		buff.position(buff.position() + 4*numLocalVars);
				
		// TILES
		System.out.println("Elevations: " + elevations);
		for(int elevation = 0; elevation < elevations; elevation++) {
			tiles.put(elevation, new ArrayList<>());
			
			// 100 * 100 tiles
			for(int i = 0; i < 10000; i++) {
				
				// FIXME: skip roof for now
				buff.getShort();
				Short tile = buff.getShort();
				tiles.get(elevation).add(tile);
				//System.out.println("Tile: " + tile);
			}
		}
	}
}
