package WSPD.dataStructures;

import ProGAL.dataStructures.SortTool;
import WSPD.geomNd.wspd.SplitTree;

public class SortToolPointInDimension implements SortTool {
    private final int dimension;

    public SortToolPointInDimension(int d) {
        this.dimension = d;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if(o1 instanceof SplitTree.PointWrapper && o2 instanceof SplitTree.PointWrapper) {
            double n1 = ((SplitTree.PointWrapper)o1).getPoint().getCoord(dimension);
            double n2 = ((SplitTree.PointWrapper)o2).getPoint().getCoord(dimension);
            return n1 < n2?-1:(n1 > n2?1:0);
        } else {
            throw SortTool.err1;
        }
    }
}
