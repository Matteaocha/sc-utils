MySynths {

	*addAll {
		MySynths.wavSynth();
		MySynths.router();
		MySynths.recorder();
		MySynths.playSample();
		MySynths.reverb();
	}

	*wavSynth {
		SynthDef(\WavSynth, {
			arg buf, freq=440, amp=0.5, pos=0.5, atkTime=0.1,
			relTime=10.0, hpf=0.6, vibDepth=0, vibHz=5, winMul=1.0, out=0;

			var sig, env, ratio, trate, clk, pan, winStart, winLen, vib;

			ratio = freq/440;
			trate = 30;

			env = EnvGen.kr(Env.perc(atkTime, relTime), doneAction: 2)*amp;
			vib = SinOsc.kr(vibHz, mul: vibDepth*0.1, add: 1);

			clk = Impulse.kr(trate);
			pan = WhiteNoise.kr(0.6);
			winLen = 15 * winMul / trate;
			winStart = (pos + TRand.kr(0, 0.01, clk))*BufDur.kr(buf);

			sig = TGrains.ar(2, clk, buf, ratio*vib, winStart, winLen, pan, 0.5)!2;
			sig = sig*env;
			sig = HPF.ar(sig, freq*hpf);
			Out.ar(out, sig!2)
		}).add;
	}

	*router {
		SynthDef(\Router, { |in, out|
			Out.ar(out, In.ar(in));
		}).add;
	}

	*recorder {
		SynthDef(\Recorder, { |in, out, buf|
			var sig = In.ar(in, 2);
			RecordBuf.ar(sig, buf, loop: 0, doneAction: 2);
			Out.ar(out, sig);
		}).add;
	}

	*playSample {
		SynthDef(\PlaySample, {|freq=440, sampleFreq=880, buf, amp=1.0, lfoAmp=0, bend=0|
			var sig, lfo;
			lfo = SinOsc.kr(8*bend, mul: lfoAmp, add: 1);
			sig = PlayBuf.ar(buf.numChannels, buf, (freq/sampleFreq), doneAction: 2);
			Out.ar(0, sig*lfo);
		}).add;
	}

	*reverb {
		SynthDef(\Reverb, {
			arg in, out, roomsize=10, revtime=3, damping=0.5, inputbw=0.5,
			spread=15, drylevel=1, earlyreflevel=0.7, taillevel=0.5;
			var sig = In.ar(in, 2);
			Out.ar(out,
				GVerb.ar(sig, roomsize, revtime, damping,
					inputbw, spread, drylevel, earlyreflevel, taillevel
				)
			);
		}).add;
	}
}