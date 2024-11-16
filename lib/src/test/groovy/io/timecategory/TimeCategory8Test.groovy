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
        setup:

        when:
        TimeCategory8.use {
            def result = 4.weeks + "not a temporal"
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
// import java.time.*

// // Example usage
// TimeCategory8.use {
//     def now = LocalDateTime.now()
//     println "Now: $now"

//     // Add 2 hours
//     def later = now + 2.hours
//     println "2 hours later: $later"

//     // Subtract 3 days
//     def earlier = now - 3.days
//     println "3 days earlier: $earlier"

//     // Add 45 minutes
//     def moreMinutes = now + 45.minutes
//     println "45 minutes later: $moreMinutes"

//     // Subtract 1 week
//     def lessWeeks = now - 1.weeks
//     println "1 week earlier: $lessWeeks"

//     // Add 2 days 1 hour 2 minutes
//     def theDay = 2.days + now + 2.minutes + 1.hours
//     println "2 day 1 hour 2 minute later: $theDay"

//     // LocalDate
//     def today = LocalDate.now()
//     println "Today: $today"

//     // Add 2 days
//     def twoDaysLater = today + 2.days
//     println "2 days later: $twoDaysLater"

//     // Add 3 weeks
//     def threeWeeksLater = 4.weeks + 1.days + today - 1.weeks - 1.days
//     println "3 weeks later: $threeWeeksLater"

//     // Add 1 year
//     def nextYear = today + 1.years
//     println "1 year later: $nextYear"

// }
