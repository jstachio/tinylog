package org.tinylog.benchmarks.logging.rainbowgum;

import java.nio.file.Path;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.tinylog.benchmarks.logging.AbstractLifeCycle;

import io.jstach.rainbowgum.LogPublisher.PublisherFactory;
import io.jstach.rainbowgum.LogRouter;
import io.jstach.rainbowgum.LogRouter.Route;
import io.jstach.rainbowgum.LogRouter.Router;
import io.jstach.rainbowgum.LogEvent;
import io.jstach.rainbowgum.RainbowGum;
import io.jstach.rainbowgum.output.FileOutput;
import io.jstach.rainbowgum.output.FileOutputBuilder;
import io.jstach.rainbowgum.pattern.format.PatternEncoderBuilder;
import io.jstach.rainbowgum.slf4j.RainbowGumSLF4JServiceProvider;

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
		
		RainbowGum gum = RainbowGum.builder()
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
					pattern = "%date{yyyy-MM-dd HH:mm:ss} - %thread - "+ name +".output\\(\\) - %level: %message%n";
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
		System.out.println("before initialize");
		provider.initialize(gum);
		System.out.println("after initialize");
		ILoggerFactory loggerFactory = provider.getLoggerFactory();
		this.logger = loggerFactory.getLogger(name);
		System.out.println("after logger - " + this.logger);
		this.logger.info("blah");
		System.out.println("test info");
		System.out.println("async: " + async);
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
