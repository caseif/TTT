package net.amigocraft.TTT;
// the class

public class Body {
	private String name;
	private Role role;
	private FixedLocation l;
	private long time;

	public Body(String name, Role role, FixedLocation l, long time){
		this.name = name;
		this.role = role;
		this.l = l;
		this.time = time;
	}
	
	public String getName(){
		return name;
	}
	
	public Role getRole(){
		return role;
	}

	public FixedLocation getLocation(){
		return l;
	}
	
	public long getTime(){
		return time;
	}
	
	public boolean equals(Object b){
		return name.equals(((Body)b).getName()) && role == ((Body)b).getRole() && l.equals(((Body)b).getLocation());
	}
	
	public int hashCode(){
		return 41 * (41 + name.hashCode() + role.hashCode() + l.hashCode());
	}
}