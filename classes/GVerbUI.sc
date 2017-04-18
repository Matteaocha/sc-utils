GVerbUI {

	var window, synth, serverQuitFunc,
	winWidth=400,
	winHeight=600,
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
	roomsizeD=10, revtimeD=3, dampingD=0.5, inputbwD=0.5,
	spreadD=15, drylevelD=1, earlyreflevelD=0.7, taillevelD=0.5, mulD=0.2;

	*new {
		^super.new.init();
	}

	init {
		this.defineSynth();
	}

	start { |in, out, x=500, y=500|

		var serverQuitFunc;

		this.createWindow(x, y);

		synth = Synth.new("GVerbUISynth", [
			\in, in,
			\out, out,
			\roomsize, roomsizeD,
			\revtime, revtimeD,
			\damping, dampingD,
			\inputbw, inputbwD,
			\spread, spreadD,
			\drylevel, drylevelD,
			\taillevel, taillevelD,
			\mul, mulD
		]);

		this.addRow(window, "Room size", 0.0, 10.0,
			roomsizeD, {|val| synth.set(\roomsize, val) }
		);
		this.addRow(window, "Reverb time", 0.0, 20.0,
			revtimeD, {|val| synth.set(\revtime, val) }
		);
		this.addRow(window, "Damping", 0.0, 1.0,
			dampingD, {|val| synth.set(\damping, val) }
		);
		this.addRow(window, "Input Bandwidth", 0.0, 2.0,
			inputbwD, {|val| synth.set(\inputbw, val) }
		);
		this.addRow(window, "Spread", 0.0, 50,
			spreadD, {|val| synth.set(\spread, val) }
		);
		this.addRow(window, "Dry level", 0.0, 1.0,
			drylevelD, {|val| synth.set(\drylevel, val) }
		);
		this.addRow(window, "Early reflection time", 0.0, 1.0,
			earlyreflevelD, {|val| synth.set(\earlyreflevel, val) }
		);
		this.addRow(window, "Tail level", 0.0, 1.0,
			taillevelD, {|val| synth.set(\taillevel, val) }
		);
		this.addRow(window, "Amplitude", 0.0, 1.0,
			mulD, {|val| synth.set(\mul, val) }
		);

		serverQuitFunc = { this.serverQuit() };
		ServerQuit.add(serverQuitFunc);
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
		window = Window.new("GVerb UI", Rect.new(x, y, winWidth, winHeight));
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
		SynthDef("GVerbUISynth", {
			arg in, out, roomsize=10, revtime=3, damping=0.5,
			inputbw=0.5, spread=15, drylevel=1, earlyreflevel=0.7,
			taillevel=0.5, mul=0.2;

			var sig = In.ar(in, 2);
			Out.ar(out,
				GVerb.ar(sig, roomsize.lag(0.1), revtime.lag(0.1), damping.lag(0.1),
					inputbw.lag(0.1), spread.lag(0.1), drylevel.lag(0.1), earlyreflevel.lag(0.1),
					taillevel.lag(0.1), mul: mul.lag(0.1)
				)
			);
		}).add;
	}
}