
public class Mod {
	
	private String name = "";
	private String url = "";
	private String imageURL = "";
	private String version = "";
	
	public Mod(String theName, String theUrl, String theImageUrl, String theVersion){
		
		name = theName;
		url = theUrl;
		imageURL = theImageUrl;
		version = theVersion;
		
	}
	
	
	public String getImageURL(){
		return imageURL;
	}
	
	public String getURL(){
		return url;
	}
	
	public String getName(){
		return name;
	}
	
	public String toString(){
		
		return "Name: " + name + " || URL: " + url + " || Image URL: " + imageURL + " || Version: " + version;
	}

}
