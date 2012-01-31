package aibn.plugin;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BlockVizPanel extends JPanel {
	public JLabel blocksFile = null; // Status text for the blocks file
	public JLabel networkFile = null; // Status text for the network file
	public JButton blocksButton = null; // Button to open a file browser for the
	// blocks file
	public JButton networkButton = null; // Button to open a file browser for
	// the network file
	public JFileChooser fileChooser = null; // File browser

	public JTextField queryField = null; // Text box to enter your query genes
	public JButton queryButton = null; // Search button

	public JLabel blocksFound = null; // Status text for the search

	public BlockVizPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT));

		blocksFile = new JLabel("No blocks loaded");
		blocksFile.setMinimumSize(new Dimension(20, 0));
		networkFile = new JLabel("No network loaded");
		networkFile.setMinimumSize(new Dimension(20, 0));
		blocksButton = new JButton("Load Blocks");
		networkButton = new JButton("Load Network");
		fileChooser = new JFileChooser();

		queryField = new JTextField("Enter a query gene", 20);
		queryButton = new JButton("Query");

		blocksFound = new JLabel("No blocks found");

		/*
		 * Construct the UI rows at a time for better control of the layout
		 */
		JPanel box = new JPanel();
		JPanel blocksRow = new JPanel();
		JPanel networkRow = new JPanel();
		JPanel queryRow = new JPanel();
		JPanel resultsRow = new JPanel();

		// Overall layout so we go top to bottom
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

		// Row for the blocks file entry
		blocksRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		blocksRow.add(blocksButton);
		blocksRow.add(blocksFile);
		box.add(blocksRow);

		// Row for the network file entry
		networkRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		networkRow.add(networkButton);
		networkRow.add(networkFile);
		box.add(networkRow);

		// Row for the query box and button
		queryRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		queryRow.add(queryButton);
		queryRow.add(queryField);
		box.add(queryRow);

		// Row to display the number of blocks found
		resultsRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		resultsRow.add(blocksFound);
		box.add(resultsRow);

		// Add the box to the overall panel
		this.add(box);
	}
}
