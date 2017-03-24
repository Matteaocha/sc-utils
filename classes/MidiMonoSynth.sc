MidiMonoSynth {

	var dev, chann, synthDef, synth, onNotes, bendval,
	synthargs, noteOnFunc, noteOffFunc,
	noteBendFunc, cmdPeriodFunc, serverQuitFunc;

	*new { |synthdef, args, device=nil, channel=nil|
		if(args == nil,
			{^super.new.init(synthdef, [], device, channel)},
			{^super.new.init(synthdef, args, device, channel)}
		);
	}

	init { |synthdef, args, device, channel|

		onNotes = Dictionary.new;
		synthDef = synthdef;
		bendval = 1;
		dev = device;
		chann = channel;
		synthargs = args;
		synth = nil;

		MIDIClient.init;
		MIDIIn.connectAll;

		noteOnFunc = {|src, chan, num, vel| this.noteOn(src, chan, num, vel)};
		noteOffFunc = {|src, chan, num, vel| this.noteOff(src, chan, num, vel)};
		noteBendFunc = {|src, chan, val| this.noteBend(src, chan, val)};
		cmdPeriodFunc = { this.cmdPeriod() };
		serverQuitFunc = { this.serverQuit() };

		this.connect();
	}

	serverQuit {
		this.disconnect();
	}

	cmdPeriod {
		onNotes = Dictionary.new;
		synth = nil;
	}

	connect {
		MIDIIn.addFuncTo(\noteOn, noteOnFunc);
		MIDIIn.addFuncTo(\noteOff, noteOffFunc);
		MIDIIn.addFuncTo(\bend, noteBendFunc);
		CmdPeriod.add(cmdPeriodFunc);
		ServerQuit.add(serverQuitFunc);
	}

	disconnect {
		MIDIIn.removeFuncFrom(\noteOn, noteOnFunc);
		MIDIIn.removeFuncFrom(\noteOff, noteOffFunc);
		MIDIIn.removeFuncFrom(\bend, noteBendFunc);
		CmdPeriod.remove(cmdPeriodFunc);
		ServerQuit.remove(serverQuitFunc);
	}

	noteOn { |src, chan, num, vel|
		if(num != nil, {
			if(dev == nil, {
				this.doNoteOn(num, vel);
			}, {
				if(dev == src, {
					if(chann == chan, {
						this.doNoteOn(num, vel);
					})
				})
			});
		});
	}

	noteOff { |src, chan, num|
		if(num != nil, {
			if(dev == nil, {
				this.doNoteOff(num);
			}, {
				if(dev == src, {
					if(chann == chan, {
						this.doNoteOff(num);
					})
				})
			})
		});
	}

	noteBend { |src, chan, val|
		bendval = 1 + (val - 8192) * 1.0/8192;
		if(dev == nil, {
			this.doNoteBend(bendval);
		}, {
			if(dev == src, {
				if(chann == chan, {
					this.doNoteBend(bendval);
				})
			});
		});
	}

	doNoteOn { |num, vel|
		if (onNotes.keys.size == 0, {
			synth = Synth(synthDef, [
				\freq, num.midicps,
				\note, num,
				\amp, vel/128
				] ++ synthargs
			);
			onNotes.put("" ++ num, num);
		}, {
			synth.set(\freq, num.midicps);
			synth.set(\note, num);
			synth.set(\amp, vel/128);
			onNotes.put("" ++ num, num);
		});
	}

	doNoteOff { |num|
		if (onNotes.keys.size == 1, {
			onNotes = Dictionary.new;
			synth.release;
			synth = nil;
		}, {
			var note;
			onNotes.removeAt("" ++ num);
			note = onNotes.values.at(onNotes.size - 1);
			synth.set(\freq, note.midicps);
			synth.set(\note, note);
		});
	}

	doNoteBend { |val|
		if(synth != nil, {synth.set(\bend, val)});
	}
}