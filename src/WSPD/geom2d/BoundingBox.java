package WSPD.geom2d;

import ProGAL.dataStructures.Pair;
import ProGAL.dataStructures.Set;
import ProGAL.geom2d.*;
import ProGAL.geom2d.viewer.J2DScene;

import java.awt.*;

public class BoundingBox {

    private ProGAL.geom2d.Point leftTop, leftBottom, rightTop, rightBottom;

    public BoundingBox(ProGAL.geom2d.Point leftTop, ProGAL.geom2d.Point leftBottom,
                       ProGAL.geom2d.Point rightTop, ProGAL.geom2d.Point rightBottom)
    {
        this.leftTop = leftTop;
        this.leftBottom = leftBottom;
        this.rightTop = rightTop;
        this.rightBottom = rightBottom;
    }

    public BoundingBox(Set<ProGAL.geom2d.Point> points) {
        double leftBound, topBound, rightBound, bottomBound;
        if(points.isEmpty()){
            throw new IllegalArgumentException(
                    "Bounding box for empty point set is not defined");
        }

        // Use the first point to get init values
        leftBound = points.get(0).x();
        rightBound = points.get(0).x();
        topBound = points.get(0).y();
        bottomBound = points.get(0).y();

        // Iterates through the rest of the points, to find the bounds
        for(int i = 1; i < points.getSize(); i++){
            ProGAL.geom2d.Point p = points.get(i);
            leftBound = Math.min(p.x(), leftBound);
            rightBound = Math.max(p.x(), rightBound);
            bottomBound = Math.min(p.y(), bottomBound);
            topBound = Math.max(p.y(), topBound);
        }

        leftTop = new ProGAL.geom2d.Point(leftBound, topBound);
        leftBottom = new ProGAL.geom2d.Point(leftBound, bottomBound);
        rightTop = new ProGAL.geom2d.Point(rightBound, topBound);
        rightBottom = new ProGAL.geom2d.Point(rightBound, bottomBound);
    }

    public void toScene(J2DScene scene, Color col) {
        double thick = 0.003;
        scene.addShape(new LineSegment(leftTop, leftBottom), col, thick);
        scene.addShape(new LineSegment(rightTop, rightBottom), col, thick);
        scene.addShape(new LineSegment(leftTop, rightTop), col, thick);
        scene.addShape(new LineSegment(leftBottom, rightBottom), col, thick);
    }

    public int getDimensionWithMaxLength(){
        if ((rightTop.x() - leftTop.x()) > (leftTop.y() - leftBottom.y())){
            return 0;
        } else {
            return 1;
        }
    }

    public boolean contains(ProGAL.geom2d.Point p){
        return leftTop.x() <= p.x() && p.x() <= rightTop.x() && leftBottom.y() <= p.y() && p.y() <= leftTop.y();
    }

    public Line getSplitLine(int dimension) {
        if (dimension == 0){
            return new Line(new ProGAL.geom2d.Point((rightTop.x() + leftTop.x()) / 2, 0),
                    new Vector(1, 0.0D));
        } else {
            return new Line(new ProGAL.geom2d.Point(0, (leftTop.y() + leftBottom.y()) / 2),
                    new Vector(0.0D, 1));
        }
    }

    public Pair<BoundingBox, BoundingBox> split(Line splitLine) {
        if (splitLine.isVertical()){

            if (splitLine.getPoint().x() < leftBottom.x() ||
                    splitLine.getPoint().x() > rightBottom.x())
            {
                throw new IllegalArgumentException("The split line does not intersect with the bounding box");
            }


            BoundingBox left = new BoundingBox(
                leftTop,
                leftBottom,
                new ProGAL.geom2d.Point(splitLine.getPoint().x(), rightTop.y()),
                new ProGAL.geom2d.Point(splitLine.getPoint().x(), rightBottom.y())
            );

            BoundingBox right = new BoundingBox(
                new ProGAL.geom2d.Point(splitLine.getPoint().x(), leftTop.y()),
                new ProGAL.geom2d.Point(splitLine.getPoint().x(), leftBottom.y()),
                rightTop,
                rightBottom
            );

            return new Pair<>(left, right);

        } else {

            if (splitLine.getPoint().y() < leftBottom.y() ||
                    splitLine.getPoint().y() > leftTop.y())
            {
                throw new IllegalArgumentException("The split line does not intersect with the bounding box");
            }

            BoundingBox top = new BoundingBox(
                leftTop,
                new ProGAL.geom2d.Point(leftBottom.x(), splitLine.getPoint().y()),
                rightTop,
                new ProGAL.geom2d.Point(rightBottom.x(), splitLine.getPoint().y())
            );

            BoundingBox bottom = new BoundingBox(
                new ProGAL.geom2d.Point(leftTop.x(), splitLine.getPoint().y()),
                leftBottom,
                new ProGAL.geom2d.Point(rightTop.x(), splitLine.getPoint().y()),
                rightBottom
            );

            return new Pair<>(top, bottom);
        }
    }

    public ProGAL.geom2d.Point getLeftTop() { return leftTop; }
    public ProGAL.geom2d.Point getLeftBottom() { return leftBottom; }
    public ProGAL.geom2d.Point getRightBottom() { return rightBottom; }

    public double getMaxLength() {
        return Math.max((rightTop.x() - leftTop.x()), (leftTop.y() - leftBottom.y()));
    }
}
