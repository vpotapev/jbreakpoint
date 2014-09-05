# JBreakpoint

The JBreakpoint is a console debugger for JVM, which uses pseudographic interface.

Primary goal of this project is to have opportunity to debug a JVM remotely when we have a high latency network connection.

When an GUI-based IDE debuggers interoperates with remote servers, it receives a huge number of small data packets - current JVM context. The context consists a stack frames, "this"-variable of current class, information about threads and their parameters and so on. Most part of this information doesn't used by user while a debugging session, but it is displayed on IDE panels. Each data request eats some time because of high latency (hundreds of milliseconds in my case), and summary time for all request-response cycle is very significant.

The JBreakpoint IDE will work with JVM on its server locally without a graphical GUI, just use the console like original JDB does. So all traffic will be only the changes in console.

## Why Clojure, not Java

Because I'm sure that Clojure more efficient then Java. It's produces significantly shorter code (less code -> less bugs). The code is simple, universal and concise. If a java programm would be rewritten on Clojure, it will be shorter and simpler for support.

Yes, currently a Java code execution speed usually in 1.5-2 times faster than the same code on Clojure. But for 95% of code it's not a problem.

## When

First working release planned in the November (it depends on the load on my main job).

## Installation

Checkout sources from the repo. Then type <code>lein uberjar</code> to build standalone jar-file.

## Usage

    $ java -jar jbreakpoint-x.x.x-standalone.jar [args]

The <code>.jbreakpointrc</code> file will be loaded and executed after start. This file contains code for initialization.

## Options

* --attach <port> - Attached the debugger to JVM port
* --scr-port <port> - Opened the port which can be used by user client which will provide code sources to the debugger

## Examples

TODO:

## Might be Useful

TODO:

## Roadmap

1. Implementation of the JDB functionality
1. Pseudographic GUI
1. Code sources displaying support
1. Scripting features

And many-many more...

## License

JBreakpoint application is distributed under GNU General Public License, Version 3 
For the terms of this license, see licenses/gpl_v3.md or <http://www.gnu.org/licenses/>.

Copyright © 2014 Vladimir Potapev

You are free to use this application under the terms of the GNU General
Public License, but WITHOUT ANY WARRANTY; without even the implied 
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.
