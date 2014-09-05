# Brief manual

## Commands
1. <code>bp \<class id\>:\<line\></code> Add breakpoint
1. <code>bp \<class id\>.\<method\>\[(argument_type,...)\]</code> Add breakpoint
1. <code>bpl</code> List of existing breakpoints
1. <code>bpd</code> Remove existing breakpoint by its number
1. <code>trace/tr methods \[thread\]</code> Set a trace point for moving into method and moving out method
1. <code>trl</code>List of trace points
1. <code>trd</code>Delete a trace point
1. <code>watch/wt \[access\|all\] \<class id\>.\<field name\></code> Set a watcher for access/modification of variable
1. <code>wtl</code>List of a watchers
1. <code>wtd</code>Delete watcher
1. <code>ct/catch \<exception name\></code> Will break whenever an Exception (or one of its derivatives) is thrown. Break when an exception of type class is generated
1. <code>ct/catch \[uncaught\|caught\|all\] \<class id\>\|\<class pattern\></code> Break when specified exception occurs
1. <code>ctl</code> List the exception based breakpoints currently in place
1. <code>ctd</code> Delete exception breakpoint
1. <code>st/step</code> Step though one line of source
1. <code>su/stepup</code> Execute until the current function/method returns
1. <code>si/stepi</code> Execute current instruction
1. <code>c/cont</code> Continue running the program until the next breakpoint is hit
1. <code>n/next</code> Step over one line of source. Executes one line of code, and then stops again. Note step steps into the code; i.e., executes the next source statement at some level, delving into method invocations instead of jumping over them like next does
1. <code>p/print \<var name\></code> Prints out the value of var. Print out the value of a variable. If var is an object, the toString method should be used. Classes are specified by either their object ID or by name. If a class is already loaded, a substring can be used, such as Thread for java.lang.Thread. Supports Java expressions
1. <code>set \<lvalue\> = \<expr\></code> Assign new value to field/variable/array element
1. <code>wh/where</code> Prints out the stack trace of the current thread. List the current execution stack, that is find out not only where you are, but why. The stack shows not only which method is currently running, but where this method was called from, where that method was called from, and so forth
1. <code>wha/where all</code> Dumps the stack of all threads in the current thread group
1. <code>wh/where \[\<thread id\>\|all\]</code> Dumps the stack of the specified thread or for all threads, or for current thread. The threadid takes the form of t@\<index\>, such as t@3
1. <code>whi/wherei \[\<thread id\>\|all\]</code> Dump a thread's stack, with pc info
1. <code>src</code> Will list the source code and show which line is about to be executed next
1. <code>src \[line number\|method\]</code> Print source code
1. <code>srcpath/sourcepath \[source file path\]</code> Display or change the source path
1. <code>dump \<var name\></code> Prints out the value of a variable, but in a more detailed format that does not rely on the toString method. Object references will appear by name and have their values (memory addresses) printed at hexidecimal values. Tells JBreakpoint to dump as much information as it can find about a variable, rather than calling toString.
1. <code>up</code> Move debugger scope up a stack frame. That is, have it show variables and position in the method that invoked this one. This command moves up the stack frame, so that you can use locals and print to examine the program at the point before the current method was called
1. <code>dn/down</code> This command moves down the stack frame to examine the program after the method call
1. <code>up \[n frames\]</code> Move up a thread's stack
1. <code>dn/down \[n frames\]</code> Move down a thread's stack
1. <code>load \<class id\></code> If you want to talk about (set breakpoints in) other classes which JBreakpoint hasn't yet seen
1. <code>classes</code> List known classes (including the default ones from the class library)
1. <code>class \<class id\></code> Show details of named class
1. <code>locals</code>List local variables and their values
1. <code>methods</code> Class list methods of class
1. <code>methods \<class id\></code> List a class's methods
1. <code>fields \<class id\></code> List a class's fields
1. <code>memory</code> This command shows the total amount of memory and the amount that isn't currently in use
1. <code>threadgroups</code> List threadgroups
1. <code>threadgroup \<name\></code> Set current threadgroup
1. <code>threads</code> This command lists the threads that are executing
1. <code>threads \[threadgroup\]</code> List threads
1. <code>thread \<thread id\></code> Set default thread
1. <code>suspend \[thread id(s)\]</code> Suspend threads (default: all)
1. <code>resume \[thread id(s)\]</code> Resume threads (default: all)
1. <code>kill \<thread id\> \<expr\></code> Kill a thread with the given exception object
1. <code>interrupt \<thread id\></code> interrupt a thread
1. <code>exclude \[\<class pattern\>, ... \| "none"\]</code> Do not report step or method events for specified classes
1. <code>classpath</code> Print classpath info from target VM
1. <code>monitor \<command\></code> Execute command each time the program stops
1. <code>monitor</code> List monitors
1. <code>unmonitor \<monitor#\></code> Delete a monitor
1. <code>read \<filename\></code> Read and execute a command file
1. <code>lock \<expr\></code> Print lock info for an object
1. <code>threadlocks \[thread id\]</code> Print lock info for a thread
1. <code>pop</code>Pop the stack through and including the current frame
1. <code>reenter</code> Same as pop, but current frame is reentered
1. <code>redefine \<class id\> \<class file name\></code> Redefine the code for a class
1. <code>disablegc \<expr\></code> Prevent garbage collection of an object
1. <code>enablegc \<expr\></code> Permit garbage collection of an object
1. <code>r</code> Repeat last command
1. <code>help/?</code> A summary of most common and helpful commands
1. <code>\<n\> \<command\></code> Repeat command n times
1. <code>version</code> Print version information
1. <code>exit/quit/bye</code> Exit debugger

### Command parameters description:
* <code>\<class id\></code> a full class id with package qualifiers
* <code>\<class pattern\></code> a class id with a leading or trailing wildcard ('*')
* <code>\<thread id\></code> thread number as reported in the 'threads' command
* <code>\<expr\></code> a Java(tm) Programming Language expression. Most common syntax is supported.

Some additional commands for JBreakpoint can be added to <code>.jbreakpointrc</code>. They will be executed on JBreakpoint startup.
