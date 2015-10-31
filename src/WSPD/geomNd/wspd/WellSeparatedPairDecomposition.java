package WSPD.geomNd.wspd;

import ProGAL.dataStructures.Pair;
import ProGAL.dataStructures.Set;
import WSPD.geomNd.BoundingBox;
import ProGAL.geomNd.HyperSphere;
import ProGAL.geomNd.Point;

public class WellSeparatedPairDecomposition {
    public Set<Pair<Set<Point>, Set<Point>>> WSPD;

    public WellSeparatedPairDecomposition(SplitTree tree, double s) {
        WSPD = new Set<>();

        for(SplitTreeNode node : tree.getAllInternalNodes()){
            WSPD.append(findPairs(node.getLeftChild(), node.getRightChild(), s));
        }
    }

    public Set<Pair<Set<Point>, Set<Point>>>  findPairs(SplitTreeNode v, SplitTreeNode w, double s)
    {
        if (isWellSeparated(v, w, s)){
            return new Set<Pair<Set<Point>, Set<Point>>> () {{
                insert(new Pair<>(v.getPoints(), w.getPoints()));
            }};
        } else {
            Set<Pair<Set<Point>, Set<Point>>> pairs = new Set<>();
            if (v.getBoundingBox().getMaxLength() <= w.getBoundingBox().getMaxLength()){
                SplitTreeNode w_l = w.getLeftChild();
                SplitTreeNode w_r = w.getRightChild();

                pairs.append(findPairs(v, w_l, s));
                pairs.append(findPairs(v, w_r, s));
            } else {
                SplitTreeNode v_l = v.getLeftChild();
                SplitTreeNode v_r = v.getRightChild();

                pairs.append(findPairs(v_l, w, s));
                pairs.append(findPairs(v_r, w, s));
            }
            return pairs;
        }
    }

    public boolean isWellSeparated(SplitTreeNode v, SplitTreeNode w, double s){
        BoundingBox box_v = v.getBoundingBox();
        Point center_v = box_v.getCenterPoint();
        double radius_v = center_v.distance(box_v.getCornerPoints().get(0));
        HyperSphere hyperSphere_v = new HyperSphere(center_v, radius_v);

        BoundingBox box_w = w.getBoundingBox();
        Point center_w = box_w.getCenterPoint();
        double radius_w = center_w.distance(box_w.getCornerPoints().get(0));
        HyperSphere hyperSphere_w = new HyperSphere(center_w, radius_w);

        double distanceBetweenSpheres = hyperSphere_v.getCenter().distance(hyperSphere_w.getCenter()) -
                (radius_v + radius_w);

        double maxRadius = Math.max(radius_v, radius_w);

        return distanceBetweenSpheres >= s*maxRadius;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Pair<Set<Point>, Set<Point>> pair : WSPD){
            for(Point p : pair.fst){
                builder.append(p.toString());
            }
            builder.append(" :: ");
            for(Point p : pair.snd){
                builder.append(p.toString());
                builder.append(",");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

}
