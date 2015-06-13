
public class ModCategory {
	
	private String name = "";
	private String url = "";
	
	public ModCategory(String theName, String theUrl){
		
		name = theName;
		url = theUrl;
		
	}
	
	public String getName(){
		return name;
	}
	
	public String getURL(){
		return url;
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public void setURL(String newURL){
		url = newURL;
	}
	
	public String toString(){
		
		return "NAME OF CATEGORY: " + name + " || URL: " + url;
		
	}

}
