# PVec

Hello there! To run the benchmarks, you'll have to have both Leiningen and Maven
installed. First, go into the `pvec` directory and issue the command `lein
install`. Then, in the `pvec-perf` directory, issue `mvn clean install`. The
benchmark suite should be at `target/benchmarks.jar`, and you can look at
options to pass in to it with `java -jar target/benchmarks.jar -h`.

Warning: It does take a long time to run through the benchmarks. If you're not
that interested in the unsafe implementations, you can add in `-e 'unsafe'` to
speed it up.
