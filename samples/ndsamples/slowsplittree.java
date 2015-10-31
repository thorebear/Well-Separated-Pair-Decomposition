package ndsamples;

import ProGAL.dataStructures.Set;
import ProGAL.geomNd.Point;


public class slowsplittree {

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 3) {
            System.out.println("Please add the following arguments: num points, num dimensions, separation factor");
        } else {
            int numPoints = Integer.parseInt(args[0]);
            int dimensions = Integer.parseInt(args[1]);
            double separationFactor = Double.parseDouble(args[2]);
            testNDimensionWSPD(numPoints, dimensions, separationFactor);
        }

    }

    private static void testNDimensionWSPD(int numPoints, int dimension, double separationFactor) {
        //Generate points
        Set<Point> points = new Set<>();
        for (int i = 0; i < numPoints; i++) {
            double[] coords = new double[dimension];
            for (int j = 0; j < dimension; j++){
                coords[j] = Math.random();
            }
            points.insert(new Point(coords));
        }

        System.out.println("Computing split tree with O(n^2) algorithm for " + numPoints +
                " points in " + dimension + " dimensions....");
        // Compute split tree with O(n log n) algorithm
        long startTime = System.nanoTime();
        WSPD.geomNd.wspd.SplitTree splitTree =
                new WSPD.geomNd.wspd.SplitTree(points, new WSPD.geomNd.BoundingBox(points), false);
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Computed split tree in " + estimatedTime / 1000000 + " milliseconds");
        // Compute the Well-Separated Pair Decomposition
        System.out.println("Computing WSPD for s=" + separationFactor);
        startTime = System.nanoTime();
        WSPD.geomNd.wspd.WellSeparatedPairDecomposition wspd
                = new WSPD.geomNd.wspd.WellSeparatedPairDecomposition(splitTree, separationFactor);
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("Computed WSPD of size " + wspd.WSPD.getSize() + " in " + estimatedTime / 1000000 + " milliseconds");
    }
}
