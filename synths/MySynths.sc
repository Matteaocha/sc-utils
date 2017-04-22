MySynths {

	*addAll {
		MySynths.wavSynth();
		MySynths.recorder();
		MySynths.playSample();
		MySynths.reverb();
		MySynths.bell();
		MySynths.grainDelay();
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

	*recorder {
		SynthDef(\Recorder, { |in, out, buf|
			var sig = In.ar(in, 2);
			RecordBuf.ar(sig, buf, loop: 0, doneAction: 2);
			Out.ar(out, sig);
		}).add;
	}

	*playSample {
		SynthDef(\PlaySample, {|freq=440, sampleFreq=440, buf, startPos=0, atk=0.01, rel=1, amp=1.0, out=0|
			var sig, env, bufStart, rate;

			bufStart = BufFrames.kr(buf)*startPos;
			rate = freq/sampleFreq;

			env = EnvGen.kr(Env.perc(atk, rel*rate.min(1)), doneAction: 2);
			sig = PlayBuf.ar(buf.numChannels, buf, rate, startPos: bufStart)!2;
			Out.ar(0, sig*env);
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

	* grainDelay {
		SynthDef(\GrainDelay, {|in, amp=1, len=3, pitch=1, rate=60, grainSize=0.2, stereoWidth=0.2, out=0|
			var buf, buflen, clk, pan, winLen, winStartMin, winStartMax, winStart, sig;

			buflen = 44100 * len;
			buf = LocalBuf(buflen);
			RecordBuf.ar(In.ar(in), buf);

			clk = Impulse.kr(rate);
			pan = WhiteNoise.kr(stereoWidth, -1*(stereoWidth/2));
			winLen = len * grainSize;
			winStartMin = 0;
			winStartMax = len - winLen;
			winStart = TRand.kr(winStartMin, winStartMax, clk);

			sig = GrainBuf.ar(2, clk, winLen, buf, pitch, winStart, 2, pan)*0.5;
			Out.ar(out, sig * amp);
		}).add;
	}
}