MidiPattern {

	var dev, chann, allPatterns, bendval, pQuant,
	patternArgs, noteOnFunc, noteOffFunc,
	noteBendFunc, cmdPeriodFunc, serverQuitFunc, rel, group;

	*new { |args, quant=1, release=true, device=nil, channel=nil|
		if(args == nil,
			{^super.new.init([\instrument, \default], quant, release, device, channel)},
			{^super.new.init(args, quant, release, device, channel)}
		);
	}

	init { |args, quant, release, device, channel|

		var treeFunc;

		allPatterns = Array.newClear(128);
		bendval = 1;
		dev = device;
		chann = channel;
		patternArgs = args;
		pQuant = quant;
		rel = release;

		MIDIClient.init;
		MIDIIn.connectAll;

		noteOnFunc = {|src, chan, num, vel| this.noteOn(src, chan, num, vel)};
		noteOffFunc = {|src, chan, num, vel| this.noteOff(src, chan, num, vel)};
		noteBendFunc = {|src, chan, val| this.noteBend(src, chan, val)};
		cmdPeriodFunc = { this.cmdPeriod() };
		serverQuitFunc = { this.serverQuit() };

		this.connect();
	}

	cmdPeriod {
		128.do({|i| allPatterns[i] = nil});
	}

	serverQuit {
		this.disconnect();
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
		128.do({|i| allPatterns[i] = nil});
	}

	noteOn { |src, chan, num, vel|
		var pArgs = [
			\root, num-48,
			\amp, vel/128,
			\bend, bendval
		] ++ patternArgs;
		if(num != nil, {
			if(dev == nil, {
				allPatterns[num] = Pbind(*pArgs).play(quant: pQuant);
			}, {
				if(dev == src, {
					if(chann == chan, {
						allPatterns[num] = Pbind(*pArgs).play(quant: pQuant);
					})
				})
			});
		});
	}

	noteOff { |src, chan, num, vel|
		if(num != nil, {
			if(dev == nil, {
				if(rel, { allPatterns[num].stop() });
				allPatterns[num] = nil;
			}, {
				if(dev == src, {
					if(chann == chan, {
						if(rel, { allPatterns[num].stop() });
						allPatterns[num] = nil;
					})
				})
			})
		});
	}

	noteBend { |src, chan, val|
		bendval = 1 + (val - 8192) * 2.0/16383;
	}
}