# Well Separated Pair Decomposition
A java application for computing a Well Separated Pair Decomposition (WSPD). The implemented algorithms are based on algorithms presented by Giri Narasimhan and Michael Smid in "Geometri Spanner Networks".
The applications use the Java library ProGAL (http://www.diku.dk/~rfonseca/ProGAL/) to geometry computations and to draw a Well Separated Pair Decomposition in 2D. 

Building
--------
Requires Java and Apache Ant. To build the application, clone the repository and run:
```
ant all
```
in the root of the repository. This will create an 'out'-folder, which contains the compiled files. 


Run samples
-----------
The repositry have 3 sample files included, which shows have to use the application.

1. samples/ndsamples/fastsplittree.java:
Will compute a WSPD using the fast split tree algorithm, for a set of random points. After building the application, this sample can be executed by:
```
java -classpath ".\out;.\lib\ProGAL.jar" ndsamples.fastsplittree <number of points> <number of dimensions> <separation factor>
```
assuming you are in the root of the repository.

2. samples/ndsamples/slowsplittree.java:
Will compute a WSPD using the slow split tree algorithm, for a set of random points. After building the application, this sample can be executed by:
```
java -classpath ".\out;.\lib\ProGAL.jar" ndsamples.slowsplittree <number of points> <number of dimensions> <separation factor>
```

3. samples/_2dsamples/wspd_draw_2d.java:
Will compute a WSPD for a random points in 2D and illustrate it using ProGAL. After building the application, this sample can be executed by:
```
java -classpath ".\out;.\lib\ProGAL.jar" _2dsamples.wspd_draw_2d <number of points> <separation factor>
```
