/*RUN*/

~waltzPiano = Dictionary.new;
~waltzPiano.put("path", PathName(thisProcess.nowExecutingPath).parentPath);
~waltzPiano.put("stereo", Buffer.read(s, ~waltzPiano["path"] ++ "stereo.wav"));
~waltzPiano.put("mono", Buffer.read(s, ~waltzPiano["path"] ++ "mono.wav"));

(
SynthDef(\WaltzPiano, {
	arg freq=440, amp=0.5, baseAmp=1.0, bend=0,
	hpf=0.6, vibHz=2, winMul=1.0, out=0, gate=1;

	var sig, ratio, trate, clk, pan, winLen, vib, viblfo,
	monoStart, monoBuf,
	stereoBuf,
	sampleSig, sampleEnv,
	granSig, granEnv;

	stereoBuf = ~waltzPiano["stereo"];
	monoBuf = ~waltzPiano["mono"];

	ratio = freq/(70.midicps);
	trate = 30;

	sampleEnv = EnvGen.kr(Env.new([0, 1, 1, 0], [0.01, 0.2/ratio, 0.5/ratio]));
	sampleSig = PlayBuf.ar(2, stereoBuf, ratio);

	vib = SinOsc.kr(vibHz*bend.abs.linlin(0, 1, 1, 4), mul: bend.abs*0.02, add: 1);
	viblfo = SinOsc.kr(vibHz*bend.abs.linlin(0, 1, 1, 4), mul: bend.abs*0.1, add: 1);

	clk = Impulse.kr(trate);
	pan = WhiteNoise.kr(0.6);
	winLen = 15 * winMul / trate;
	monoStart = (0.1 + TRand.kr(0, 0.01, clk))*BufDur.kr(monoBuf);

	granEnv = EnvGen.kr(Env.adsr(1, sustainLevel: 0.8, releaseTime: 3), gate, doneAction: 2);
	granSig = TGrains.ar(
		2,
		clk,
		monoBuf,
		ratio*vib,
		monoStart,
		winLen,
		pan
	);

	granSig = granSig*granEnv*viblfo;

	sig = sampleSig + granSig;
	sig = HPF.ar(sig, freq*hpf)*baseAmp;

	Out.ar(out, sig)
}).add;
)