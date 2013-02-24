package net.amigocraft.TTT;
// the class

public class Body {
	private String name;
	private int role;
	private FixedLocation l;

	public Body(String name, int role, FixedLocation l){
		this.name = name;
		this.role = role;
		this.l = l;
	}
	
	public String getName(){
		return name;
	}
	
	public int getRole(){
		return role;
	}

	public FixedLocation getLocation(){
		return l;
	}
	
	public boolean equals(Object b){
		return name.equals(((Body)b).getName()) && role == ((Body)b).getRole() && l.equals(((Body)b).getLocation());
	}
	
	public int hashCode(){
		return 41 * (41 + name.hashCode() + role + l.hashCode());
	}
}