This was an old project I played with in gradschool and imported from google code before it shutdown.

# JQCADesigner

## What is JQCADesigner?
A Java implementation of the [QCADesigner](http://www.mina.ubc.ca/qcadesigner) tool by the [University of British Columbia](http://www.ubc.ca/).  The current focus is on providing more robust command line functionality. The project is 100% compatible with QCADesigner in that all files use the same format.

Some more recent work has been done at UBC to improve the performance of QCADesigner by using NVIDIA's CUDA technology, which is really interesting.  You can see more on that specific project [here](http://code.google.com/p/qcadesigner-cuda/). The primary goal if this project is not performance, but it is a close secondary goal.

## Why?
Personally, I find the original QCADesigner code a bit messy, and it's not easy to extend or modify.  After running into multiple layers of segfaults when trying to add some simple command line functionality, I decided to start this project. The primary goal is to provide a more maintainable, extensible, and reliable version of QCADesigner.  A close secondary goal is to provide as good if not better performance.

## Why Java?
The question is: Why _not_ Java! The heavy focus on object oriented programming helps in writing more maintainable and extensible code, which is incredibly important when designing large and complex systems.  Java is also easier to debug since more run-time checking is performed. That means no segfaults! This helps in writing reliable code. Memory leaks are also not an issue, since memory management is handled by Java's garbage collector.  The short answer is: With Java, it's just easier to write safer and more reliable code.

In addition, it's extremely easy to write Jython bindings for Java programs.  This makes writing automation scripts and utilities a snap.  See JythonBindings for more information.

### What about performance?
There have been a number of benchmarks that show that Java is definitely a strong competitor to C/C++ when it comes to performance. There are even situations where Java may outperform C/C++ due to run-time optimizations that static compilers can't take advantage of.  CUDA bindings for Java are also in development that could help boost performance. Although there are no current plans to include CUDA support, it is a possibility.

The bottom line is that even if some performance is lost, it's a small price to pay for a stable, reliable and maintainable application.
