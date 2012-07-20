package fi.uta.infim.usaproxyFork;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="plugin")
public class PluginDescriptor {

	/**
	 * Plugin's name
	 */
	private String name;
	
	/**
	 * Name of the main plugin JS file
	 */
	private String pluginFilename;
	
	/**
	 * Filenames of all the other (support) JS files to load
	 */
	private List< String > jsFilenames;
	
	/**
	 * Filenames of the CSS files to load
	 */
	private List< String > cssFilenames;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement( name="filename" )
    @XmlElementWrapper( name="supportJSFiles" )
	public List< String > getJsFilenames() {
		return jsFilenames;
	}

	public void setJsFilenames(List< String > jsFilenames) {
		this.jsFilenames = jsFilenames;
	}

	@XmlElement( name="filename" )
    @XmlElementWrapper( name="cssFiles" )
	public List< String > getCssFilenames() {
		return cssFilenames;
	}

	public void setCssFilenames(List< String > cssFilenames) {
		this.cssFilenames = cssFilenames;
	}

	public String getPluginFilename() {
		return pluginFilename;
	}

	public void setPluginFilename(String pluginFilename) {
		this.pluginFilename = pluginFilename;
	}
	
}
