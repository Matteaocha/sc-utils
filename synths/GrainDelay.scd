(
SynthDef(\GrainDelay, {|in, amp=1, len=1, pitch=1, rate=30, grainSize=0.1, stereoWidth=0.6, out=0|
	var buf, buflen, clk, pan, winLen, winStartMin, winStartMax, winStart, sig;

	buflen = 44100 * len;

	buf = LocalBuf(buflen);
	RecordBuf.ar(In.ar(in), buf);

	clk = Impulse.kr(rate);
	pan = WhiteNoise.kr(stereoWidth, -1*(stereoWidth/2));
	winLen = len * grainSize;
	winStartMin = winLen/2;
	winStartMax = len - (winLen/2);
	winStart = TRand.kr(winStartMin, winStartMax, clk);

	sig = TGrains.ar(2, clk, buf, pitch, winStart, winLen, pan, 0.5, 2);
	Out.ar(out, sig * amp);
}).add;
)

(
SynthDef(\Sawy, {|freq=100, amp=0.3, out=0|
	var sig, env;

	sig = LFSaw.ar(freq)*amp;

	Out.ar(out, sig);
}).add;
)


~buf = Buffer.new(s, 44100)

b = Bus.audio(s, 1);

g = Synth.new(\GrainDelay, [\in, b, \pitch, 2, \buf, ~buf])
g.set(\grainSize, 0.8)

Synth.new(\Sawy, [\out, b])





(
SynthDef(\RecSaw, {|buf, freq=100, amp=0.3, out=0|
	var sig, buffer;

	buffer = LocalBuf(44100);
	sig = LFSaw.ar(freq)*amp;
	RecordBuf.ar(sig, buffer);

	Out.ar(out, PlayBuf.ar(1, buffer, loop: 1));
}).add;
)
Synth.new(\RecSaw, [\buf, ~buf])
{PlayBuf.ar(1, ~buf, loop: 1)}.play
~buf