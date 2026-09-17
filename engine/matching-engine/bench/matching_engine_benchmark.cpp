#include "marketforge/matching_engine.hpp"

#include <algorithm>
#include <chrono>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <string>
#include <vector>

namespace {

using Clock = std::chrono::steady_clock;
using Nanoseconds = std::chrono::nanoseconds;

constexpr std::size_t WARMUP = 10'000;
constexpr std::size_t RESTING_ORDERS = 100'000;
constexpr std::size_t MEASURED_ORDERS = 100'000;

marketforge::Order make_order(
    std::uint64_t id,
    marketforge::Side side,
    std::int64_t price,
    std::int64_t quantity
) {
    return marketforge::Order{
        id,
        "client-" + std::to_string(id),
        side,
        marketforge::OrderType::LIMIT,
        marketforge::TimeInForce::GTC,
        price,
        quantity,
        0
    };
}

double percentile_us(
    std::vector<std::uint64_t>& samples,
    double percentile
) {
    std::sort(samples.begin(), samples.end());

    const double index =
        percentile * static_cast<double>(samples.size() - 1);

    const auto lower = static_cast<std::size_t>(index);
    const auto upper =
        std::min(lower + 1, samples.size() - 1);

    const double fraction = index - lower;

    const double value =
        static_cast<double>(samples[lower]) * (1.0 - fraction) +
        static_cast<double>(samples[upper]) * fraction;

    return value / 1000.0;
}

void populate_asks(
    marketforge::MatchingEngine& engine,
    std::uint64_t first_id
) {
    for (std::size_t i = 0; i < RESTING_ORDERS; ++i) {
        engine.submit(
            make_order(
                first_id + i,
                marketforge::Side::SELL,
                100'000 + static_cast<std::int64_t>(i % 100),
                1
            )
        );
    }
}

void populate_bids(
    marketforge::MatchingEngine& engine,
    std::uint64_t first_id
) {
    for (std::size_t i = 0; i < RESTING_ORDERS; ++i) {
        engine.submit(
            make_order(
                first_id + i,
                marketforge::Side::BUY,
                100'000 - static_cast<std::int64_t>(i % 100),
                1
            )
        );
    }
}

void warmup_matching(
    marketforge::MatchingEngine& engine,
    std::uint64_t first_id
) {
    for (std::size_t i = 0; i < WARMUP; ++i) {
        engine.submit(
            make_order(
                first_id + i,
                marketforge::Side::BUY,
                100'100,
                1
            )
        );
    }
}

void benchmark_active_matching() {
    /*
     * Create exactly one resting maker for every
     * measured taker. Every measured operation must
     * execute one trade.
     */
    marketforge::MatchingEngine engine;

    for (std::size_t i = 0; i < MEASURED_ORDERS; ++i) {
        engine.submit(
            make_order(
                static_cast<std::uint64_t>(i + 1),
                marketforge::Side::SELL,
                100'000,
                1
            )
        );
    }

    /*
     * Warm-up is performed on a separate engine so
     * warm-up work does not alter the measured book.
     */
    marketforge::MatchingEngine warmup_engine;
    populate_asks(warmup_engine, 1);
    warmup_matching(warmup_engine, RESTING_ORDERS + 1);

    const auto start = Clock::now();

    std::size_t trades = 0;

    for (std::size_t i = 0; i < MEASURED_ORDERS; ++i) {
        const auto fills = engine.submit(
            make_order(
                MEASURED_ORDERS + i + 1,
                marketforge::Side::BUY,
                100'000,
                1
            )
        );

        trades += fills.size();
    }

    const auto end = Clock::now();

    const double elapsed =
        std::chrono::duration<double>(end - start).count();

    std::cout << "\n========== ACTIVE MATCHING THROUGHPUT ==========\n";
    std::cout << "Resting orders:   " << MEASURED_ORDERS << '\n';
    std::cout << "Measured orders:  " << MEASURED_ORDERS << '\n';
    std::cout << "Trades generated: " << trades << '\n';

    std::cout << std::fixed << std::setprecision(2);

    std::cout << "Total time (ms):  "
              << elapsed * 1000.0 << '\n';

    std::cout << "Throughput:       "
              << static_cast<double>(MEASURED_ORDERS) / elapsed
              << " orders/sec\n";
}

void benchmark_active_latency() {
    marketforge::MatchingEngine engine;

    for (std::size_t i = 0; i < MEASURED_ORDERS; ++i) {
        engine.submit(
            make_order(
                static_cast<std::uint64_t>(i + 1),
                marketforge::Side::SELL,
                100'000,
                1
            )
        );
    }

    constexpr std::size_t BATCH_SIZE = 1000;
    constexpr std::size_t BATCHES = MEASURED_ORDERS / BATCH_SIZE;

    std::vector<std::uint64_t> samples;
    samples.reserve(BATCHES);

    std::size_t trades = 0;

    for (std::size_t batch = 0; batch < BATCHES; ++batch) {
        const auto begin = Clock::now();

        for (std::size_t j = 0; j < BATCH_SIZE; ++j) {
            const std::size_t i = batch * BATCH_SIZE + j;

            const auto fills = engine.submit(
                make_order(
                    MEASURED_ORDERS + i + 1,
                    marketforge::Side::BUY,
                    100'000,
                    1
                )
            );

            trades += fills.size();
        }

        const auto end = Clock::now();

        samples.push_back(
            static_cast<std::uint64_t>(
                std::chrono::duration_cast<Nanoseconds>(
                    end - begin
                ).count() / BATCH_SIZE
            )
        );
    }

    std::cout << "\n========== ACTIVE MATCHING LATENCY ==========\n";
    std::cout << "Operations:       " << MEASURED_ORDERS << '\n';
    std::cout << "Trades generated: " << trades << '\n';

    std::cout << std::fixed << std::setprecision(2);

    std::cout << "p50 latency:      "
              << percentile_us(samples, 0.50)
              << " us\n";

    std::cout << "p95 latency:      "
              << percentile_us(samples, 0.95)
              << " us\n";

    std::cout << "p99 latency:      "
              << percentile_us(samples, 0.99)
              << " us\n";
}


void benchmark_cancellation_latency() {
    constexpr std::size_t CANCEL_ORDERS = 100'000;
    constexpr std::size_t BATCH_SIZE = 100;
    constexpr std::size_t BATCHES = CANCEL_ORDERS / BATCH_SIZE;

    auto run_case = [](const char* name, std::size_t target_index) {
        std::vector<std::uint64_t> samples;
        samples.reserve(BATCHES);

        for (std::size_t batch = 0; batch < BATCHES; ++batch) {
            marketforge::OrderBook book;

            for (std::size_t i = 0; i < CANCEL_ORDERS; ++i) {
                book.add(
                    make_order(
                        i + 1,
                        marketforge::Side::BUY,
                        200'000,
                        1
                    )
                );
            }

            /*
             * Each batch measures one cancellation at the same
             * position in a freshly constructed 100K-order book.
             */
            const auto target =
                static_cast<std::uint64_t>(target_index + 1);

            const auto begin = Clock::now();

            const bool cancelled = book.cancel(target);

            const auto end = Clock::now();

            if (!cancelled) {
                std::cerr << "Cancellation failed\n";
                std::exit(1);
            }

            samples.push_back(
                static_cast<std::uint64_t>(
                    std::chrono::duration_cast<Nanoseconds>(
                        end - begin
                    ).count()
                )
            );
        }

        std::cout << "\n--- " << name << " cancellation ---\n";
        std::cout << "Active orders:    " << CANCEL_ORDERS << '\n';
        std::cout << "Measured ops:     " << BATCHES << '\n';

        std::cout << std::fixed << std::setprecision(2);

        std::cout << "p50 latency:      "
                  << percentile_us(samples, 0.50)
                  << " us\n";

        std::cout << "p95 latency:      "
                  << percentile_us(samples, 0.95)
                  << " us\n";

        std::cout << "p99 latency:      "
                  << percentile_us(samples, 0.99)
                  << " us\n";
    };

    run_case("FRONT", 0);
    run_case("MIDDLE", CANCEL_ORDERS / 2);
    run_case("BACK", CANCEL_ORDERS - 1);
}

void benchmark_resting_throughput() {
    marketforge::MatchingEngine warmup_engine;

    for (std::size_t i = 0; i < WARMUP; ++i) {
        warmup_engine.submit(
            make_order(
                i + 1,
                marketforge::Side::BUY,
                200'000 - static_cast<std::int64_t>(i),
                1
            )
        );
    }

    marketforge::MatchingEngine engine;

    const auto start = Clock::now();

    for (std::size_t i = 0; i < MEASURED_ORDERS; ++i) {
        engine.submit(
            make_order(
                i + 1,
                marketforge::Side::BUY,
                200'000 -
                    static_cast<std::int64_t>(i % 100),
                1
            )
        );
    }

    const auto end = Clock::now();

    const double elapsed =
        std::chrono::duration<double>(end - start).count();

    std::cout << "\n========== RESTING ORDER THROUGHPUT ==========\n";
    std::cout << "Measured orders:  " << MEASURED_ORDERS << '\n';

    std::cout << std::fixed << std::setprecision(2);

    std::cout << "Total time (ms):  "
              << elapsed * 1000.0 << '\n';

    std::cout << "Throughput:       "
              << static_cast<double>(MEASURED_ORDERS) / elapsed
              << " orders/sec\n";
}

} // namespace

int main() {
    std::cout << "MarketForge Matching Engine Benchmark\n";
    std::cout << "Warmup:            " << WARMUP << '\n';
    std::cout << "Resting orders:    " << RESTING_ORDERS << '\n';
    std::cout << "Measured orders:   " << MEASURED_ORDERS << '\n';

    benchmark_resting_throughput();
    benchmark_active_matching();
    benchmark_active_latency();
    benchmark_cancellation_latency();

    return 0;
}
