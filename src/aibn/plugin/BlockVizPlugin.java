package aibn.plugin;

import giny.model.Node;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.visual.VisualMappingManager;

public class BlockVizPlugin extends CytoscapePlugin implements ActionListener {

	public Vector myResults = null; // This Vector will contain the blocks
	public CyNetwork myNetwork = null; // This network will be constructed from
	// the provided file

	public BufferedReader myBlocks = null;
	private CyLayoutAlgorithm myLayout = null;

	private static CytoPanel cyPanel; // The Cytoscape panel
	private BlockVizPanel bvPanel = null; // The MD UI panel

	File blocksFile = null; // File pointer for the blocks
	File networkFile = null; // File pointer for the network
	String[] queryGenes = null; // The genes we're searching for
	boolean blocksLoaded = false; // Whether or not the blocks file has been
	// loaded
	boolean networkLoaded = false; // Whether or not the network file has been

	// loaded

	public BlockVizPlugin() {
		// create a new action to respond to menu activation
		BlockVizPluginAction action = new BlockVizPluginAction();
		// set the preferred menu
		action.setPreferredMenu("Plugins");
		// and add it to the menus
		Cytoscape.getDesktop().getCyMenus().addAction(action);
	}

	public class BlockVizPluginAction extends CytoscapeAction {
		public BlockVizPluginAction() {
			super("BlockViz");
		}

		public void actionPerformed(ActionEvent ae) {
			init();
		}
	}

	/*
	 * Initialize the UI panel for the plugin
	 */
	public void init() {
		CytoscapeDesktop cyDesktop = Cytoscape.getDesktop();
		cyPanel = cyDesktop.getCytoPanel(SwingConstants.WEST);
		bvPanel = new BlockVizPanel();
		bvPanel.blocksButton.addActionListener(this);
		bvPanel.networkButton.addActionListener(this);
		bvPanel.queryButton.addActionListener(this);

		cyPanel.add("MD", bvPanel);

		/*
		 * Settle for the Cytoscape hierarchical layout, since the yFiles
		 * algorithms are explicitly unavailable through CyLayout due to
		 * licensing issues
		 */
		Collection allLayouts = CyLayouts.getAllLayouts();
		Iterator layoutIterator = allLayouts.iterator();
		while (layoutIterator.hasNext()) {
			CyLayoutAlgorithm cla = (CyLayoutAlgorithm) layoutIterator.next();
			if (cla.getName().equals("hierarchical")) {
				myLayout = cla;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == bvPanel.blocksButton) {
			// Open a file browser for the blocks
			bvPanel.fileChooser.showOpenDialog(bvPanel);
			blocksFile = bvPanel.fileChooser.getSelectedFile();

			// Load the blocks into a File we can search through
			loadBlocks();
			blocksLoaded = true;
		} else if (evt.getSource() == bvPanel.networkButton) {
			// Open a file browser for the network
			bvPanel.fileChooser.showOpenDialog(bvPanel);
			networkFile = bvPanel.fileChooser.getSelectedFile();

			// Create a network from the provided file, but don't visualize it
			loadNetwork();
			networkLoaded = true;
		} else if (evt.getSource() == bvPanel.queryButton) {
			// Get the genes we're searching for, separated by commas or spaces
			queryGenes = bvPanel.queryField.getText().split("[ ,]");

			// Make sure the environment is sane, then search
			doSearch();
		} else {
			System.out.println("Unknown event");
		}
	}

	/*
	 * Check to see if the blocks and network files are loaded and keep the UI
	 * panel updated
	 */
	private void doSearch() {
		if (blocksLoaded && networkLoaded) {
			bvPanel.blocksFound.setText("Searching...");
			bvPanel.paintImmediately(bvPanel.getBounds());
			searchGenes(queryGenes);
			bvPanel.blocksFound.setText(myResults.size() + " blocks found");
		} else {
			if (blocksLoaded && !networkLoaded) {
				bvPanel.blocksFound
						.setText("Please load a network before performing your search");
			} else if (!blocksLoaded && networkLoaded) {
				bvPanel.blocksFound
						.setText("Please load a blocks file before performing your search");
			} else {
				bvPanel.blocksFound
						.setText("Please load a network and a blocks file before performing your search");
			}
		}
	}

	/*
	 * Search for the provided genes in the blocks file, then fill myResults
	 * with matching blocks and construct new networks based on it
	 * 
	 * @param String searchGenes[] Array of genes to search for
	 */
	private void searchGenes(String searchGenes[]) {
		try {
			/*
			 * Pick out every line from the block file where the queried gene
			 * appears before the tab
			 */

			String line;
			String[] queryGenes = null;
			String[] targetGenes = null;
			// Initialize the vector with each search
			myResults = new Vector();
			// Use a new BufferedReader each time since it keeps its position
			myBlocks = new BufferedReader(new FileReader(blocksFile.getPath()));

			if (searchGenes.length == 0) {
				throw new Exception("Empty query");
			}

			while ((line = myBlocks.readLine()) != null) {
				// Narrow it down to any line containing the first query
				// gene
				if (Pattern.matches(".*" + searchGenes[0] + ".*", line)) {
					// Break the line up at the tab (before the tab is query
					// genes, after is target)
					String[] blockString = line.split("\t");

					// Loop through the query genes and make sure all of
					// them appear in the list
					queryGenes = blockString[0].split(",");
					targetGenes = blockString[1].split(",");

					// Stop looping once we've found them all
					int found = 0;

					for (int i = 0; i < queryGenes.length; i++) {
						for (int j = 0; j < searchGenes.length; j++) {
							if (searchGenes[j].equals(queryGenes[i])) {
								found++;
							}
							if (found == searchGenes.length) {
								break;
							}
						}
						if (found == searchGenes.length) {
							break;
						}
					}

					if (found != searchGenes.length) {
						// Skip to the next line
						continue;
					} else {
						// Construct a block based on this line, then add it to
						// myResults
						Block newBlock = new Block();
						newBlock.setQuery(queryGenes);
						newBlock.setTarget(targetGenes);
						myResults.add(newBlock);
					}
				}
			}
			// Done parsing the file

			// Create a label based on the query
			String networkLabel = "";
			for (int i = 0; i < searchGenes.length; i++) {
				networkLabel = searchGenes[i] + " " + networkLabel;
			}

			// Loop through all of the blocks we found and create networks out
			// of them. Add a unique number to the label.
			for (int i = 0; i < myResults.size(); i++) {
				Block myBlock = (Block) myResults.get(i);
				constructNetworkFromBlock(myBlock, "Blocks for " + networkLabel
						+ "(" + i + ")");
			}
		} catch (Exception ex) {
			System.out.println("Caught exception: " + ex.getMessage());
		}
	}

	/*
	 * This method will construct a new Cytoscape network using data from the
	 * provided block
	 * 
	 * @param Block myBlock Contains a Vector of the query and target genes
	 * 
	 * @param String networkLabel The name the network will have in Cytoscape
	 */
	private void constructNetworkFromBlock(Block myBlock, String networkLabel)
			throws Exception {
		// Construct a new network with the provided label
		CyNetwork cyNetwork = Cytoscape.createNetwork(networkLabel, false);

		/*
		 * Iterate through the query and target genes, pulling matching edges
		 * out of the appropriate network as we go
		 */

		for (int x = 0; x < myBlock.queryBlocks.size(); x++) {
			for (int y = 0; y < myBlock.targetBlocks.size(); y++) {
				Iterator edges = myNetwork.edgesIterator();

				// Construct the new network using edges from the network we
				// loaded from a file

				while (edges.hasNext()) {
					CyEdge myEdge = (CyEdge) edges.next();
					Node mySource = myEdge.getSource();
					Node myTarget = myEdge.getTarget();
					if ((mySource.getIdentifier().equals(myBlock.queryBlocks
							.get(x)))
							&& (myTarget.getIdentifier()
									.equals(myBlock.targetBlocks.get(y)))) {
						cyNetwork.addEdge(myEdge);
						cyNetwork.addNode(myEdge.getSource());
						cyNetwork.addNode(myEdge.getTarget());
					}
				}
			}
		}

		// Create a view based on the network we just created
		CyNetworkView cnv = Cytoscape.createNetworkView(cyNetwork,
				networkLabel, myLayout);

		// Strangely, the visual style the networks default to is not, in fact,
		// 'default', so we need to set this explicitly
		VisualMappingManager vmm = new VisualMappingManager(cnv);
		vmm.setVisualStyle("default");

	}

	/*
	 * Get the name of the blocks file from the user and store it
	 */
	private void loadBlocks() {
		try {
			bvPanel.blocksFile.setText("Loading...");
			bvPanel.paintImmediately(bvPanel.getBounds());
			bvPanel.blocksFile.setText(blocksFile.getName());
		} catch (Exception ex) {
			System.out.println("Caught exception loading blocks: "
					+ ex.getMessage());
		}

	}

	/*
	 * Get the name of the network file from the user and create a network from
	 * it
	 */
	private void loadNetwork() {
		try {
			bvPanel.networkFile.setText("Loading...");
			bvPanel.paintImmediately(bvPanel.getBounds());
			// Don't create a network view ('false' argument)
			myNetwork = Cytoscape.createNetworkFromFile(networkFile.getPath(),
					false);
			bvPanel.networkFile.setText(networkFile.getName());
		} catch (Exception ex) {
			System.out.println("Caught exception loading the network: "
					+ ex.getMessage());
		}
	}
}
