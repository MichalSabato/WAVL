
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * WAVLTree
 *
 * An implementation of a WAVL Tree with distinct integer keys and info
 *
 */

public class WAVLTree {
	private IWAVLNode root;
	private int size;
	private IWAVLNode min;
	private IWAVLNode max;

	public WAVLTree() {
		this.root = new WAVLNode();
		this.min = this.max = this.root;
		this.size = 0;
	}

	/**
	 * public boolean empty()
	 *
	 * returns true if and only if the tree is empty
	 *
	 */
	public boolean empty() {
		return !root.isRealNode();
		// return true if root not a fake node and therefore empty.
	}

	/**
	 * public String search(int k)
	 *
	 * returns the info of an item with key k if it exists in the tree
	 * otherwise, returns null
	 */
	public String search(int k) {
		IWAVLNode node = search(root, k);
		if (node == null) { // returns a null node (search found nothing)
			return null;
		}
		return node.getValue();
	}

	// start search from a certain node
	public IWAVLNode search(IWAVLNode node, int k) {
		if (!node.isRealNode()) {
			return null;
		}
		if (node.getKey() == k) {
			return node;
		}
		if (node.getKey() > k) {
			return search(node.getLeft(), k);
		}
		return search(node.getRight(), k);
	}

	/**
	 * public int insert(int k, String i)
	 *
	 * inserts an item with key k and info i to the WAVL tree. the tree must
	 * remain valid (keep its invariants). returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were necessary. returns -1
	 * if an item with key k already exists in the tree.
	 */
	public int insert(int k, String i) {
		if (search(k) != null) {
			return -1;
		}
		size++; // on this point it's certain an insert will be made, overall size of the tree is updating
		if (empty()) {
			this.root = new WAVLNode(k, i);
			min = max = root;
			return 0;
		}
		IWAVLNode new_node = new WAVLNode(k, i);
		IWAVLNode parent = getInsertPosition(root, k);// getInsertPosition = returns the parents position of the node that needs insertion.
		new_node.setParent(parent);
		if (min.getKey() > k) {
			min = new_node;
		}
		if (max.getKey() < k) {
			max = new_node;
		}
		fixSubtreeSize(parent);
		return rebalance(parent);
	}

	public void fixSubtreeSize(IWAVLNode node) {
		while (node.isRealNode()) {
			node.setSubtreeSize(node.getLeft().getSubtreeSize() + node.getRight().getSubtreeSize() + 1);
			node = node.getParent();
		} 

	}

	public IWAVLNode getInsertPosition(IWAVLNode node, int k) {
		IWAVLNode parent = null;
		while (node.isRealNode()) {
			parent = node;
			if (k < node.getKey()) {
				node = node.getLeft();
			} else
				node = node.getRight();
		}
		return parent;
	}

	public String rebalanceCase(IWAVLNode node) {
		if (!node.isRealNode()) {
			return "fakenode"; // will go to the default on reblancing, which does nothing.
		}
		int[] diff = node.getRankDif();
		if (rankDifCompare(diff, new int[] { 1, 0 })) {
			return "promote"; // insert case 1
		}
		if (rankDifCompare(diff, new int[] { 0, 2 })) {
			boolean isLeftHeavy = diff[0] == 0;
			IWAVLNode heavyChild = isLeftHeavy ? node.getLeft() : node.getRight(); // heavyChild is the child that is the heavy subtree  
			diff = heavyChild.getRankDif(); // diff is now the rank diff in the heavy subtree
			if ((isLeftHeavy && Arrays.equals(diff, new int[] { 1, 2 }))
					|| (!isLeftHeavy && Arrays.equals(diff, new int[] { 2, 1 }))) {
				return "insertSingleRotate"; // insert case 2
			}
			if ((isLeftHeavy && Arrays.equals(diff, new int[] { 2, 1 }))
					|| (!isLeftHeavy && Arrays.equals(diff, new int[] { 1, 2 })))
				return "insertDoubleRotate"; // insert case 3
		}
		if (node.getNodeType() == 0 && node.getRank() == 1) { // leaf that needs to be demoted.
			return "leafRankOne"; // delete: case 3 option
		}
		if (rankDifCompare(diff, new int[] { 3, 2 })) {
			return "demote"; // delete: case 1
		}
		if (rankDifCompare(diff, new int[] { 3, 1 })) {
			boolean isLeftHeavy = (diff[1] == 3);
			IWAVLNode heavyChild = isLeftHeavy ? node.getLeft() : node.getRight();
			diff = heavyChild.getRankDif();
			if (rankDifCompare(diff, new int[] { 2, 2 })) {
				return "doubleDemote"; // delete: case 2
			} else if ((isLeftHeavy && Arrays.equals(diff, new int[] { 2, 1 }))
					|| (!isLeftHeavy && Arrays.equals(diff, new int[] { 1, 2 }))) {
				return "deleteDoubleRotate"; // delete: case 4
			} else {
				return "deleteRotate"; // delete: case 3
			}
		}
		return "ok"; // none of the if's happened - the diff is ok.
	}

	public int rebalance(IWAVLNode node) {
		if (!node.isRealNode()) {
			return 0;
		}
		int count = 0;
		int[] diff = node.getRankDif();
		switch (rebalanceCase(node)) {
		case "promote": // insert: case 1
			node.promote();
			count = count + 1 + rebalance(node.getParent()); //  promote + call for re-balance recursively
			break;
		case "insertSingleRotate": // insert: case2
			if (diff[0] == 0) {
				rotateRight(node);
			} else {
				rotateLeft(node);
			}
			node.demote();
			fixSubtreeSize(node.getParent());
			count = count + 2; // rotate + demote.
			break;
		case "insertDoubleRotate": // insert: case 3
			IWAVLNode childHeavy = (diff[0] == 0 ? node.getLeft() : node.getRight());
			if (diff[0] == 0) {
				rotateLeft(childHeavy);
				rotateRight(node);
			} else { // diff[1]==0
				rotateRight(childHeavy);
				rotateLeft(node);
			}
			node.demote();
			childHeavy.demote();
			// promote node that was childHeavy's child, now is childHeavy's parents after rotation
			childHeavy.getParent().promote();
			fixSubtreeSize(node.getParent());
			count = count + 5; // (2) double rotate + (3) demote and promote actions
			break;
		case "leafRankOne":
			node.demote();
			this.fixSubtreeSize(node);
			count = count + 1 + rebalance(node.getParent());
			break;
		case "demote": // delete: case 1
			node.demote();
			this.fixSubtreeSize(node);
			count = count + 1 + rebalance(node.getParent()); // (1) demote + call for re-balance recursively
			break;
		case "doubleDemote": // delete: case 2
			IWAVLNode childToDemote = diff[0] == 1 ? node.getLeft() : node.getRight(); // the child with the diff 1 with his parent, need to be domoted as well 
			node.demote();
			childToDemote.demote();
			this.fixSubtreeSize(node);
			count = count + 2 + rebalance(node.getParent()); // (2) demotes + call for re-balance recursively
			break;
		case "deleteRotate": // delete: case 3
			IWAVLNode childToPromote = (diff[0] == 1 ? node.getLeft() : node.getRight());
			if (diff[0] == 1) {
				rotateRight(node);
			} else { // diff[1]==1
				rotateLeft(node);
			}
			node.demote();
			childToPromote.promote();
			
			fixSubtreeSize(node.getParent());
			count = count + 3 + rebalance(node); // node might be a [2,2] leaf that needs fixing. if not, it's a final action.
			// (1) demote + (1) promote + (1) rotate 
			break;
		case "deleteDoubleRotate": // delete: case 4
			IWAVLNode heavyChild = diff[0] == 1 ? node.getLeft() : node.getRight();
			if (diff[0] == 1) { // left is heavy
				rotateLeft(heavyChild);
				rotateRight(node);
			} else { // right is heavy
				rotateRight(heavyChild);
				rotateLeft(node);
			}
			node.demote();
			node.demote();
			heavyChild.demote();
			// the node that used to be the heavyChild's child, that is now it's parent
			heavyChild.getParent().promote();
			heavyChild.getParent().promote();
			this.fixSubtreeSize(node.getParent());
			count = count + 7; //(2) double rotate + (3) demote + (2) promote 
			break;
		case "ok":
			this.fixSubtreeSize(node);
			break;
		default:
		}
		return count;
	}

	public void rotateRight(IWAVLNode node) {
		IWAVLNode child = node.getLeft();
		IWAVLNode parent = node.getParent();

		node.setSubtreeSize(child.getRight().getSubtreeSize() + node.getRight().getSubtreeSize() + 1);
		child.setSubtreeSize(node.getSubtreeSize() + child.getLeft().getSubtreeSize() + 1);

		child.setParent(parent);
		if (!child.getParent().isRealNode()) {
			root = child;
		}
		node.setLeft(child.getRight());
		child.setRight(node);

		
	}

	public void rotateLeft(IWAVLNode node) {
		IWAVLNode child = node.getRight();
		IWAVLNode parent = node.getParent();

		node.setSubtreeSize(node.getLeft().getSubtreeSize() + child.getLeft().getSubtreeSize() + 1);
		child.setSubtreeSize(node.getSubtreeSize() + child.getRight().getSubtreeSize() + 1);

		child.setParent(parent);
		if (!child.getParent().isRealNode()) {
			root = child;
		}
		node.setRight(child.getLeft());
		child.setLeft(node);

		if (!child.getParent().isRealNode()) {
			root = child;
		}
	}

	public static boolean rankDifCompare(int[] firstDiff, int[] secondDiff) {
		return Arrays.equals(firstDiff, secondDiff)
				|| Arrays.equals(firstDiff, new int[] { secondDiff[1], secondDiff[0] });
	}

	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there; the tree
	 * must remain valid (keep its invariants). returns the number of
	 * rebalancing operations, or 0 if no rebalancing operations were needed.
	 * returns -1 if an item with key k was not found in the tree.
	 */
	public int delete(int k) {
		IWAVLNode node = search(root, k);
		if (node == null) {
			return -1; // not found - nothing to delete
		}
		size--;
		// check if the deleted node is max or min, if so - updated a new min/max
		if (min.getKey() == k) {
			min = min.successor();
		}
		if (max.getKey() == k) {
			max = max.predecessor();
		}
		// if it's a binary node (with 2 kids), swap with successor.
		if (node.getNodeType() == 3) {
			node = swapWithSuccessor(node);
		}

		IWAVLNode nodeForRebalance = removeNode(node);
		this.fixSubtreeSize(nodeForRebalance);
		
		return rebalance(nodeForRebalance); // returns the parent of the deleted node for re-balancing purposes
	}

	public IWAVLNode swapWithSuccessor(IWAVLNode node) {
		IWAVLNode successor = node.successor();
		if (!successor.isRealNode()) {
			return node;
		}

		// switch key and info between node and successor. update max if necessary.
		int nodeOldKey = node.getKey();
		String nodeOldValue = node.getValue();
		node.setKey(successor.getKey());
		node.setValue(successor.getValue());
		successor.setKey(nodeOldKey);
		successor.setValue(nodeOldValue);

		if (successor == max) {
			max = node;
		}

		return successor;
	}

	public IWAVLNode removeNode(IWAVLNode node) {
		IWAVLNode child = node.getLeft().isRealNode() ? node.getLeft() : node.getRight();
		// the first child that is real (starting from left), if both are null the child is fake node.
		IWAVLNode parent = node.getParent();
		IWAVLNode nodeForRebalance = parent;

		// set root
		if (node == root) {
			root = child;
			nodeForRebalance = child;
		}

		// delete node
		if (child.isRealNode()) { // the deleted node is onary
			child.setParent(parent);
		} else { // the deleted node is a leaf
			if (parent.isRealNode()) { // parent is real
				if (parent.getLeft().isRealNode() && parent.getLeft().getKey() == node.getKey()) {
					parent.setLeft(new WAVLNode());
				} else {
					parent.setRight(new WAVLNode());
				}
			}
		}
		return nodeForRebalance;
	}

	/**
	 * public String min()
	 *
	 * Returns the info of the item with the smallest key in the tree, or null
	 * if the tree is empty
	 */
	public String min() {
		return this.min.getValue();
	}

	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree, or null if
	 * the tree is empty
	 */
	public String max() {
		return this.max.getValue();
	}

	/**
	 * public int[] keysToArray()
	 *
	 * Returns a sorted array which contains all keys in the tree, or an empty
	 * array if the tree is empty.
	 */
	public int[] keysToArray() {
		int len = size();
		IWAVLNode[] array = new WAVLNode[len];
		inOrderTree(root, array, 0);
		int[] res = new int[len];
		for (int i = 0; i < len; i++)
			res[i] = array[i].getKey();
		return res;
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree, sorted by their
	 * respective keys, or an empty array if the tree is empty.
	 */
	public String[] infoToArray() {
		int len = size();
		IWAVLNode[] array = new WAVLNode[len];
		inOrderTree(root, array, 0);
		String[] res = new String[len];
		for (int i = 0; i < len; i++)
			res[i] = array[i].getValue();
		return res;
	}
	
	public int inOrderTree(IWAVLNode node, IWAVLNode[] array, int index) {
		if (!node.isRealNode()){
			return index;
		}
		index = inOrderTree(node.getLeft(), array, index);
		array[index] = node;
		index = inOrderTree(node.getRight(), array, index + 1);
		return index;
	}

	/**
	 * public int size()
	 *
	 * Returns the number of nodes in the tree.
	 *
	 * precondition: none postcondition: none
	 */
	public int size() {
		return size;
	}

	/**
	 * public int getRoot()
	 *
	 * Returns the root WAVL node, or null if the tree is empty
	 *
	 * precondition: none postcondition: none
	 */
	public IWAVLNode getRoot() {
		return this.root;
	}

	/**
	 * public int select(int i)
	 *
	 * Returns the value of the i'th smallest key (return -1 if tree is empty)
	 * Example 1: select(1) returns the value of the node with minimal key
	 * Example 2: select(size()) returns the value of the node with maximal key
	 * Example 3: select(2) returns the value 2nd smallest minimal node, i.e the
	 * value of the node minimal node's successor
	 *
	 * precondition: size() >= i > 0 postcondition: none
	 */
	
	public String select(int i){
		if (this.empty() || i > this.size || i < 0) {
			return null;
		}
		IWAVLNode node = min; 
		//climbs up until the first node that it's sub tree size is more or equal to the i we're looking.
		while (node.getSubtreeSize()<i){  
			node = node.getParent();
		}
		return selectDown (node, i); 
	}
	
	public String selectDown (IWAVLNode node, int i){
		int leftAndOne = node.getLeft().getSubtreeSize() + 1;
		if (leftAndOne == i){
			return node.getValue();
		}
		else if (leftAndOne < i){
			return selectDown (node.getRight(), i - leftAndOne);
		}
		else{ //leftAndOne > i
			return selectDown (node.getLeft(), i);
		} 
	}
	/**
	 * public interface IWAVLNode ! Do not delete or modify this - otherwise all
	 * tests will fail !
	 */
	public interface IWAVLNode {
		public int getKey(); // returns node's key (for virtual node return -1)
		public String getValue(); // returns node's value [info] (for virtual node return null)
		public IWAVLNode getLeft(); // returns left child (if there is no left child return null)
		public IWAVLNode getRight(); // returns right child (if there is no right child return null)
		public boolean isRealNode(); // Returns True if this is a non-virtual WAVL node (i.e not a virtual leaf)
		public int getSubtreeSize(); // Returns the number of real nodes in this node's subtree (Should be implemented in O(1))

		// functions we've added
		public int setKey(int newKey);
		public String setValue(String newValue);
		public IWAVLNode getParent();
		public void setRight(IWAVLNode NewRight);// sets Right
		public void setLeft(IWAVLNode NewLeft);// sets Left
		public void setParent(IWAVLNode NewParent);// sets Parent
		public void setSubtreeSize(int NewSize);// sets size
		public void promote();// rank+1
		public void demote();// rank-1
		public int getNodeType();		// 0-leaf ; 1-Unary Left (has only left) ; 2-Unary Right (has only right) ; 3-binary ;
		public int[] getRankDif();// returns an array with the rank differences
		public int getRank();
		public IWAVLNode successor();
		public IWAVLNode predecessor();
		public IWAVLNode subMin();
		public IWAVLNode subMax();

	}

	/**
	 * public class WAVLNode
	 *
	 * If you wish to implement classes other than WAVLTree (for example
	 * WAVLNode), do it in this file, not in another file. This class can and
	 * must be modified. (It must implement IWAVLNode)
	 */
	public class WAVLNode implements IWAVLNode {
		private int key;
		private String info;
		private int rank;

		private boolean isReal;
		private int size;
		private IWAVLNode parent;
		private IWAVLNode right;
		private IWAVLNode left;

		private WAVLNode() {
			this.key = -1;
			this.info = null;
			this.rank = -1;
			this.isReal = false;
			this.size = 0;
		}

		private WAVLNode(int key, String info) {
			this.key = key;
			this.info = info;
			this.rank = 0;
			this.isReal = true;
			this.parent = new WAVLNode();
			this.right = new WAVLNode();
			this.left = new WAVLNode();
			this.size = 1;
		}

		public int getKey() {
			return this.key;
		}

		public String getValue() {
			return this.info;
		}

		public int setKey(int newKey) {
			this.key = newKey;
			return this.key;
		}

		public String setValue(String newValue) {
			this.info = newValue;
			return this.info;
		}

		public IWAVLNode getLeft() {
			return this.left;
		}

		public IWAVLNode getRight() {
			return this.right;
		}

		public boolean isRealNode() {
			return this.isReal;
		}

		public int getSubtreeSize() { 
			return this.size;
		}

		// our new functions

		public void setSubtreeSize(int newSize) {
			this.size = newSize;
		}

		public void setRight(IWAVLNode NewRight) {
			this.right = NewRight;
			if (NewRight.isRealNode() && NewRight.getParent() != this) {
				NewRight.setParent(this);
			}
		}

		public void setLeft(IWAVLNode NewLeft) {
			this.left = NewLeft;
			if (NewLeft.isRealNode() && NewLeft.getParent() != this) {
				NewLeft.setParent(this);
			}
		}

		public void setParent(IWAVLNode newParent) {
			this.parent = newParent;
			if (newParent != null) {
				if (newParent.getKey() < this.key && newParent.getRight() != this) {
					newParent.setRight(this);
				}
				if (newParent.getKey() > this.key && newParent.getLeft() != this) {
					newParent.setLeft(this);
				}
			}
		}

		public void promote() {
			this.rank++;
		}

		@Override
		public void demote() {
			this.rank--;

		}

		// 0-Leaf ; 1-Unary Left (has only left) ;
		// 2-Unary Right (has only right) ; 3-Binary.
		@Override
		public int getNodeType() {
			if (!this.left.isRealNode() && !this.right.isRealNode())// Leaf
				return 0;
			if (!this.right.isRealNode())// Unary Left
				return 1;
			if (!this.left.isRealNode())// Unary Right
				return 2;
			return 3;// Binary
		}

		@Override
		public int[] getRankDif() { 
			int leftDiff = this.getRank() - this.getLeft().getRank();
			int rightDiff = this.getRank() - this.getRight().getRank();
			return new int[] { leftDiff, rightDiff };
		}

		@Override
		public int getRank() {
			if (!this.isRealNode()) {
				return -1;
			}
			return this.rank;
		}

		@Override
		public IWAVLNode getParent() {
			return this.parent;
		}

		@Override
		public IWAVLNode successor() {
			IWAVLNode node = this;
			if (!node.isRealNode()) {
				return node; 
			}
			if (node.getRight().isRealNode()) {
				return node.getRight().subMin(); // returns the min in the right subtree.
			}
			// no right child, finding the lowest ancestor
			IWAVLNode parent = node.getParent();
			while (parent.isRealNode() && parent.getKey() < node.getKey()) {
				node = parent;
				parent = node.getParent();
			}
			return parent; 
		}

		@Override
		public IWAVLNode predecessor() {
			IWAVLNode node = this;
			if (!node.isRealNode()) {
				return node; 
			}
			if (node.getLeft().isRealNode()) {
				return node.getLeft().subMax();
			}
			IWAVLNode parent = node.getParent();
			while (parent.isRealNode() && parent.getKey() > node.getKey()) {
				node = parent;
				parent = node.getParent();
			}
			return parent;
		}

		public IWAVLNode subMin() {
			IWAVLNode node = this;
			while (node.getLeft().isRealNode()) {
				node = node.getLeft();
			}
			return node;
		}

		public IWAVLNode subMax() {
			IWAVLNode node = this;
			while (node.getRight().isRealNode()) {
				node = node.getRight();
			}
			return node;
		}


	}

}
