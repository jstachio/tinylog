package org.tinylog.benchmarks.logging.rainbowgum;

import java.io.IOException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.slf4j.Logger;
import org.tinylog.benchmarks.logging.AbstractBenchmark;


public class RainbowGumBenchmark extends AbstractBenchmark<LifeCycle> {

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Override
	public void discard(final LifeCycle lifeCycle) {
		lifeCycle.logger.debug("Hello {}!", MAGIC_NUMBER);
	}

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@Override
	public void output(final LifeCycle lifeCycle) throws IOException, InterruptedException {
		Logger logger = lifeCycle.logger;
		for (int i = 0; i < LOG_ENTRIES; ++i) {
			logger.info("Hello {}!", MAGIC_NUMBER);
		}

		lifeCycle.waitForWriting();
	}

}
