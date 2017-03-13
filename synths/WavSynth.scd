(
SynthDef(\WavSynth, {arg bufnum=0, freq=440, amp=0.5, pos=0.5, atkTime=0.1, relTime=10.0, hpf=0.6, vibDepth=0, vibHz=5, winMul=1.0;

			var sig, env, ratio, trate, clk, pan, winStart, winLen, vib;

			ratio = freq/440;
			trate = 30;

			env = EnvGen.kr(Env.perc(atkTime, relTime), doneAction: 2)*amp;
			vib = SinOsc.kr(vibHz, mul: vibDepth*0.1, add: 1);

			clk = Impulse.kr(trate);
			pan = WhiteNoise.kr(0.6);
			winLen = 15 * winMul / trate;
			winStart = (pos + TRand.kr(0, 0.01, clk))*BufDur.kr(bufnum);

	        sig = TGrains.ar(2, clk, bufnum, ratio*vib, winStart, winLen, pan, 0.5)!2;
			sig = sig*env;
			sig = HPF.ar(sig, freq*hpf);
			Out.ar(0, sig!2)
}).add;
)
