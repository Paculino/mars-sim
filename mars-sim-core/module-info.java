module mars-sim-core {

	requires java.util.Optional;
	requires java.time;
//	requires java.lang;
//	requires java.nio;
//	requires java.io;
//  requires java.json;
//  requires java.util;
//  requires java.text;
	requires java;

	requires sun.misc.Unsafe;
    requires javax.json;

    requires junit;
    requires jdom;
    requires guice;
    requires log4j;
    requires commons-collections;
//    requires text-io;
    requires gson;
    requires commons-lang3;
//    requires xz;
    requires jackson;
	requires flogger;
    requires kotlin;

    requires mars-sim-mapdata;

	exports mars-sim-core;
}