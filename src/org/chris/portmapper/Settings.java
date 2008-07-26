/**
 * 
 */
package org.chris.portmapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.chris.portmapper.router.PortMappingPreset;

/**
 * @author chris
 * 
 */
public class Settings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1349121864190190000L;

	public final static String PROPERTY_PORT_MAPPING_PRESETS = "presets";

	private List<PortMappingPreset> presets;
	private Boolean useEntityEncoding;
	private Integer logLevel;

	private transient PropertyChangeSupport propertyChangeSupport;

	public Settings() {
		useEntityEncoding = true;
		this.setLogLevel(Level.INFO);
		presets = new ArrayList<PortMappingPreset>();
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(String property,
			PropertyChangeListener listener) {
		this.propertyChangeSupport
				.addPropertyChangeListener(property, listener);
	}

	public List<PortMappingPreset> getPresets() {
		return presets;
	}

	public void setPresets(List<PortMappingPreset> presets) {
		this.presets = presets;
	}

	public void addPreset(PortMappingPreset newPreset) {
		List<PortMappingPreset> oldPresets = new ArrayList<PortMappingPreset>(
				this.presets);
		this.presets.add(newPreset);
		this.propertyChangeSupport.firePropertyChange(
				PROPERTY_PORT_MAPPING_PRESETS, oldPresets,
				new ArrayList<PortMappingPreset>(this.presets));
	}

	public void removePresets(PortMappingPreset selectedPreset) {
		List<PortMappingPreset> oldPresets = new ArrayList<PortMappingPreset>(
				this.presets);
		this.presets.remove(selectedPreset);
		this.propertyChangeSupport.firePropertyChange(
				PROPERTY_PORT_MAPPING_PRESETS, oldPresets,
				new ArrayList<PortMappingPreset>(this.presets));
	}

	/**
	 * @param portMappingPreset
	 */
	public void savePreset(PortMappingPreset portMappingPreset) {
		this.propertyChangeSupport.firePropertyChange(
				PROPERTY_PORT_MAPPING_PRESETS, null,
				new ArrayList<PortMappingPreset>(this.presets));
	}

	@Override
	public String toString() {
		return "[Settings: presets=" + presets + ", useEntityEncoding="
				+ useEntityEncoding + ", logLevel=" + logLevel + "]";
	}

	/**
	 * @return the useEntityEncoding
	 */
	public boolean isUseEntityEncoding() {
		return useEntityEncoding;
	}

	/**
	 * @param useEntityEncoding
	 *            the useEntityEncoding to set
	 */
	public void setUseEntityEncoding(boolean useEntityEncoding) {
		this.useEntityEncoding = useEntityEncoding;
	}

	/**
	 * @return
	 */
	public Level getLogLevel() {
		return Level.toLevel(this.logLevel);
	}

	/**
	 * @param logLevel
	 *            the logLevel to set
	 */
	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel.toInt();
	}
}