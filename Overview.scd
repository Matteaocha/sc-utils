/*

CONTROL STRUCTURES

if ( cond , {true func}, {false func} )
while ( cond, {do func} )
for ( startVal, stopVal, { |val| do func} )
do ( collection, { |val| do func} )

-----------------------------------------------------

RANDOM

[1, 2, 3].choose
linrand(10, 20) (evaluated locally)
LinRand(10, 20) (evaluated on the server when the UGen is created)
LFNoise[0/1/2] (Jumps to random values in step, linear, or exponential flow)

------------------------------------------------------

DICTIONARY
- d = Dictionary.new;
- d.add( \key -> val )
- d.put( "key", val )
- d.at(\key)  d[\key]

ARRAY
- []
- Array.fill(size, { |i| fillFunc })

------------------------------------------------------

BLOCKS/FUNCS {}
- Return the value on the last line (like ruby)
- .value runs the function
- .play passes the last value into an Out.ar() object and creates a node

ABSTRACTFUNCTION
- A function which is evaluated by the server at the point of instantiation (in a UGen)

ROUTINE (func)
- Runs a function in a new thread
- When an object is yielded by a function (1.yield), the thread is paused
- .next resumes the routine
- .play interprets yielded values as a length of time to wait until .next is called

STREAM
- an abstract concept
- anything with a .next method
- a stream returns itself when .asStream is called

FUNCSTREAM (func)
- Wraps a function so that it can be evaluated multiple times with .next

P____ (arrray, repeat)
- A pattern object, for making common routines
- Has an asStream method

PBIND ([] args)
- Uses patterns to create synths
- Takes objects with .asStream methods
- .play(quant: n) start playing on a given beat

EVENT
- A bundle of keys and values which are sent to the server as a UGen request
- (freq: 500, dur: 3, instrument: \blah).play

--------------------------------------------------------------

PATHNAME (string)
- Wraps a file object
- .entries/.files/.folders returns a list of entries (if it's a directory)
- .extension gets the file object
- ++ allows you to concatenate the PathName obejct to create a new one

FILE
- .open (pathname, mode) opens a file
- .read
- .write
- .close
- .getLine

NETADDR (host, port)
- .sendMsg send a message
- .disconnect

----------------------------------------------------------------

BUFFERS

- Buffer.read(server, pathname) creates a new buffer from a file, and returns a bufnum
- PlayBuf.ar(channels, bufnum, rate, trigger, startPos, loop, doneAction)
- Buffer.alloc(server, size, channels)
- b.play
- b.free
- b.sineFill(size, harmonicAmplitudeArray)

(See Signals for buffer filling)

----------------------------------------------------------------

ENVIRONMENTS

- a = Environment.make({ ~aLocalVar = 100 })
- a.use({ ~aLocalVar.postln })

----------------------------------------------------------------

CLASSES

ClassName : InheritsFrom {
    var localvar, <varWithGetter, >varWithSetter, <>setterGetter;

    *classMethod {
        ^returnObject;
    }

    instanceMethod {
        this.otherInstanceMethod();
    }

    otherInstanceMethod {}

    *new { |arg1, arg2| ^super.new.init(arg1, arg2); }

    init { |arg1, arg2| }
}

Add class paths in preferences!

----------------------------------------------------------------

SERVER EVENTS

ServerBoot.add/remove
ServerStart.add/remove
ServerTree.add/remove
CmdPeriod.add/remove    (clears the server tree)

Things sent to the server (like UGen add) are asynchronous, so you can't perform
actions on them immediately. Instead, do them in a routine with the s.sync method.

Routine({
    SynthDef(\ugen, {}).add;
    s.sync;
    Synth.new(\ugen);
})


---------------------------------------------------------------

PATHS

~moduleDir = PathName(thisProcess.nowExecutingPath).parentPath;

Place this in your module to get the directory of the file

"modulename.scd".loadPaths

This will execute the file. (As long as RUN) is placed in a comment at the top

*/