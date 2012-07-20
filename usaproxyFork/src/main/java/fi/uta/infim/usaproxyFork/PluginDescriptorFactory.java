package fi.uta.infim.usaproxyFork;

import java.io.File;

import javax.xml.bind.JAXB;

public abstract class PluginDescriptorFactory {

	public static PluginDescriptor createFromFile( File descriptorFile )
	{
		PluginDescriptor pd = JAXB.unmarshal(descriptorFile, PluginDescriptor.class ); 
		return pd;
	}
	
	public static PluginDescriptor getDescriptorByName( String name, File applicationRootDir )
	{
		File pluginsDir = new File( applicationRootDir, "plugins" );
		File thisPluginDir = new File( pluginsDir, name );
		File descriptorFile = new File( thisPluginDir, "descriptor.xml" );
		return createFromFile(descriptorFile);
	}
}
