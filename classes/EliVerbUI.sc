EliVerbUI {

	var window, synth,
	winWidth=400,
	winHeight=350,
	winPadding=10,
	sliderWidth=285,
	sliderHeight=30,
	labelHeight=25,
	rowWidth=380,
	rowHeight=60,
	numberBoxWidth=90,
	labelWidth=380,
	yPadding=5,
	xPadding=5,
	predelayD=0.1, revtimeD=1.8, lpfD=4500, mixD=0.5, ampD=1.0;

	*new {
		^super.new.init();
	}

	init {
		this.defineSynth();
	}

	start { |in, out, x=500, y=500|

		var serverQuitFunc;

		this.createWindow(x, y);

		synth = Synth.new("EliVerbUISynth", [
			\in, in,
			\out, out,
			\predelay, predelayD,
			\revtime, revtimeD,
			\lpf, lpfD,
			\mix, mixD,
			\amp, ampD
		]);

		this.addRow(window, "Pre-Delay", 0.01, 1.0,
			predelayD, {|val| synth.set(\predelay, val) }
		);
		this.addRow(window, "Reverb time", 0.2, 5.0,
			revtimeD, {|val| synth.set(\revtime, val) }
		);
		this.addRow(window, "Low-Pass filter", 2000, 10000,
			lpfD, {|val| synth.set(\lpf, val) }
		);
		this.addRow(window, "Mix", 0.0, 1.0,
			mixD, {|val| synth.set(\mix, val) }
		);
		this.addRow(window, "Amplitude", 0.0, 1.0,
			ampD, {|val| synth.set(\amp, val) }
		);

		ServerQuit.add({ this.serverQuit() });
	}

	close {
		window.close;
	}

	onWindowClose {
		synth.free;
		synth = nil;
	}

	serverQuit {
		this.close();
	}

	createWindow { |x, y|
		window = Window.new("Eli-Verb UI", Rect.new(x, y, winWidth, winHeight));
		window.front();
		window.alwaysOnTop_(true);

		window.view.decorator_(FlowLayout(
			window.bounds,
			Point(winPadding, winPadding),
			Point(xPadding, yPadding)
		));

		window.onClose_({this.onWindowClose()});
	}

	addRow { |win, text, minVal, maxVal, defaultVal, valueCallback|
		var slider, numberBox, label;


		slider = Slider(win.view, Point(sliderWidth, sliderHeight));

		numberBox = NumberBox(win.view, Point(numberBoxWidth, sliderHeight))
		.clipLo_(minVal)
		.clipHi_(maxVal);

		label = StaticText(win.view, Point(labelWidth, labelHeight))
		.string_(text)
		.align_(\center);

		slider.value_(defaultVal.linlin(minVal, maxVal, 0, 1));
		numberBox.value_(defaultVal);

		slider.action_({|obj|
			var newVal = obj.value.linlin(0, 1, minVal, maxVal);
			numberBox.value_(newVal);
			valueCallback.value(newVal);
		});

		numberBox.action({|obj|
			var newVal = obj.value.linlin(minVal, maxVal, 0, 1);
			slider.value_(newVal);
		});
	}

	defineSynth {
		SynthDef("EliVerbUISynth", {
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
}