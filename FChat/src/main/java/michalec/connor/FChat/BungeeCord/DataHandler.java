package michalec.connor.FChat.BungeeCord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

//BUNGEECORD BRANCH

public class DataHandler {
	
	public Plugin plugin;
	
	//HASHMAPS:
	private HashMap<String, File> files = new HashMap<String, File>(); //NameID, Corresponding file
	private HashMap<String, Configuration> YAMLData = new HashMap<String, Configuration>(); //NameID, corresponding YAML data
	
	private ConfigurationProvider configProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
	
	public DataHandler(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void addFile(String nameID, String filePath) {
		files.put(nameID, new File(filePath)); //Add new file to file path hashmap
	}
	
	//Load a specific added file's yaml data
	public boolean loadFileYAML(String nameID) {
		if(files.containsKey(nameID)) {
			if(YAMLData.containsKey(nameID)) { //Delete from YAMLData if it already contains it
				YAMLData.remove(nameID);
			}
			
			//Now add it to YAMLData
			try {
				YAMLData.put(nameID, configProvider.load(files.get(nameID)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return(true);
		}
		return(false);
	}
	
	//for saving yaml data(private):
	private void saveYAML(String nameID) {
		try {
			configProvider.save(YAMLData.get(nameID), files.get(nameID));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Does a specified path exist?
	public Boolean YAMLPathExists(String nameID, String YAMLpath) {
		return(YAMLData.get(nameID).contains(YAMLpath));
	}
	
	
	//SET(yaml):
	public void setYAMLField(String nameID, String YAMLpath, String value) {
		YAMLData.get(nameID).set(YAMLpath, value);
		this.saveYAML(nameID); //save file
		this.loadFileYAML(nameID); //reload cache
	}
	
	public void setYAMLField(String nameID, String YAMLpath, Boolean value) {
		YAMLData.get(nameID).set(YAMLpath, value);
		this.saveYAML(nameID); //save file
		this.loadFileYAML(nameID); //reload cache
	}
	
	public void setYAMLField(String nameID, String YAMLpath, Integer value) {
		YAMLData.get(nameID).set(YAMLpath, value);
		this.saveYAML(nameID); //save file
		this.loadFileYAML(nameID); //reload cache
	}
	
	public void setYAMLField(String nameID, String YAMLpath, List<?> value) { 	
		YAMLData.get(nameID).set(YAMLpath, value);
		this.saveYAML(nameID); //save file
		this.loadFileYAML(nameID); //reload cache
	}
	
	//GET VALUE(yaml):
	public String getYAMLStringField(String nameID, String YAMLpath) {
		return (YAMLData.get(nameID).getString(YAMLpath));
	}
	
	public boolean getYAMLBooleanField(String nameID, String YAMLpath) {
		return (YAMLData.get(nameID).getBoolean(YAMLpath));
	}
	
	public Integer getYAMLIntegerField(String nameID, String YAMLpath) {
		return (Integer) (YAMLData.get(nameID).getInt(YAMLpath));
	}
	
	public List<?> getYAMLListField(String nameID, String YAMLpath) {
		return(YAMLData.get(nameID).getList(YAMLpath)); //? wildcard, we don't know the type of list, use cast
	}
	
	//GET more(yaml):
	public Collection<String> getConfigurationSections(String nameID, String YAMLpath) {
		return(YAMLData.get(nameID).getSection(YAMLpath).getKeys());
	}
	
	//DELETE(yaml):
	public void deleteYAMLPath(String nameID, String YAMLpath) {
		YAMLData.get(nameID).set(YAMLpath, null);
		this.saveYAML(nameID); //save file
		this.loadFileYAML(nameID); //reload cache
	}
	
	//UTILITY, does not require you to add the file first
	//Creates a directory if it doesn't exist
	public void createDirectoryIfMissing(String dirPath) {
		File directory = new File(dirPath);
		if(!directory.exists()) {
			directory.mkdir();
		}
	}
	
	//UTILITY, does not require you to add the file first
	//Copys a default template file from the src of the jar to the specified resource folder(probably plugins/something) IF it doesnt exist
	public void copyTemplateIfMissing(String srcFilePath, String resourceFilePath) {
		File target = new File(resourceFilePath);
		
		if(!target.exists()) {
			try {
				Files.copy(plugin.getResourceAsStream(srcFilePath), target.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}