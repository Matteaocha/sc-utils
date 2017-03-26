/*RUN*/

~bagpipeBlast = Dictionary.new;
~bagpipeBlast.put("path", PathName(thisProcess.nowExecutingPath).parentPath);
~bagpipeBlast.put("chorus", Buffer.read(s, ~bagpipeBlast["path"] ++ "chorus.wav"));
~bagpipeBlast.put("solo", Buffer.read(s, ~bagpipeBlast["path"] ++ "solo.wav"));

(
SynthDef(\BagpipeBlast, {
	arg freq=440, amp=0.5, bend=0, pos=0.5, atkTime=0.1,
	relTime=10.0, hpf=0.6, vibHz=2, winMul=1.0, out=0;

	var sig, env, ratio, trate, clk, pan, soloStart, chorusStart,
	soloSig, chorusSig, winLen, vib, viblfo, chorusBuf, soloBuf,
	triSig, triSigEnv;

	chorusBuf = ~bagpipeBlast["chorus"];
	soloBuf = ~bagpipeBlast["solo"];

	ratio = freq/(70.midicps);
	trate = 30;

	triSig = SinOsc.ar(freq/2, mul: 0.1) + SinOsc.ar(freq, mul: 0.1);
	triSigEnv = EnvGen.kr(Env.new());
	triSig = triSig*triSigEnv!2;

	env = EnvGen.kr(Env.perc(atkTime, relTime), doneAction: 2)*amp;
	vib = SinOsc.kr(vibHz*bend.abs.linlin(0, 1, 1, 4), mul: bend.abs*0.02, add: 1);
	viblfo = SinOsc.kr(vibHz*bend.abs.linlin(0, 1, 1, 4), mul: bend.abs*0.1, add: 1);

	clk = Impulse.kr(trate);
	pan = WhiteNoise.kr(0.6);
	winLen = 15 * winMul / trate;
	soloStart = (pos + TRand.kr(0, 0.01, clk))*BufDur.kr(soloBuf);
	chorusStart = (pos + TRand.kr(0, 0.01, clk))*BufDur.kr(chorusBuf);

	soloSig = TGrains.ar(
		2,
		clk,
		soloBuf,
		ratio*vib,
		soloStart,
		winLen,
		pan,
		0.5
	)!2;

/*	chorusSig = TGrains.ar(
		2,
		clk,
		chorusBuf,
		ratio,
		chorusStart,
		winLen,
		pan,
		0.01
	)!2;*/

	sig = soloSig + triSig;
	sig = sig*env*viblfo;
	sig = HPF.ar(sig, freq*hpf);
	Out.ar(out, sig!2)
}).add;
)