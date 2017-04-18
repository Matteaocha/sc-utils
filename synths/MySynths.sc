MySynths {

	*addAll {
		MySynths.wavSynth();
		MySynths.router();
		MySynths.recorder();
		MySynths.playSample();
		MySynths.reverb();
		MySynths.bell();
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
		SynthDef("Reverb", {
			arg in, out=0, predelay=0.1, revtime=1.8, lpf=4500, mix=0.5, amp=1.0;

			var dry, wet, temp, sig;

			dry = In.ar(in, 2);
			wet = 0;
			temp = In.ar(in, 2);
			temp = DelayN.ar(temp, delaytime: predelay);

			16.do({
				temp = AllpassN.ar(temp, 0.05, {Rand(0.001, 0.05)}!2, revtime);
				temp = LPF.ar(temp, lpf);
				wet = wet + temp;
			});
			sig = XFade2.ar(dry, wet, mix*2 -1, amp);
			Out.ar(out, sig);
		}).add;
	}

	*bell {
		SynthDef(\Bell, {
			arg freq=220, amp=0.5, modHarm=7, modDev=2, modAmp=5, atk=1, hld=3, rel=2, out=0;

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
		).add;
	}
}