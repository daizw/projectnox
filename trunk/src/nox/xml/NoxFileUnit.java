package nox.xml;

import java.io.Serializable;

@SuppressWarnings("serial")
public class NoxFileUnit implements Serializable{
	private String name = "";
	private byte[] data;

	public NoxFileUnit(String name, byte[] data){
		this.name = name;
		this.data = data;
	}
	public String getName(){
		return name;
	}
	public byte[] getData(){
		return data;
	}
}