~rootPath = {
	var p;
	p = PathName.(thisProcess.nowExecutingPath).parentPath;
	p++"/";
};

~bufferDictFromTree = { |root|
	var dict;
	PathName(root).entries.do { |entry|
		dict.add(
			entry.folderName.asSymbol ->
			Array.fill( entry.entries.size, { |i|
				Buffer.read(s, entry.entries[i].fullPath)
			})
		);
	};
	dict;
};