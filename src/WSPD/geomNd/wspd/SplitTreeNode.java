package WSPD.geomNd.wspd;

import ProGAL.dataStructures.Set;
import WSPD.geomNd.BoundingBox;
import ProGAL.geomNd.Point;

import java.util.ArrayList;
import java.util.List;

public class SplitTreeNode {

    private Set<Point> points;
    private SplitTreeNode leftChild;
    private SplitTreeNode rightChild;
    private BoundingBox boundingBox;
    private BoundingBox rectangle;

    public SplitTreeNode(BoundingBox boundingBox, BoundingBox rectangle, Set<Point> points) {
        leftChild = null;
        rightChild = null;
        this.boundingBox = boundingBox;
        this.rectangle = rectangle;
        this.points = points;
    }

    public SplitTreeNode(SplitTreeNode leftChild, SplitTreeNode rightChild,
                         BoundingBox boundingBox, BoundingBox rectangle, Set<Point> points) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.boundingBox = boundingBox;
        this.rectangle = rectangle;
        this.points = points;
    }

    public SplitTreeNode getLeftChild(){
        return leftChild;
    }

    public SplitTreeNode getRightChild() {
        return rightChild;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Set<Point> getPoints() { return points; }

    // Gets all internal nodes in the subtree with root in this node.
    public List<SplitTreeNode> getAllInternalNodes()
    {
        SplitTreeNode leftChild = getLeftChild();
        SplitTreeNode rightChild = getRightChild();

        List<SplitTreeNode> nodes = new ArrayList<>();
        if(leftChild != null || rightChild != null){
            nodes.add(this);
            if (leftChild != null) {
                nodes.addAll(leftChild.getAllInternalNodes());
            }
            if (rightChild != null) {
                nodes.addAll(rightChild.getAllInternalNodes());
            }
        }
        return nodes;
    }

    public void setLeftChild(SplitTreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(SplitTreeNode rightChild) {
        this.rightChild = rightChild;
    }
}
