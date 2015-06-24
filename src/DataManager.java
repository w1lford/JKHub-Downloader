import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DataManager {
	
	
	//Note: Must be used on a category page.
	
	public File downloadMod(Mod modToDownload, String filePath) throws MalformedURLException, FileNotFoundException,  IOException {
		
		String downloadLink = "";
		String redirectedURL = "";
		
		Document doc = Jsoup.parse(getHTML(modToDownload.getURL()));
		Elements ipsNodeGroup = doc.select("h1[class=ipsType_pagetitle]");
		Element downloadButton = ipsNodeGroup.first();
		
		downloadLink = downloadButton.child(0).attr("href");
		
		//end JSoup stuff.
		
		URLConnection con = new URL(downloadLink ).openConnection();
		
		con.addRequestProperty("User-Agent",
                "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924"
                        + "Epiphany/1.4.4 (Ubuntu)");
		
		con.connect();
		
		InputStream is = con.getInputStream();
		redirectedURL = con.getURL().toString();
		
		//OK, now we have the redirected URL, so now I need to download the file.
		//Convert url to HTTPS (http by default)
		redirectedURL = redirectedURL.replace(redirectedURL.substring(0, 4), "https");
		
		URL website = new URL(redirectedURL);
		
		//Get the file name from the URL.
		String[] strSplitter = redirectedURL.split("/");
		String fileName = strSplitter[strSplitter.length-1];
		
		//....and download the file.
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(filePath + "/" + fileName);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		
		is.close();
		fos.close();
		
		File downloadedFile = new File(filePath + "/" + fileName);
		
		return downloadedFile;
		
	}
	
	 public void decompress(String zipFile, String outputFolder){
		 
	     byte[] buffer = new byte[1024];
	 
	     try{
	 
	    	//create output directory is not exists
	    	File folder = new File(zipFile + "/beetus");
	    	if(!folder.exists()){
	    		folder.mkdir();
	    	}
	 
	    	//get the zip file content
	    	ZipInputStream zis = 
	    		new ZipInputStream(new FileInputStream(zipFile));
	    	//get the zipped file list entry
	    	ZipEntry ze = zis.getNextEntry();
	 
	    	while(ze!=null){
	 
	    	   String fileName = ze.getName();
	    	   
	    	   if ( fileName.contains(".pk3") || fileName.contains("readme.txt")) {
		           File newFile = new File(outputFolder + File.separator + fileName);
		 
		           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
		 
		            //create all non exists folders
		            //else you will hit FileNotFoundException for compressed folder
		            new File(newFile.getParent()).mkdirs();
		 
		            FileOutputStream fos = new FileOutputStream(newFile);             
		 
		            int len;
		            while ((len = zis.read(buffer)) > 0) {
		       		fos.write(buffer, 0, len);
		            }
		 
		            fos.close();
	    	   }
	    	   
	           ze = zis.getNextEntry();
	    	}
	 
	        zis.closeEntry();
	    	zis.close();
	 
	    	System.out.println("Done");
	 
	    }catch(IOException ex){
	       ex.printStackTrace(); 
	    }
	   }    
	
	public int getNumberOfPages(String htmlToParse){
		
		int numberOfPages = 0;
		
		Document doc = Jsoup.parse(htmlToParse);
		
		//Get number of pages of category 
		Element pageNumber = doc.select("a[href=#]").first();
		
		//If there's more than 1 page then get the number of pages.
		if(!pageNumber.text().matches("Change Theme")){
			
			numberOfPages = Integer.parseInt(pageNumber.text().replaceAll("[^0-9]", "").substring(1));
			
		} else {
			//Otherwise, number of pages is 1
			numberOfPages = 1;
		}
		
		return numberOfPages;
	}
	
	public ArrayList<Mod> getListOfMods(String htmlToParse){
		
		ArrayList listOfMods = new ArrayList<Mod>();
		
		String modName = "";
		String modURL = "";
		String modImageURL = "";
		String modVersion = "";
		
		Document doc = Jsoup.parse(htmlToParse);
		
		//Now get the mod info
		Element modTable = doc.select("div[id=idm_category]").first();
		Elements modList = modTable.select("a[href][title]");
		
		for(int i = 0; i < modList.size(); i++){
			//If tag has a child
			if(modList.get(i).children().size() > 0){
				
				//if the child has an image file.
			
				if(modList.get(i).children().get(0).hasAttr("src")){
					//System.out.println(modList.get(i).children().attr("src"));
					//modVersion = modList.get(i).select("span[class]").text();
					
					modURL = modList.get(i).attr("href");
					modImageURL = modList.get(i).children().attr("src");
					modName = modList.get(i).attr("title");
					
					Mod theMod = new Mod(modName,modURL,modImageURL,modVersion);
					listOfMods.add(theMod);
				}
	
				//System.out.printf(modList.get(i).select("span[class]").text());
				
			}
			
		}
		
		return listOfMods;
		
	}
	
	

	public ArrayList<ModCategory> getCategories(String htmlToParse){
		
		ArrayList categoryList = new ArrayList<ModCategory>();
		
		Document doc = Jsoup.parse(htmlToParse);
		Element categoryNode = doc.select("ul[id=idm_categories]").first();
		Elements categories = categoryNode.select("a[href]");
		
		//Create an arraylist of categories.
		
		for(int i = 0; i < categories.size(); i++){
			
			ModCategory mc = new ModCategory(categories.get(i).text(),categories.get(i).attr("href"));
			
			categoryList.add(mc);
		}
	
		return categoryList;
	}
	
	
	public String getHTML(String url){
		
		String content = null;
		URLConnection connection = null;
		try {
		  connection =  new URL(url).openConnection();
		  
		  connection.addRequestProperty("User-Agent",
                  "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924"
                   + "Epiphany/1.4.4 (Ubuntu)");
		  
		  Scanner scanner = new Scanner(connection.getInputStream());
		  scanner.useDelimiter("\\Z");
		  content = scanner.next();
		}catch ( Exception ex ) {
		    ex.printStackTrace();
		}
		
		return content;
		
	}
	
	
}
