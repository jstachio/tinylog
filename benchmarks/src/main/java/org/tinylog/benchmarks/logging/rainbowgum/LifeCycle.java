package org.tinylog.benchmarks.logging.rainbowgum;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.tinylog.benchmarks.logging.AbstractLifeCycle;
import org.tinylog.benchmarks.logging.LocationInfo;

import io.jstach.rainbowgum.LogConfig;
import io.jstach.rainbowgum.LogProperties;
import io.jstach.rainbowgum.LogProperties.MutableLogProperties;
import io.jstach.rainbowgum.LogPublisher.PublisherFactory;
import io.jstach.rainbowgum.RainbowGum;
import io.jstach.rainbowgum.output.FileOutput;
import io.jstach.rainbowgum.output.FileOutputBuilder;
import io.jstach.rainbowgum.pattern.format.PatternEncoderBuilder;
import io.jstach.rainbowgum.slf4j.RainbowGumSLF4JServiceProvider;
import io.jstach.rainbowgum.spi.RainbowGumServiceProvider;

@State(Scope.Benchmark)
public class LifeCycle extends AbstractLifeCycle {


	@Param({"false", "true"})
	boolean async;
	
	//Logger logger;
	RainbowGum gum;
	///LogRouter router;
	Logger logger;
		
	@Override
	protected void init(
			Path file)
			throws Exception {
		String fileName = file.toString();
		FileOutput fileOutput = new FileOutputBuilder("file").fileName(fileName).append(true).build();
		
		boolean changeable = getLocationInfo() == LocationInfo.FULL;
		
		/*
		 * Changeable logger is only configurable with properties at the moment.
		 */
		LogProperties props =  ! changeable  ? LogProperties.StandardProperties.EMPTY :  MutableLogProperties.builder()
				.build()
				.put("logging.global.change", "true")
				.put("logging.change", "true");
		
		
		ServiceLoader<RainbowGumServiceProvider> loader = ServiceLoader.load(RainbowGumServiceProvider.class);
		
		LogConfig config = LogConfig.builder()
				.serviceLoader(loader)
				.properties(props)
				.build();
		
		RainbowGum gum = RainbowGum.builder(config)
		.route(r -> {
			r.level(System.Logger.Level.INFO);
			
			if (async) {
				r.publisher(PublisherFactory.ofAsync(1024));
			}
			
			r.appender("file", a -> {
				a.output(fileOutput);
				final String pattern;
				switch(getLocationInfo()) {
				case NONE:
					pattern = "%date{yyyy-MM-dd HH:mm:ss} - %thread - %level: %message%n";
					break;
				case CLASS_OR_CATEGORY_ONLY:
					pattern = "%date{yyyy-MM-dd HH:mm:ss} - %thread - %logger - %level: %message%n";
					break;
				case FULL:
					pattern = "%date{yyyy-MM-dd HH:mm:ss} - %thread - %57.57class.%method\\(\\) - %level: %message%n";
					break;
				default:
					throw new IllegalStateException();
				
				}
				a.encoder(new PatternEncoderBuilder("file")
						.pattern(pattern)
						.build());
			});
		}).build().start();
		
		this.gum = gum;
		
		// I assume all this padding with underscores is to make
		// logger names the same size.

		
		System.out.println(gum.router());
		
		RainbowGumSLF4JServiceProvider provider = new RainbowGumSLF4JServiceProvider();
		provider.initialize(gum);
		ILoggerFactory loggerFactory = provider.getLoggerFactory();
		this.logger = loggerFactory.getLogger(name);
		this.logger.info("blah");
	}
	
	final static String name = 
	// org.tinylog.benchmarks.logging.tinylog2.Tinylog2Benchmark
	// org.tinylog.benchmarks.logging.rainbowgum.GumBenchmark___
			"org.tinylog.benchmarks.logging.rainbowgum.GumBenchmark___";
//	
//	LogEvent.Builder info() {
//		return router.eventBuilder(name, System.Logger.Level.INFO);
//	}
//	
//	LogEvent.Builder debug() {
//		return router.eventBuilder(name, System.Logger.Level.DEBUG);
//	}

	@Override
	protected void shutDown()
			throws Exception {
		RainbowGum gum = this.gum;
		if (gum != null) {
			gum.close();
		}
	}

}
