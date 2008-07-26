/**
 * 
 */
package org.chris.portmapper.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chris.portmapper.PortMapperApp;
import org.chris.portmapper.router.PortMapping;
import org.chris.portmapper.router.PortMappingPreset;
import org.chris.portmapper.router.Router;
import org.chris.portmapper.router.RouterException;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;

/**
 * @author chris
 * 
 */
public class PortMapperView extends FrameView {

	private static final String ACTION_SHOW_ABOUT_DIALOG = "mainFrame.showAboutDialog";
	private static final String ACTION_DISPLAY_ROUTER_INFO = "mainFrame.router.info";
	private static final String ACTION_CONNECT_ROUTER = "mainFrame.router.connect";
	private static final String ACTION_DISCONNECT_ROUTER = "mainFrame.router.disconnect";
	private static final String ACTION_COPY_INTERNAL_ADDRESS = "mainFrame.router.copyInternalAddress";
	private static final String ACTION_COPY_EXTERNAL_ADDRESS = "mainFrame.router.copyExternalAddress";
	private static final String ACTION_UPDATE_ADDRESSES = "mainFrame.router.updateAddresses";
	private static final String ACTION_UPDATE_PORT_MAPPINGS = "mainFrame.mappings.update";

	private static final String ACTION_PORTMAPPER_SETTINGS = "mainFrame.portmapper.settings";

	private static final String ACTION_REMOVE_MAPPINGS = "mainFrame.mappings.remove";

	private static final String ACTION_CREATE_PRESET_MAPPING = "mainFrame.preset_mappings.create";
	private static final String ACTION_EDIT_PRESET_MAPPING = "mainFrame.preset_mappings.edit";
	private static final String ACTION_REMOVE_PRESET_MAPPING = "mainFrame.preset_mappings.remove";
	private static final String ACTION_USE_PRESET_MAPPING = "mainFrame.preset_mappings.use";

	private Log logger = LogFactory.getLog(this.getClass());

	private static final String PROPERTY_MAPPING_SELECTED = "mappingSelected";
	private static final String PROPERTY_ROUTER_CONNECTED = "connectedToRouter";
	private static final String PROPERTY_PRESET_MAPPING_SELECTED = "presetMappingSelected";

	private PortMappingsTableModel tableModel;
	private JTable mappingsTable;
	private JLabel externalIPLabel, internalIPLabel;
	private JButton connectDisconnectButton;
	private JList portMappingPresets;

	/**
	 * @param application
	 */
	public PortMapperView() {
		super(PortMapperApp.getInstance());
		initView();
	}

	/**
	 * 
	 */
	private void initView() {
		// Create and set up the window.
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[fill, grow]",
				"[grow 50]unrelated[]unrelated[grow 50]"));

		panel.add(getMappingsPanel(), "wrap");
		panel.add(getRouterPanel(), "grow 0, split 2");
		panel.add(getPresetPanel(), "wrap");
		panel.add(getLogPanel(), "wrap");

		this.setComponent(panel);
	}

	private JComponent getRouterPanel() {
		ActionMap actionMap = this.getContext().getActionMap(this.getClass(),
				this);
		JPanel routerPanel = new JPanel(new MigLayout("", "[fill, grow][]", ""));
		routerPanel.setBorder(BorderFactory.createTitledBorder(PortMapperApp
				.getResourceMap().getString("mainFrame.router.title")));

		routerPanel.add(new JLabel(PortMapperApp.getResourceMap().getString(
				"mainFrame.router.external_address")), "align label"); //$NON-NLS-2$
		externalIPLabel = new JLabel(PortMapperApp.getResourceMap().getString(
				"mainFrame.router.not_connected"));
		routerPanel.add(externalIPLabel, "width 130!");
		routerPanel.add(
				new JButton(actionMap.get(ACTION_COPY_EXTERNAL_ADDRESS)),
				"sizegroup router");
		routerPanel.add(new JButton(actionMap.get(ACTION_UPDATE_ADDRESSES)),
				"wrap, spany 2, aligny base, sizegroup router");

		routerPanel.add(new JLabel(PortMapperApp.getResourceMap().getString(
				"mainFrame.router.internal_address")), "align label");
		internalIPLabel = new JLabel(PortMapperApp.getResourceMap().getString(
				"mainFrame.router.not_connected"));
		routerPanel.add(internalIPLabel, "width 130!");
		routerPanel.add(
				new JButton(actionMap.get(ACTION_COPY_INTERNAL_ADDRESS)),
				"wrap, sizegroup router");

		connectDisconnectButton = new JButton(actionMap
				.get(ACTION_CONNECT_ROUTER));
		routerPanel.add(connectDisconnectButton, "");
		routerPanel.add(new JButton(actionMap.get(ACTION_DISPLAY_ROUTER_INFO)),
				"sizegroup router");
		routerPanel.add(new JButton(actionMap.get(ACTION_SHOW_ABOUT_DIALOG)),
				"sizegroup router, wrap");

		this.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(PROPERTY_ROUTER_CONNECTED)) {
					logger.info("Connection state changed to "
							+ evt.getNewValue());
					ActionMap actionMap = getContext().getActionMap(
							PortMapperApp.getInstance().getView().getClass(),
							PortMapperApp.getInstance().getView());

					if (evt.getNewValue().equals(Boolean.TRUE)) {
						connectDisconnectButton.setAction(actionMap
								.get(ACTION_DISCONNECT_ROUTER));
					} else {
						connectDisconnectButton.setAction(actionMap
								.get(ACTION_CONNECT_ROUTER));
					}
				}
			}
		});
		routerPanel.add(new JButton(actionMap.get(ACTION_PORTMAPPER_SETTINGS)),
				"");

		return routerPanel;
	}

	private JComponent getLogPanel() {

		JTextArea logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		logTextArea.setWrapStyleWord(true);
		logTextArea.setLineWrap(true);

		PortMapperApp.getInstance().setLoggingTextArea(logTextArea);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(logTextArea);

		JPanel logPanel = new JPanel(new MigLayout("", "[grow, fill]",
				"[grow, fill]"));
		logPanel.setBorder(BorderFactory.createTitledBorder(PortMapperApp
				.getResourceMap().getString("mainFrame.log_messages.title")));
		logPanel.add(scrollPane, "height 100::");

		return logPanel;
	}

	private JComponent getPresetPanel() {
		ActionMap actionMap = this.getContext().getActionMap(this.getClass(),
				this);

		JPanel presetPanel = new JPanel(new MigLayout("", "[grow, fill][]", ""));
		presetPanel.setBorder(BorderFactory.createTitledBorder(PortMapperApp
				.getResourceMap().getString(
						"mainFrame.port_mapping_presets.title")));

		portMappingPresets = new JList(new PresetListModel(PortMapperApp
				.getInstance().getSettings()));
		portMappingPresets
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		portMappingPresets.setLayoutOrientation(JList.VERTICAL);

		portMappingPresets
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						logger.trace("Selection of preset list has changed: "
								+ isPresetMappingSelected());
						firePropertyChange(PROPERTY_PRESET_MAPPING_SELECTED,
								false, isPresetMappingSelected());
					}
				});

		presetPanel.add(new JScrollPane(portMappingPresets), "spany 4, grow");

		presetPanel.add(
				new JButton(actionMap.get(ACTION_CREATE_PRESET_MAPPING)),
				"wrap, sizegroup preset_buttons");
		presetPanel.add(new JButton(actionMap.get(ACTION_EDIT_PRESET_MAPPING)),
				"wrap, sizegroup preset_buttons");
		presetPanel.add(
				new JButton(actionMap.get(ACTION_REMOVE_PRESET_MAPPING)),
				"wrap, sizegroup preset_buttons");
		presetPanel.add(new JButton(actionMap.get(ACTION_USE_PRESET_MAPPING)),
				"wrap, sizegroup preset_buttons");

		return presetPanel;
	}

	private JComponent getMappingsPanel() {
		// Mappings panel

		ActionMap actionMap = this.getContext().getActionMap(this.getClass(),
				this);

		tableModel = new PortMappingsTableModel();
		mappingsTable = new JTable(tableModel);
		mappingsTable
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mappingsTable.setSize(new Dimension(400, 100));
		mappingsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						firePropertyChange(PROPERTY_MAPPING_SELECTED, false,
								isMappingSelected());
					}
				});

		JScrollPane mappingsTabelPane = new JScrollPane();
		mappingsTabelPane.setViewportView(mappingsTable);

		JPanel mappingsPanel = new JPanel(new MigLayout("", "[fill,grow]",
				"[grow,fill][]"));
		mappingsPanel.setName("port_mappings");
		Border panelBorder = BorderFactory.createTitledBorder(PortMapperApp
				.getResourceMap().getString("mainFrame.port_mappings.title"));
		mappingsPanel.setBorder(panelBorder);
		mappingsPanel.add(mappingsTabelPane, "height 100::, span 2, wrap");

		mappingsPanel.add(new JButton(actionMap.get(ACTION_REMOVE_MAPPINGS)),
				"");
		mappingsPanel.add(new JButton(actionMap
				.get(ACTION_UPDATE_PORT_MAPPINGS)), "wrap");
		return mappingsPanel;
	}

	@Action(name = ACTION_UPDATE_ADDRESSES, enabledProperty = PROPERTY_ROUTER_CONNECTED)
	public void updateAddresses() {
		Router router = PortMapperApp.getInstance().getRouter();
		if (router == null) {
			externalIPLabel.setText(PortMapperApp.getResourceMap().getString(
					"mainFrame.router.not_connected"));
			internalIPLabel.setText(PortMapperApp.getResourceMap().getString(
					"mainFrame.router.not_connected"));
			return;
		}
		externalIPLabel.setText(PortMapperApp.getResourceMap().getString(
				"mainFrame.router.updating"));
		internalIPLabel.setText(PortMapperApp.getResourceMap().getString(
				"mainFrame.router.updating"));

		internalIPLabel.setText(router.getInternalIPAddress());
		try {
			externalIPLabel.setText(router.getExternalIPAddress());
		} catch (RouterException e) {
			externalIPLabel.setText("");
			logger.error("Did not get external IP address", e);
		}
	}

	@Action(name = ACTION_CONNECT_ROUTER)
	public Task<Void, Void> connectRouter() {
		return new ConnectTask();
	}

	@Action(name = ACTION_DISCONNECT_ROUTER)
	public void disconnectRouter() {
		PortMapperApp.getInstance().disconnectRouter();
		updateAddresses();
		updatePortMappings();
	}

	private void addMapping(Collection<PortMapping> portMappings) {
		boolean success = false;
		Router router = PortMapperApp.getInstance().getRouter();
		if (router == null) {
			return;
		}

		try {
			success = router.addPortMappings(portMappings);
		} catch (RouterException e) {
			logger.error("Could not add port mapping", e);
		}

		if (success) {
			logger.info("Portmapping was added successfully");
		} else {
			logger.warn("Portmapping was NOT added successfully");
			JOptionPane.showMessageDialog(this.getFrame(),
					"The port mapping could not be added.",
					"Error adding port mapping", JOptionPane.WARNING_MESSAGE);
		}
		this.updatePortMappings();
	}

	@Action(name = ACTION_REMOVE_MAPPINGS, enabledProperty = PROPERTY_MAPPING_SELECTED)
	public void removeMappings() {
		Collection<PortMapping> selectedMappings = this
				.getSelectedPortMappings();
		for (PortMapping mapping : selectedMappings) {
			logger.info("Removing mapping " + mapping);
			boolean success = false;
			try {
				success = PortMapperApp.getInstance().getRouter()
						.removeMapping(mapping);
			} catch (RouterException e) {
				logger.error("Could not remove port mapping " + mapping, e);
				break;
			}
			if (success) {
				logger.info("Mapping was removed successfully: " + mapping);
			} else {
				logger
						.error("Mapping was not removed successfully: "
								+ mapping);
				break;
			}
		}
		if (selectedMappings.size() > 0) {
			updatePortMappings();
		}
	}

	@Action(name = ACTION_DISPLAY_ROUTER_INFO, enabledProperty = PROPERTY_ROUTER_CONNECTED)
	public void displayRouterInfo() {
		Router router = PortMapperApp.getInstance().getRouter();
		if (router == null) {
			logger.warn("Not connected to router, could not get router info");
			return;
		}
		try {
			router.logRouterInfo();
		} catch (RouterException e) {
			logger.error("Could not get router info", e);
		}
	}

	@Action(name = ACTION_SHOW_ABOUT_DIALOG)
	public void showAboutDialog() {
		PortMapperApp.getInstance().show(new AboutDialog());
	}

	@Action(name = ACTION_COPY_INTERNAL_ADDRESS, enabledProperty = PROPERTY_ROUTER_CONNECTED)
	public void copyInternalAddress() {
		this.copyTextToClipboard(this.internalIPLabel.getText());
	}

	@Action(name = ACTION_COPY_EXTERNAL_ADDRESS, enabledProperty = PROPERTY_ROUTER_CONNECTED)
	public void copyExternalAddress() {
		this.copyTextToClipboard(this.externalIPLabel.getText());
	}

	@Action(name = ACTION_UPDATE_PORT_MAPPINGS, enabledProperty = PROPERTY_ROUTER_CONNECTED)
	public void updatePortMappings() {
		Router router = PortMapperApp.getInstance().getRouter();
		if (router == null) {
			this.tableModel.setMappings(new LinkedList<PortMapping>());
			return;
		}
		try {
			Collection<PortMapping> mappings = router.getPortMappings();
			logger.info("Found " + mappings.size() + " mappings");
			this.tableModel.setMappings(mappings);
		} catch (RouterException e) {
			logger.error("Could not get port mappings", e);
		}
	}

	@Action(name = ACTION_USE_PRESET_MAPPING, enabledProperty = PROPERTY_PRESET_MAPPING_SELECTED)
	public void addPresetMapping() {
		PortMappingPreset selectedItem = (PortMappingPreset) this.portMappingPresets
				.getSelectedValue();
		if (selectedItem != null) {
			String localHostAddress = PortMapperApp.getInstance()
					.getLocalHostAddress();
			if (localHostAddress == null) {
				JOptionPane.showMessageDialog(this.getFrame(), PortMapperApp
						.getResourceMap().getString(
								"messages.error_getting_localhost_address"),
						PortMapperApp.getResourceMap().getString(
								"messages.error"), JOptionPane.ERROR_MESSAGE);
			} else {
				logger.info("Adding port mappings for preset "
						+ selectedItem.toString());
				addMapping(selectedItem.getPortMappings(localHostAddress));
			}
		}
	}

	@Action(name = ACTION_CREATE_PRESET_MAPPING)
	public void createPresetMapping() {
		PortMapperApp.getInstance().show(
				new EditPresetDialog(new PortMappingPreset()));
	}

	@Action(name = ACTION_EDIT_PRESET_MAPPING, enabledProperty = PROPERTY_PRESET_MAPPING_SELECTED)
	public void editPresetMapping() {
		PortMappingPreset selectedPreset = (PortMappingPreset) this.portMappingPresets
				.getSelectedValue();
		PortMapperApp.getInstance().show(new EditPresetDialog(selectedPreset));
	}

	@Action(name = ACTION_PORTMAPPER_SETTINGS)
	public void changeSettings() {
		logger.debug("Open Settings dialog");
		PortMapperApp.getInstance().show(new SettingsDialog());
	}

	@Action(name = ACTION_REMOVE_PRESET_MAPPING, enabledProperty = PROPERTY_PRESET_MAPPING_SELECTED)
	public void removePresetMapping() {
		PortMappingPreset selectedPreset = (PortMappingPreset) this.portMappingPresets
				.getSelectedValue();
		PortMapperApp.getInstance().getSettings().removePresets(selectedPreset);
	}

	public void fireConnectionStateChange() {
		firePropertyChange(PROPERTY_ROUTER_CONNECTED, !isConnectedToRouter(),
				isConnectedToRouter());
	}

	public boolean isConnectedToRouter() {
		return PortMapperApp.getInstance().isConnected();
		// return true;
	}

	public boolean isMappingSelected() {
		return this.isConnectedToRouter()
				&& this.getSelectedPortMappings().size() > 0;
	}

	public boolean isPresetMappingSelected() {
		return this.portMappingPresets.getSelectedValue() != null;
	}

	public Collection<PortMapping> getSelectedPortMappings() {
		int[] selectedRows = mappingsTable.getSelectedRows();
		Collection<PortMapping> selectedMappings = new LinkedList<PortMapping>();
		if (selectedRows != null) {
			for (int rowNumber : selectedRows) {
				if (rowNumber >= 0) {
					PortMapping mapping = tableModel.getPortMapping(rowNumber);
					if (mapping != null) {
						selectedMappings.add(mapping);
					}
				}
			}
		}
		return selectedMappings;
	}

	private void copyTextToClipboard(String text) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(text), new ClipboardOwner() {
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
			}
		});
	}

	private class ConnectTask extends Task<Void, Void> {

		/**
		 * @param application
		 */
		public ConnectTask() {
			super(PortMapperApp.getInstance());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jdesktop.swingworker.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {
			PortMapperApp.getInstance().connectRouter();
			message("updateAddresses");
			updateAddresses();
			message("updatePortMappings");
			updatePortMappings();
			return null;
		}

		protected void failed(Throwable cause) {
			logger.warn("Could not connect to router: " + cause.getMessage());
		}
	}
}