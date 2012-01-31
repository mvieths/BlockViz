package aibn.plugin;

import java.util.Vector;

public class Block {
	Vector queryBlocks = null; // The query genes for this block
	Vector targetBlocks = null; // The target genes for this block

	public Block() {
		queryBlocks = new Vector();
		targetBlocks = new Vector();
	}

	public void setQuery(String[] genes) {
		for (int i = 0; i < genes.length; i++) {
			queryBlocks.add(genes[i]);
		}
	}

	public void setTarget(String[] genes) {
		for (int i = 0; i < genes.length; i++) {
			targetBlocks.add(genes[i]);
		}
	}
}
