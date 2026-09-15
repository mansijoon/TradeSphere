cmake_minimum_required(VERSION 3.20)

project(matching-engine LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(PkgConfig REQUIRED)
pkg_check_modules(RDKAFKA REQUIRED rdkafka)

include_directories(
    include
    ${RDKAFKA_INCLUDE_DIRS}
)

add_executable(matching-engine
    src/main.cpp
    src/order_book.cpp
    src/matching_engine.cpp
    src/kafka_consumer.cpp
    src/kafka_producer.cpp
    src/trade_id_generator.cpp
)

target_link_libraries(matching-engine
    PRIVATE
    ${RDKAFKA_LINK_LIBRARIES}
)

enable_testing()

add_executable(order-test
    tests/order_test.cpp
)

add_executable(order-book-test
    tests/order_book_test.cpp
    src/order_book.cpp
)

add_executable(order-book-cancel-test
    tests/order_book_cancel_test.cpp
    src/order_book.cpp
)

add_executable(matching-engine-test
    src/trade_id_generator.cpp
    tests/matching_engine_test.cpp
    src/order_book.cpp
    src/matching_engine.cpp
)

add_executable(trade-test
    src/trade_id_generator.cpp
    tests/trade_test.cpp
    src/order_book.cpp
    src/matching_engine.cpp
)

add_executable(order-command-test
    tests/order_command_test.cpp
)

add_test(NAME order-test COMMAND order-test)
add_test(NAME order-book-test COMMAND order-book-test)
add_test(NAME order-book-cancel-test COMMAND order-book-cancel-test)
add_test(NAME matching-engine-test COMMAND matching-engine-test)
add_test(NAME trade-test COMMAND trade-test)
add_test(NAME order-command-test COMMAND order-command-test)
