# Well Separated Pair Decomposition
A java application for computing a Well Separated Pair Decomposition (WSPD). The implemented algorithms are based on algorithms presented by Giri Narasimhan and Michael Smid in "Geometri Spanner Networks".
The applications use the Java library ProGAL (http://www.diku.dk/~rfonseca/ProGAL/) to geometry computations and to draw a Well Separated Pair Decomposition in 2D. 

Building
--------
Requires Java 1.8. To build the application, clone the repository and run:
```
mkdir out
javac -d out -cp ./lib/ProGAL.jar @source.txt 
```
in the root of the repository. This will create an 'out'-folder, which will contain the compiled files. 


Run samples
-----------
The repository have 3 sample files included, which shows have to use the algorithms.

__samples/ndsamples/fastsplittree.java:__
Will compute a WSPD using the fast split tree algorithm, for a set of random points. After building the application, this sample can be executed by:
```
java -classpath ".\out;.\lib\ProGAL.jar" ndsamples.fastsplittree <number of points> <number of dimensions> <separation factor>
```
assuming you are in the root of the repository.

__samples/ndsamples/slowsplittree.java:__
Will compute a WSPD using the slow split tree algorithm, for a set of random points. After building the application, this sample can be executed by:
```
java -classpath ".\out;.\lib\ProGAL.jar" ndsamples.slowsplittree <number of points> <number of dimensions> <separation factor>
```
assuming you are in the root of the repository.

__samples/_2dsamples/wspd_draw_2d.java:__
Will compute a WSPD for a set of random points in 2D and illustrate it using ProGAL. After building the application, this sample can be executed by:
```
java -classpath ".\out;.\lib\ProGAL.jar" _2dsamples.wspd_draw_2d <number of points> <separation factor>
```
assuming you are in the root of the repository.
