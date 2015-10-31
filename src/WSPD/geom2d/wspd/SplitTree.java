package WSPD.geom2d.wspd;

import ProGAL.dataStructures.Pair;
import ProGAL.dataStructures.Set;
import WSPD.geom2d.BoundingBox;
import ProGAL.geom2d.Line;
import ProGAL.geom2d.Point;
import ProGAL.geom2d.PointSet;

import java.util.List;

public class SplitTree {

    private SplitTreeNode root;

    public SplitTree(Set<Point> points, BoundingBox rectangle) {
        root = calcSlowSplitTree(points, rectangle);
    }

    private SplitTreeNode calcSlowSplitTree(Set<Point> points, BoundingBox rectangle) {
        BoundingBox boundingBox = new BoundingBox(points);
        if (points.getSize() == 1){
            return new SplitTreeNode(boundingBox, points);
        } else {
            int i = boundingBox.getDimensionWithMaxLength();
            Line splitLine = boundingBox.getSplitLine(i);
            Pair<BoundingBox, BoundingBox> rectanglePair = rectangle.split(splitLine);
            BoundingBox R1 = rectanglePair.fst;
            BoundingBox R2 = rectanglePair.snd;

            Set<Point> S1 = new PointSet();
            Set<Point> S2 = new PointSet();
            for(Point p : points){
                if (R1.contains(p)){
                    S1.insert(p);
                } else {
                    S2.insert(p);
                }
            }

            SplitTreeNode v = calcSlowSplitTree(S1, R1);
            SplitTreeNode w = calcSlowSplitTree(S2, R2);

            return new SplitTreeNode(v, w, boundingBox, points);
        }
    }

    public List<SplitTreeNode> getAllInternalNodes() {
        return root.getAllInternalNodes();
    }
}
