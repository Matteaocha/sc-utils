(
SynthDef(\Twinkle, { arg freq=220, amp=0.5, modHarm=7, modDev=2, modAmp=5, atk=1, hld=3, rel=2, out=0;

	var freqarray, sig, mod, sigenv, filterenv, passenv, lfo, freqenv;

	freqarray = [1/2, 1, 7.midiratio, 2]*freq;

	mod = SinOsc.ar(freqarray*(modHarm+(0.02*modDev)), amp*0.3*{LinRand(0.2, 0.5)}!4*modAmp);
	sig = SinOsc.ar(freqarray, mod)*amp*0.3*[0.5, 1.0, 0.7, 0.4];

	sigenv = EnvGen.kr(Env.perc(releaseTime: 2), doneAction: 2);

	sig = sig*sigenv;
	sig = Mix.new(sig);

	filterenv = SinOsc.kr(3, mul: 0.3, add:1.0)*sigenv;
	sig = HPF.ar(sig, freq*1.5*filterenv);

	sig = Pan2.ar(sig, Rand(-1.0, 1.0));

	Out.ar(out, sig);
}
).add

)

//------------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------------------------

Synth(\Twinkle, [\freq, 55, \amp, 0.2])


(
Pbind(
	\instrument, \Twinkle,
	\degree, Prand([0, 1, 2, 3, 4, 5], inf),
	\octave, Prand([0, 1, 2] + 5, inf),
	\root, -10,
	\dur, Pwrand([0.1, 0.2, 0.3, 0.4], [0.2, 0.2, 0.3, 0.3], inf),
	\amp, 0.01,
	\modHarm, 3,
	\modDev, 1
).play
)


s.quit