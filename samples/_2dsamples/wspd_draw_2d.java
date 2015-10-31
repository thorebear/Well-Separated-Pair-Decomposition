package _2dsamples;

import ProGAL.dataStructures.Set;
import ProGAL.geom2d.PointSet;
import ProGAL.geom2d.viewer.J2DScene;
import WSPD.geom2d.BoundingBox;
import WSPD.geom2d.wspd.SplitTree;
import WSPD.geom2d.wspd.WellSeparatedPairDecomposition;


public class wspd_draw_2d {

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 2) {
            System.out.println("Please add the following arguments: num points, separation factor");
        } else {
            int numPoints = Integer.parseInt(args[0]);
            double separationFactor = Double.parseDouble(args[1]);
            test2DimensionWSPD(numPoints, separationFactor);
        }

    }

    private static void test2DimensionWSPD(int numPoints, double separationFactor) {
        Set<ProGAL.geom2d.Point> points = new PointSet();
        for (int i = 0; i < numPoints; i++)
            points.insert(new ProGAL.geom2d.Point(Math.random(), Math.random()));


        //Display them
        J2DScene scene = J2DScene.createJ2DSceneInFrame();

        BoundingBox box = new BoundingBox(points);

        SplitTree tree = new SplitTree(points, box);
        //tree.toScene(scene);


        WellSeparatedPairDecomposition wspd
                = new WellSeparatedPairDecomposition(tree, separationFactor);
        wspd.toScene(scene);

        scene.setZoomFactor(400);
        scene.centerCamera();
    }
}
