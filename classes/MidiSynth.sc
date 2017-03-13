MidiSynth {

	var dev, chann, synthDef, allNotes, bendval, synthargs;

	*new { |synthdef, args, device=nil, channel=nil|
		if(args == nil, {^super.new.init(synthdef, [], device, channel)}, {^super.new.init(synthdef, args, device, channel)});
	}

	init { |synthdef, args, device, channel|

		allNotes = Array.newClear(128);
		synthDef = synthdef;
		bendval = 0;
		dev = device;
		chann = channel;
		synthargs = args;

		MIDIClient.init;
		MIDIIn.connectAll;

		MIDIIn.addFuncTo(\noteOn, this.noteOn);
		MIDIIn.addFuncTo(\noteOff, this.noteOff);
		MIDIIn.addFuncTo(\bend, this.noteBend);
	}

	noteOn { |src, chan, num, vel|
		[src, chan, num, vel].postln;
		if(num != nil, {
			if(dev == nil, {
				allNotes[num] = Synth(synthDef, [
					\freq, num.midicps,
					\note, num,
					\amp, vel/128
					] ++ synthargs
				);
			}, {
				if(dev == src, {
					if(chann == chan, {
						allNotes[num] = Synth(synthDef, [
							\freq, num.midicps,
							\note, num,
							\amp, vel/128
							] ++ synthargs
						);
					})
				})
			});
		});
	}

	noteOff { |src, chan, num, vel|
		if(num != nil, {
			if(dev == nil, {
				allNotes[num] = nil;
			}, {
				if(dev == src, {
					if(chann == chan, {
						allNotes[num] = nil;
					})
				})
			})
		});
	}

	noteBend { |src, chan, val|
		if(dev == src, {
				if(chann == chan, {
				})
			});
	}
}