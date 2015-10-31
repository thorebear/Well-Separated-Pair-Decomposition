package WSPD.geomNd.wspd;

import ProGAL.dataStructures.*;
import WSPD.dataStructures.SortToolPointInDimension;
import WSPD.geomNd.BoundingBox;
import ProGAL.geomNd.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitTree {

    private SplitTreeNode root;

    public SplitTree(Set<Point> points, BoundingBox rectangle, boolean fastAlgorithm) {
        if (!fastAlgorithm) {
            root = calcSlowSplitTree(points, rectangle);
        } else {
            root = calcFastSplitTree(points, rectangle);
        }
    }

    private SplitTreeNode calcFastSplitTree(Set<Point> points, BoundingBox rectangle) {
        if (points.getSize() == 1)
            return calcSlowSplitTree(points, rectangle);

        int dimension = points.get(0).getDimensions();

        Map<Integer,Pair<PointWrapper, PointWrapper>> LS = new HashMap<>();
        // Pre-processing step

        Map<Integer, Set<PointWrapper>> tempLS = new HashMap<>();
        // 1: Make the list for points
        for(int d = 0; d < dimension; d++){
            Set<PointWrapper> points_d = new Set<>();
            for(int pi = 0; pi < points.getSize(); pi++){
                Point p = points.get(pi);
                PointWrapper pw = new PointWrapper(p);
                points_d.insert(pw);

                // Insert pw as cross pointer in the same 'point' in all the other lists (which are initilized)
                for(int di = 0; di < d; di++){
                    PointWrapper other = tempLS.get(di).get(pi);
                    // Add cross pointer in both directions:
                    other.CrossPointers.put(d, pw);
                    pw.CrossPointers.put(di, other);
                }
            }

            tempLS.put(d, points_d);
        }

        // sorting
        for(int d = 0; d < dimension; d++) {
            Set<PointWrapper> points_d = tempLS.get(d);
            SorterQuick sorter = new SorterQuick();
            sorter.Sort(points_d, new SortToolPointInDimension(d));

            // When the list is sorted, we can set the prev/next information
            for(int pi = 0; pi < points_d.getSize(); pi++){
                if (pi > 0) {
                    points_d.get(pi).Prev = points_d.get(pi - 1);
                }
                if (pi < points_d.getSize() - 1) {
                    points_d.get(pi).Next = points_d.get(pi + 1);
                }
            }

            // Update the lists in LS
            tempLS.put(d, points_d);
        }

        // Now that we have sorted the list, we doesn't need the sets, because the pointers to prev and next is created
        for(int d = 0; d < dimension; d++) {
            LS.put(d, new Pair<>(tempLS.get(d).getFirst(), tempLS.get(d).getLast()));
        }

        PartialSplitTree partialSplitTree = new PartialSplitTree(rectangle, LS);
        for(PartialSplitTree.PartialSplitTreeNode leaf : partialSplitTree.getLeaves(partialSplitTree.root)){
            computePartialSplitTreeWithoutPrepossessing(leaf);
        }

        return partialSplitTree.toSplitTree(partialSplitTree.root);
    }

    private void computePartialSplitTreeWithoutPrepossessing(PartialSplitTree.PartialSplitTreeNode leaf) {
        // Test if n = 1!
        if (leaf.getLS().get(0).fst.Next == null){
            leaf.setBoundingBox(new BoundingBox(leaf.getPoints()));
            //leaf.points = null;
            //leaf.addPoint(leaf.getLS().get(0).fst.point);
        } else {
            PartialSplitTree leafPartialSplitTree = new PartialSplitTree(leaf.rectangle, leaf.getLS());
            leaf.leftChild = leafPartialSplitTree.root.leftChild;
            leaf.rightChild = leafPartialSplitTree.root.rightChild;
            for(PartialSplitTree.PartialSplitTreeNode l : leafPartialSplitTree.getLeaves(leafPartialSplitTree.root)){
                computePartialSplitTreeWithoutPrepossessing(l);
            }
        }
    }

    private SplitTreeNode calcSlowSplitTree(Set<Point> points, BoundingBox rectangle) {
        BoundingBox boundingBox = new BoundingBox(points);
        if (points.getSize() == 1){
            return new SplitTreeNode(boundingBox, rectangle, points);
        } else {
            int i = boundingBox.getDimensionWithMaxLength();
            double whereToSplit = boundingBox.getMiddleInDimension(i);
            Pair<BoundingBox, BoundingBox> rectanglePair = rectangle.split(i, whereToSplit);
            BoundingBox R1 = rectanglePair.fst;
            BoundingBox R2 = rectanglePair.snd;

            Set<Point> S1 = new Set<>();
            Set<Point> S2 = new Set<>();
            for(Point p : points){
                if (R1.contains(p)){
                    S1.insert(p);
                } else {
                    S2.insert(p);
                }
            }

            SplitTreeNode v = calcSlowSplitTree(S1, R1);
            SplitTreeNode w = calcSlowSplitTree(S2, R2);

            return new SplitTreeNode(v, w, boundingBox, rectangle, points);
        }
    }

    public List<SplitTreeNode> getAllInternalNodes() { return root.getAllInternalNodes(); }

    public class PointWrapper {
        private Point point;
        public PointWrapper Prev;
        public PointWrapper Next;
        private Map<Integer, PointWrapper> CrossPointers;
        public PointWrapper Copy;
        public PointWrapper Original;
        private PartialSplitTree.PartialSplitTreeNode Node;

        public PointWrapper(Point p) {
            this.point = p;
            this.CrossPointers = new HashMap<>();
        }

        public Point getPoint(){
            return point;
        }

        public PointWrapper getCleanCopy(){
            PointWrapper pw = new PointWrapper(new Point(point.getCoords().clone()));
            this.Copy = pw;
            pw.Original = this;
            return pw;
        }

        public void setNode(PartialSplitTree.PartialSplitTreeNode n){
            if (this.Node != null)
                throw new IllegalArgumentException("This PointWrapper already points to a node");

            this.Node = n;
        }

        public void deleteInList(Map<Integer, Pair<PointWrapper, PointWrapper>> LS, int i) {
            // To delete the point wrapper in the list, we set fix the prev next and next prev:
            if (this.Prev != null) {
                this.Prev.Next = this.Next;
            } else {
                // If we delete the first point, we must update the pointer to the first:
                LS.put(i, new Pair<>(this.Next, LS.get(i).snd));
            }
            if (this.Next != null) {
                this.Next.Prev = this.Prev;
            } else {
                // If we delete the last point, we must update the pointer to the last point:
                LS.put(i, new Pair<>(LS.get(i).fst, this.Prev));
            }
        }
    }

    private class PartialSplitTree {

        private PartialSplitTreeNode root;

        public PartialSplitTree(BoundingBox rectangle, Map<Integer, Pair<PointWrapper, PointWrapper>> LS) {

            Set<Point> points = new Set<>();
            PointWrapper fs = LS.get(0).fst;
            points.insert(fs.getPoint());
            while (fs.Next != null) {
                points.insert(fs.Next.getPoint());
                fs = fs.Next;
            }

            int dimension = points.get(0).getDimensions();

            /// STEP 1: ///
            int n = points.getSize();
            int size = n;

            PartialSplitTreeNode u = new PartialSplitTreeNode(rectangle);
            root = u;

            Map<Integer, Pair<PointWrapper, PointWrapper>> CLS = new HashMap<>();

            /* COPY TO CLS */
            for (int d = 0; d < dimension; d++) {
                // Set<PointWrapper> CLS_d = new Set<PointWrapper>();
                //Set<PointWrapper> LS_d = LS.get(d);
                PointWrapper firstInLS_d = LS.get(d).fst;

                PointWrapper firstInCLS_d = firstInLS_d.getCleanCopy();

                // Copy all points in d'th dimension, so we have them in CLS
                // (notice that we do not establish cross pointers, we instead use the originals cross-pointers)
                PointWrapper p = firstInCLS_d;
                while (p.Original.Next != null) {
                    PointWrapper cls_next = p.Original.Next.getCleanCopy();
                    p.Next = cls_next;
                    cls_next.Prev = p;

                    p = cls_next;
                }

                // When the loop stops, p must point to the last point in CLS_d:
                CLS.put(d, new Pair<>(firstInCLS_d, p));
            }

            while (size > n / 2) {
                /// STEP 3: ///
                points = new Set<>();
                fs = LS.get(0).fst;
                points.insert(fs.getPoint());
                while (fs.Next != null) {
                    points.insert(fs.Next.getPoint());
                    fs = fs.Next;
                }

                BoundingBox boundingBox_u = new BoundingBox(points);
                u.setBoundingBox(boundingBox_u);
                u.setPoints(points);
                int i = boundingBox_u.getDimensionWithMaxLength();
                double middle = boundingBox_u.getMiddleInDimension(i);
                PointWrapper p = LS.get(i).fst;
                PointWrapper p_prime = p.Next;
                PointWrapper q = LS.get(i).snd;
                PointWrapper q_prime = q.Prev;

                int size_prime = 1;
                while (p_prime.getPoint().getCoord(i) <= middle
                        && q_prime.getPoint().getCoord(i) >= middle) {
                    p = p_prime;
                    p_prime = p.Next;
                    q = q_prime;
                    q_prime = q.Prev;
                    size_prime++;
                }

                Pair<BoundingBox, BoundingBox> pair = boundingBox_u.split(i, middle);
                BoundingBox rectangle_v = pair.fst;
                BoundingBox rectangle_w = pair.snd;

                PartialSplitTreeNode v = new PartialSplitTreeNode(rectangle_v);
                PartialSplitTreeNode w = new PartialSplitTreeNode(rectangle_w);

                u.setLeftChild(v);
                u.setRightChild(w);

                if (p_prime.getPoint().getCoord(i) >= middle) {
                    /// STEP 4: ///
                    boolean p_encountered = false;
                    PointWrapper z = LS.get(i).fst;
                    while (!p_encountered) {
                        for (PointWrapper pw : z.CrossPointers.values()) {
                            pw.Copy.setNode(v);
                        }

                        z.Copy.setNode(v);

                        // We have to go to sizes + 1, because we don't have a cross pointers to the dimension
                        // we are in.
                        for (int di = 0; di < z.CrossPointers.size() + 1; di++) {
                            if (di == i)
                                continue;

                            PointWrapper crossPointer = z.CrossPointers.get(di);
                            crossPointer.deleteInList(LS, di);
                        }

                        z.deleteInList(LS, i);

                        if (z.equals(p)) {
                            p_encountered = true;
                        } else {
                            z = z.Next;
                        }

                    }

                    u = w;
                    size = size - size_prime;

                } else {
                    /// STEP 5: ///
                    boolean q_encountered = false;
                    PointWrapper z = LS.get(i).snd;
                    while (!q_encountered) {

                        for (PointWrapper pw : z.CrossPointers.values()) {
                            pw.Copy.setNode(w);
                        }

                        z.Copy.setNode(w);

                        // We have to go to sizes + 1, because we don't have a cross pointers to the dimension with
                        // we are in.
                        for (int di = 0; di < z.CrossPointers.size() + 1; di++) {
                            if (di == i)
                                continue;

                            PointWrapper crossPointer = z.CrossPointers.get(di);
                            crossPointer.deleteInList(LS, di);
                        }

                        z.deleteInList(LS, i);

                        if (z.equals(q)) {
                            q_encountered = true;
                        } else {
                            z = z.Prev;
                        }
                    }

                    u = v;

                    size = size - size_prime;
                }
            }

            /// STEP 2: (size is now < n/2) ///
            for (int d = 0; d < dimension; d++) {
                PointWrapper p = LS.get(d).fst;
                p.Copy.setNode(u);
                while (p.Next != null) {
                    p = p.Next;
                    p.Copy.setNode(u);
                }
            }

            /// STEP 6: ///
            List<PartialSplitTreeNode> leaves = getLeaves(root);

            Map<PartialSplitTreeNode, Map<Integer, Set<PointWrapper>>> LS_leafs = new HashMap<>();
            for (PartialSplitTreeNode leaf : leaves) {
                Map<Integer, Set<PointWrapper>> LS_leaf = new HashMap<>();
                for (int di = 0; di < dimension; di++) {
                    LS_leaf.put(di, new Set<>());
                }
                LS_leafs.put(leaf, LS_leaf);
            }

            for (int d = 0; d < dimension; d++) {
                PointWrapper pw = CLS.get(d).fst;
                Map<Integer, Set<PointWrapper>> LS_leaf = LS_leafs.get(pw.Node);
                LS_leaf.get(d).insert(pw.Original);
                pw.Original.Copy = null;
                pw.Original.Next = null;
                pw.Original.Prev = null;

                while (pw.Next != null) {
                    pw = pw.Next;
                    LS_leaf = LS_leafs.get(pw.Node);
                    LS_leaf.get(d).insert(pw.Original);
                    pw.Original.Copy = null;
                    pw.Original.Next = null;
                    pw.Original.Prev = null;
                }
            }

            for (Map<Integer, Set<PointWrapper>> LS_leaf : LS_leafs.values()) {
                for (int d = 0; d < dimension; d++) {
                    Set<PointWrapper> points_d = LS_leaf.get(d);

                    for (int pi = 0; pi < points_d.getSize(); pi++) {
                        if (pi > 0) {
                            points_d.get(pi).Prev = points_d.get(pi - 1);
                        }
                        if (pi < points_d.getSize() - 1) {
                            points_d.get(pi).Next = points_d.get(pi + 1);
                        }
                    }
                }
            }

            // finally for each leaf compute the bounding box
            for (int leaf_index = 0; leaf_index < leaves.size(); leaf_index++) {
                PartialSplitTreeNode leaf = leaves.get(leaf_index);
                Map<Integer, Set<PointWrapper>> LS_leaf = LS_leafs.get(leaf);
                Map<Integer, Pair<PointWrapper, PointWrapper>> LS_leaf_endpoints = new HashMap<>();

                // Since the points are sorted, we only need the first and the last point in each dimension,
                // to compute the bounding box:
                Set<Point> pointsNeedForBB = new Set<>();
                for (int d = 0; d < dimension; d++) {
                    pointsNeedForBB.insert(LS_leaf.get(d).getFirst().getPoint());
                    pointsNeedForBB.insert(LS_leaf.get(d).getLast().getPoint());
                    LS_leaf_endpoints.put(d, new Pair<>(LS_leaf.get(d).getFirst(), LS_leaf.get(d).getLast()));
                }
                leaf.setLS(LS_leaf_endpoints);

                points = new Set<>();
                fs = LS_leaf_endpoints.get(0).fst;
                points.insert(fs.getPoint());
                while (fs.Next != null) {
                    points.insert(fs.Next.getPoint());
                    fs = fs.Next;
                }
                leaf.setPoints(points);
                leaf.setBoundingBox(new BoundingBox(pointsNeedForBB));
            }
        }

        private List<PartialSplitTreeNode> getLeaves(PartialSplitTreeNode root) {
            List<PartialSplitTreeNode> leaves = new ArrayList<>();

            List<PartialSplitTreeNode> layer = new ArrayList<>();
            layer.add(root);
            while(!layer.isEmpty()) {
                List<PartialSplitTreeNode> newLayer = new ArrayList<>();
                for (PartialSplitTreeNode node : layer) {
                    if (node.leftChild != null){
                        newLayer.add(node.leftChild);
                    }
                    if (node.rightChild != null){
                        newLayer.add(node.rightChild);
                    }

                    if (node.leftChild == null && node.rightChild == null){
                        leaves.add(node);
                    }
                }

                layer = newLayer;
            }

            return leaves;
        }

        public SplitTreeNode toSplitTree(PartialSplitTreeNode p_node) {
            if (p_node.boundingBox == null){
                throw new RuntimeException("Should have bounding box defined");
            }
            SplitTreeNode node = new SplitTreeNode(p_node.boundingBox, null, p_node.getPoints());

            if (p_node.leftChild != null){
                if (p_node.leftChild.boundingBox == null){
                    throw new RuntimeException("Child should have bounding box defined");
                }
                node.setLeftChild(this.toSplitTree(p_node.leftChild));
            }

            if (p_node.rightChild != null){
                if (p_node.rightChild.boundingBox == null){
                    throw new RuntimeException("Child should have bounding box defined");
                }
                node.setRightChild(this.toSplitTree(p_node.rightChild));
            }
            return node;
        }

        private class PartialSplitTreeNode {
            private final BoundingBox rectangle;
            private PartialSplitTreeNode rightChild;
            private PartialSplitTreeNode leftChild;
            private BoundingBox boundingBox;
            private Map<Integer, Pair<PointWrapper, PointWrapper>> LS;
            private Set<Point> points;

            public PartialSplitTreeNode(BoundingBox rectangle) {
                this.rectangle = rectangle;
            }

            public void setLeftChild(PartialSplitTreeNode leftChild) {
                this.leftChild = leftChild;
            }

            public void setRightChild(PartialSplitTreeNode rightChild) {
                this.rightChild = rightChild;
            }

            public void setBoundingBox(BoundingBox boundingBox) {
                this.boundingBox = boundingBox;
            }

            public void setLS(Map<Integer, Pair<PointWrapper,PointWrapper>> ls){
                this.LS = ls;
            }

            public Map<Integer, Pair<PointWrapper,PointWrapper>> getLS(){
                return LS;
            }

            public void setPoints(Set<Point> points){
                this.points = points;
            }

            public Set<Point> getPoints(){
                if (points != null){
                    return points;
                } else {
                    Set<Point> set = new Set<>();
                    PointWrapper fs = LS.get(0).fst;
                    set.insert(fs.getPoint());
                    while (fs.Next != null) {
                        set.insert(fs.Next.getPoint());
                        fs = fs.Next;
                    }

                    return set;
                }
            }
        }
    }
}
