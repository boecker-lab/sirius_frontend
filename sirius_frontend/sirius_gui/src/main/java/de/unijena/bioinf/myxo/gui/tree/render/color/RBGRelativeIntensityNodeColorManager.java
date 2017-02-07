package de.unijena.bioinf.myxo.gui.tree.render.color;

import de.unijena.bioinf.myxo.gui.tree.structure.TreeNode;

public class RBGRelativeIntensityNodeColorManager extends RBGNodeColorManager {

	public RBGRelativeIntensityNodeColorManager(TreeNode root) {
		super(root);
	}

	@Override
	public double getValue(TreeNode node) {
		return node.getPeakRelativeIntensity();
	}

}
