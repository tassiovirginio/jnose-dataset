package io.dropwizard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DurationTest {
    @Test
    public void convertsDays() throws Exception {
        assertThat(Duration.days(2).toDays())
                .isEqualTo(2);
        assertThat(Duration.days(2).toHours())
                .isEqualTo(48);
    }

    @Test
    public void convertsHours() throws Exception {
        assertThat(Duration.hours(2).toMinutes())
                .isEqualTo(120);
    }

    @Test
    public void parsesDays() throws Exception {
        assertThat(Duration.parse("1d"))
                .isEqualTo(Duration.days(1));

        assertThat(Duration.parse("1 day"))
                .isEqualTo(Duration.days(1));

        assertThat(Duration.parse("2 days"))
                .isEqualTo(Duration.days(2));
    }

}
