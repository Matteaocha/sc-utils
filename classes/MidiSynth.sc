MidiSynth {

	var dev, chann, synthDef, allNotes, bendval,
	synthargs, noteOnFunc, noteOffFunc,
	noteBendFunc, cmdPeriodFunc, serverTreeFunc, gate, group;

	*new { |synthdef, args, gated=false, device=nil, channel=nil|
		if(args == nil,
			{^super.new.init(synthdef, [], gated, device, channel)},
			{^super.new.init(synthdef, args, gated, device, channel)}
		);
	}

	init { |synthdef, args, gated, device, channel|

		var treeFunc;

		allNotes = Array.newClear(128);
		synthDef = synthdef;
		bendval = 1;
		dev = device;
		chann = channel;
		synthargs = args;
		gate = gated;
		group = Group.new;

		MIDIClient.init;
		MIDIIn.connectAll;


		noteOnFunc = {|src, chan, num, vel| this.noteOn(src, chan, num, vel)};
		noteOffFunc = {|src, chan, num, vel| this.noteOff(src, chan, num, vel)};
		noteBendFunc = {|src, chan, val| this.noteBend(src, chan, val)};
		cmdPeriodFunc = { this.cmdPeriod() };
		serverTreeFunc = { this.serverTree() };

		this.connect();
	}

	cmdPeriod {
		128.do({|i| allNotes[i] = nil});
	}

	serverTree {
		if(group != nil, {group.free});
		group = Group.new;
	}

	connect {
		MIDIIn.addFuncTo(\noteOn, noteOnFunc);
		MIDIIn.addFuncTo(\noteOff, noteOffFunc);
		MIDIIn.addFuncTo(\bend, noteBendFunc);
		CmdPeriod.add(cmdPeriodFunc);
		ServerTree.add(serverTreeFunc);
	}

	disconnect {
		MIDIIn.removeFuncFrom(\noteOn, noteOnFunc);
		MIDIIn.removeFuncFrom(\noteOff, noteOffFunc);
		MIDIIn.removeFuncFrom(\bend, noteBendFunc);
		CmdPeriod.remove(cmdPeriodFunc);
		ServerTree.remove(serverTreeFunc);
		128.do({|i| allNotes[i] = nil});
	}

	noteOn { |src, chan, num, vel|
		if(num != nil, {
			if(dev == nil, {
				allNotes[num] = Synth(synthDef, [
					\freq, num.midicps,
					\note, num,
					\amp, vel/128
					] ++ synthargs,
					group
				);
			}, {
				if(dev == src, {
					if(chann == chan, {
						allNotes[num] = Synth(synthDef, [
							\freq, num.midicps,
							\note, num,
							\amp, vel/128
							] ++ synthargs,
							group
						);
					})
				})
			});
		});
	}

	noteOff { |src, chan, num, vel|
		if(num != nil, {
			if(dev == nil, {
				if(gate, {allNotes[num].release()});
				allNotes[num] = nil;
			}, {
				if(dev == src, {
					if(chann == chan, {
						if(gate, {allNotes[num].release()});
						allNotes[num] = nil;
					})
				})
			})
		});
	}

	noteBend { |src, chan, val|
		bendval = 1 + (val - 8192) * 2.0/16383;
		if(dev == nil, {
			group.set(\bend, bendval);
		}, {
			if(dev == src, {
				if(chann == chan, {
					group.set(\bend, bendval);
				})
			});
		});
	}
}