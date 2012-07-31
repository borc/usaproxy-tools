package fi.uta.infim.usaproxyFork;

import java.io.File;

import javax.xml.bind.JAXB;

/**
 * A factory class for creating PluginDescriptor objects from descriptor files.
 * @author Teemu Pääkkönen
 *
 */
public abstract class PluginDescriptorFactory {

	/**
	 * Creates a PluginDescriptor object from a descriptor file
	 * @param descriptorFile a descriptor file to read
	 * @return a descriptor object
	 */
	public static PluginDescriptor createFromFile( File descriptorFile )
	{
		PluginDescriptor pd = JAXB.unmarshal(descriptorFile, PluginDescriptor.class ); 
		return pd;
	}
	
	/**
	 * Given a plugin name, finds its descriptor file and returns a descriptor
	 * object.
	 * @param name the plugin's name
	 * @return a descriptor object
	 */
	public static PluginDescriptor getDescriptorByName( String name )
	{
		File thisPluginDir = new File( UsaProxy.PLUGINS_DIR, name );
		File descriptorFile = new File( thisPluginDir, "descriptor.xml" );
		return createFromFile(descriptorFile);
	}
}
