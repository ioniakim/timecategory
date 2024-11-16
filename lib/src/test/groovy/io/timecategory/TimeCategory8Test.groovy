package io.timecategory

import spock.lang.Specification
import java.time.*

class TimeCategory8Test extends Specification {

    def "should correctly add time units to LocalDate"() {
        setup:
        TimeCategory8.use {
            def today = LocalDate.of(2024, 11, 15)

            when:
            def result = today + 2.days + 3.weeks

            then:
            result == today.plusDays(2).plusWeeks(3)
        }
    }

    def "should correctly subtract time units from LocalDateTime"() {
        setup:
        TimeCategory8.use {
            def now = LocalDateTime.of(2024, 11, 15, 10, 0)

            when:
            def result = now - 2.hours - 30.minutes

            then:
            result == now.minusHours(2).minusMinutes(30)
        }
    }

    def "should handle complex chained operations"() {
        setup:
        TimeCategory8.use {
            def today = LocalDate.of(2024, 11, 15)

            when:
            def result = 4.weeks + 1.days + today - 1.weeks

            then:
            result == today.plusWeeks(3).plusDays(1)
        }
    }

    def "should handle unresolved TemporalOperationWrapper"() {
        setup:
        TimeCategory8.use {
            when:
            def operation = 4.weeks + 2.days - 1.weeks

            then:
            operation.toString().contains("Unresolved TemporalOperationWrapper")
        }
    }

    def "should resolve TemporalOperationWrapper with LocalDate"() {
        setup:
        TimeCategory8.use {
            def today = LocalDate.of(2024, 11, 15)

            when:
            def result = (4.weeks + 2.days - 1.weeks) + today

            then:
            result == today.plusWeeks(3).plusDays(2)
        }
    }

    def "should resolve TemporalOperationWrapper with LocalDateTime"() {
        setup:
        TimeCategory8.use {
            def now = LocalDateTime.of(2024, 11, 15, 10, 0)

            when:
            def result = (1.hours + 30.minutes + 2.days) + now

            then:
            result == now.plusHours(1).plusMinutes(30).plusDays(2)
        }
    }

    def "should throw exception for unsupported types"() {
        when:
        TimeCategory8.use {
            4.weeks + "not a temporal"
        }

        then:
        thrown(UnsupportedOperationException)
    }

    def "should calculate properly with a mix of TemporalUnitWrapper and base Temporal"() {
        setup:
        TimeCategory8.use {
            def now = LocalDateTime.of(2024, 11, 15, 10, 0)

            when:
            def result = 2.hours + now + 1.days - 30.minutes

            then:
            result == now.plusHours(2).plusDays(1).minusMinutes(30)
        }
    }

    def "should support negative time units in calculations"() {
        setup:
        TimeCategory8.use {
            def today = LocalDate.of(2024, 11, 15)

            when:
            def result = today + (-2).days + (-1).weeks

            then:
            result == today.minusDays(2).minusWeeks(1)
        }
    }
}
