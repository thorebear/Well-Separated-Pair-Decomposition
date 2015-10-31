package WSPD.geom2d.wspd;

import ProGAL.dataStructures.Pair;
import ProGAL.dataStructures.Set;
import ProGAL.geom2d.*;
import ProGAL.geom2d.Point;
import ProGAL.geom2d.viewer.J2DScene;
import WSPD.geom2d.BoundingBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WellSeparatedPairDecomposition {
    Set<Pair<Set<Point>, Set<Point>>> WSPD;

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
        Circle c_v = new Circle(box_v.getLeftBottom(), box_v.getLeftTop(), box_v.getRightBottom());

        BoundingBox box_w = w.getBoundingBox();
        Circle c_w = new Circle(box_w.getLeftBottom(), box_w.getLeftTop(), box_w.getRightBottom());

        double distanceBetweenCircles = c_v.getCenter().distance(c_w.getCenter()) -
                (c_v.getRadius() + c_w.getRadius());

        double maxRadius = Math.max(c_w.getRadius(), c_v.getRadius());

        return distanceBetweenCircles >= s*maxRadius;
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

    public void toScene(J2DScene scene)
    {
        // Get list of unique colors (same amount of colors as pairs)
        int lowerLimit = 0x101010;
        int upperLimit = 0xE0E0E0;
        int diffBetweenColors = (upperLimit-lowerLimit)/WSPD.getSize();

        final List<Integer> colors = new ArrayList<>();
        for (int i=0; i<WSPD.getSize(); i++) {
            colors.add(lowerLimit + diffBetweenColors * i);
        }

        for (int i=0; i<WSPD.getSize(); i++){
            Pair<Set<Point>, Set<Point>> pair = WSPD.get(i);
            Color color = new Color(colors.get(i));

            Circle circleFst, circleSnd;

            // Draw circle for the first set in the pair
            if (pair.fst.getSize() > 1) {
                BoundingBox box = new BoundingBox(pair.fst);
                circleFst = new Circle(box.getLeftBottom(), box.getLeftTop(), box.getRightBottom());
                circleFst.toScene(scene, color);
            } else {
                Point p = pair.fst.get(0);
                circleFst = new Circle(p, 0.01);
                scene.addShape(circleFst, color, 0, true);
            }

            // Draw circle for the second set in the pair
            if (pair.snd.getSize() > 1) {
                BoundingBox box = new BoundingBox(pair.snd);
                circleSnd = new Circle(box.getLeftBottom(), box.getLeftTop(), box.getRightBottom());
                circleSnd.toScene(scene, color);
            } else {
                Point p = pair.snd.get(0);
                circleSnd = new Circle(p, 0.01);
                scene.addShape(circleSnd, color, 0, true);
            }

            // Draw line between the two circles
            Vector vector = new Vector(circleFst.getCenter(), circleSnd.getCenter());
            vector = vector.scaleToLength(circleFst.getRadius());
            Point fstPointOnCircle = circleFst.getCenter().add(vector);
            vector.negative();
            vector = vector.scaleToLength(circleSnd.getRadius());
            Point sndPointOnCircle = circleSnd.getCenter().add(vector);
            scene.addShape(new LineSegment(fstPointOnCircle, sndPointOnCircle), color, 0.005, false);
        }

    }

    public void addTSpannerToScene(J2DScene scene){
        for(Pair<Set<Point>,Set<Point>> pair : WSPD){
            Set<Point> A = pair.fst;
            Set<Point> B = pair.snd;

            Point p_a = A.get((new Random()).nextInt(A.getSize()));
            Point p_b = B.get((new Random()).nextInt(B.getSize()));

            scene.addShape(new LineSegment(p_a, p_b), Color.BLACK, 0.005);
        }
    }
}
