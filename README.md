[![codecov](https://codecov.io/gh/spring-templates/spring-concurrency-thread/graph/badge.svg?token=N3GEH8C5K7)](https://codecov.io/gh/spring-templates/spring-concurrency-thread)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-D22128?logo=jacoco&logoColor=white)
![Codecov](https://img.shields.io/badge/Codecov-F01F7A?logo=codecov&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

# 관심사

- [멀티 스레드의 자원 공유 문제](https://github.com/spring-templates/spring-concurrency-thread/discussions/16)
- [멀티 쓰레드 자원 업데이트 문제](https://github.com/spring-templates/spring-concurrency-thread/discussions/17)

# 정보

- [동시성 기본 조건과 관심사](https://github.com/spring-templates/spring-concurrency-thread/discussions/2)

# [Counter-implementation Benchmark](https://www.notion.so/softsquared/f314375356b54381a8878cf2dabd381b)

> - median of 25 iterations
> - nRequests: 2^21 - 1

| name              | nThreads | time (ms) | memory (KB) |
|-------------------|----------|-----------|-------------|
| AtomicBatch       | 4        | 12        | 480         |
| Atomic            | 1        | 14        | 318         |
| AtomicBatch       | 1        | 30        | 240         |
| Lock              | 1        | 61        | 241         |
| Synchronized      | 1        | 61        | 241         |
| Polling           | 1        | 78        | 463         |
| CompletableFuture | 1        | 158       | 25710       |

### AtomicBatch vs Atomic

> - nThreads: AtomicBatch=4, Atomic=1

| name        | nRequests | time (ms) | memory (KB) |
|-------------|-----------|-----------|-------------|
| AtomicBatch | 2^21 - 1  | 12        | 480         |
| AtomicBatch | 2^22 - 1  | 24        | 538         |
| AtomicBatch | 2^23 - 1  | 42        | 572         |
| AtomicBatch | 2^30 - 1  | 5695      | 511         |
| AtomicBatch | 2^31 - 1  | 11621     | 294         |
| Atomic      | 2^21 - 1  | 14        | 318         |
| Atomic      | 2^22 - 1  | 27        | 244         |
| Atomic      | 2^23 - 1  | 55        | 344         |
| Atomic      | 2^30 - 1  | 7178      | 103         |
| Atomic      | 2^31 - 1  | 14377     | 266         |
