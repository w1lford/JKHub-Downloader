import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIDriver extends JFrame{
	
	static DataManager dm = new DataManager();
	
	public static JPanel topPanel = new JPanel();
	public static JPanel categoryPanel = new JPanel();
	public static JPanel modPanel = new JPanel();
	public static JPanel pagePanel = new JPanel();
	static String html = dm.getHTML("http://jkhub.org/files/");
	public static int selectedPage = 1;
	static ArrayList<ModCategory> listOfCategories = dm.getCategories(html);
	public static ArrayList<Mod> theMods = new ArrayList<Mod>();
	
	public static Mod selectedMod;
	
	public static JButton nextPageButton = new JButton("Next Page");
	public static JButton prevPageButton = new JButton("Prev Page");
	public static JButton browseButton = new JButton("Browse...");
	public static JLabel pageStatus;
	public static JLabel selectedPageDisplay = new JLabel("");
	public static ModCategory selectedCategory;
	public static JTextField basePathBox = new JTextField(30);
	
	public static JLabel statusIndicator = new JLabel("Ready");
	
	final String OS = System.getProperty("os.name");
	final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	final static String HOME_DIR = System.getProperty("user.home");
	
	public static File dataCache = new File(HOME_DIR + "/JKHubDownloaderCache.dat");
	
	public static PrintWriter writer;
	
	public static JButton[] downloadButtons;
	
	public static String basePath = "";
	
	public GUIDriver() throws IOException{
		super("JKHub Downloader");
		
		/*
		if(OS.equals("Mac OS X")){
			
			basePath = HOME_DIR + "/Library/Application Support/Jedi Academy/base";
		}
		*/
		
		if(dataCache.exists()){
			
			System.out.println("Detected cache file");
			
			BufferedReader reader = new BufferedReader(new FileReader(dataCache.getAbsolutePath()));
	       
			String line;
			while ((line = reader.readLine()) != null) {
			    basePathBox.setText(line);
			}
		} else {
			 basePathBox.setText("Browse Jedi Academy base folder...");
		}
			
		basePathBox.setEditable(false);
		topPanel.add(basePathBox);
		topPanel.add(browseButton);
		
		browseButton.addActionListener(new ButtonListener());
		
		this.setLayout(new BorderLayout());
		this.setSize(750,500);
		
		this.add(topPanel,BorderLayout.NORTH);
		this.add(categoryPanel,BorderLayout.WEST);
		this.add(modPanel,BorderLayout.CENTER);
		this.add(pagePanel,BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//this.add(new JLabel("afsdasdf"));
		
		//categoryPanel.setLayout(new GridLayout(30,1));
		this.setVisible(true);
	
		
	}
	
	public static class ButtonListener implements ActionListener{

		public void updateGUI(){
			//Write refreshed list.
		
			theMods = dm.getListOfMods(dm.getHTML(selectedCategory.getURL() + "?sort_order=DESC&sort_key=file_updated&num=20&st=" + (selectedPage-1)*20));
			
			downloadButtons = new JButton[theMods.size()];
			
			for(int j = 0; j < theMods.size();j++){
				
				modPanel.setLayout(new GridLayout(theMods.size(),2));
				modPanel.add(new JLabel(theMods.get(j).getName()));
				
				//JButton downloadButton = new JButton("Download");
				
				downloadButtons[j] = new JButton("Download");
				
				modPanel.add(downloadButtons[j] );
				downloadButtons[j].addActionListener(this);
				
			}
			
			pageStatus = new JLabel("Page " + selectedPage + " of " + dm.getNumberOfPages(dm.getHTML(selectedCategory.getURL())));
			pagePanel.add(selectedPageDisplay);
			pagePanel.add(pageStatus);
			pagePanel.add(prevPageButton);
			pagePanel.add(nextPageButton);
			pagePanel.add(statusIndicator);
			
			if(selectedPage < 2){
				prevPageButton.setEnabled(false);
			} else {
				
				prevPageButton.setEnabled(true);
			}
			
			if(selectedPage == dm.getNumberOfPages(dm.getHTML(selectedCategory.getURL()))){
				nextPageButton.setEnabled(false);
			} else {
				nextPageButton.setEnabled(true);
			}
			
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e){
			// TODO Auto-generated method stub
			
			modPanel.removeAll();
			modPanel.repaint();
			pagePanel.removeAll();
			pagePanel.repaint();
			
			if(e.getSource() == browseButton){
				
				  JFileChooser f = new JFileChooser();
			      f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
			      f.showOpenDialog(null);
			      
			      basePath = f.getSelectedFile().getPath().toString();
			      
			      
			      System.out.println(basePath);
			      
			      basePathBox.setText(basePath);

					try {
						BufferedWriter writer;
						writer = new BufferedWriter(new FileWriter(new File(dataCache.getAbsolutePath()),false));
						
						writer.write(basePath);
						writer.close();
						 
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						
					}
					
			
					
							      
		
			      
		
			      
			      updateGUI();
				
			}
			
			if(e.getActionCommand() == "Download"){
				
				for(int i = 0; i < theMods.size(); i++){
					
					if(e.getSource() == downloadButtons[i]){
	
						try {

							File downloadedFile = dm.downloadMod(theMods.get(i), basePath);
							
							System.out.println("Downloaded: " + theMods.get(i).getName());
							
							dm.decompress(downloadedFile.getAbsolutePath(),downloadedFile.getParent());
							
							System.out.println("Decompressed file.");
							
							statusIndicator.setText("Downloaded '" + theMods.get(i).getName()+"'");
							
							 int reply = JOptionPane.showConfirmDialog(null, "Download complete. Would you like to view the readme file?", "Download complete", JOptionPane.YES_NO_OPTION);
						        if (reply == JOptionPane.YES_OPTION) {
						        
						          System.out.println(downloadedFile.getParent());
						          
						          //Open text file.
						         if (Desktop.isDesktopSupported()) {
						        	 
						        	 File readme = new File(downloadedFile.getParent() + "/readme.txt");
						        	 
						        	 if(readme.exists()){
						        		 Desktop.getDesktop().edit(readme);
						        	 } else {
						        		 
						        		 JOptionPane.showMessageDialog(null, "No readme file found.");
						        		 
						        	 }
						        	 
						         }    
						          
						    }
			
							System.out.println("Deleting zip file...");
							downloadedFile.delete();
							System.out.println("Done.");
							
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					

				}
				
				updateGUI();
				
			}
					
			if(e.getActionCommand() == "Next Page"){	
				
				selectedPage++;
				updateGUI();
	
			}
			
			if(e.getActionCommand() == "Prev Page"){
				
				selectedPage--;
				updateGUI();
				
			}
			
			for (int i = 0; i < listOfCategories.size(); i++){
			
				//If the user has clicked an auto generated button.
				if(e.getActionCommand() == listOfCategories.get(i).getName()){
					
					
					
					//This if statement prevents action listeners from aggregating and causing a mess.
					if(selectedCategory == null){
						nextPageButton.addActionListener(this);
						prevPageButton.addActionListener(this);
					}
					
					selectedPage = 1;
					selectedCategory = listOfCategories.get(i);
					
					selectedPageDisplay.setText(selectedCategory.getName() + " - ");
					
					updateGUI();
					
				}
			
			}
			
			modPanel.revalidate();
		}
		
	}
	
	public static void main(String[] args) throws MalformedURLException, FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		GUIDriver gui = new GUIDriver();
		
		for(int i = 0; i < listOfCategories.size();i++){
			
			//System.out.println(listOfCategories.get(i).getName());
			JButton aButton = new JButton(listOfCategories.get(i).getName());
			aButton.addActionListener(new ButtonListener());
			categoryPanel.add(aButton);
			gui.validate();
			
		}
	
		categoryPanel.setLayout(new GridLayout(listOfCategories.size(),1));
		
		gui.validate();
		
	}


}
