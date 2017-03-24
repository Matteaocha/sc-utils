SampleLoader {

	*directory { |pathname, server|
		var path, dict;
		path = PathName(pathname);
		dict = Dictionary.new;
		path.files.do({|file|
			if(file.extension == "wav", {
				dict.put(
					file.fileNameWithoutExtension,
					Buffer.read(server, file.fullPath)
				);
			});
		});
		^dict;
	}

	*free { |dictionary|
		dictionary.values.do({ |buf|
			buf.free;
		});
	}
}