package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherTest {

    @Test
    void testLoadEmptyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 1);

        Weather weather = Weather.fromNbt(nbt);

        assertNull(weather.getRaining());
        assertNull(weather.getRainTime());
        assertNull(weather.getThunderTime());
        assertNull(weather.getClearWeatherTime());
        assertNull(weather.getThundering());
    }

    @Test
    void testSaveAndLoadWeather() {
        Weather original = new Weather();
        original.setDataVersion(1);
        original.setRaining(true);
        original.setRainTime(1000);
        original.setThunderTime(500);
        original.setThundering(false);

        Weather parsed = Weather.fromNbt(original.toNbt());

        assertEquals(original.getRaining(), parsed.getRaining());
        assertEquals(original.getRainTime(), parsed.getRainTime());
        assertEquals(original.getThunderTime(), parsed.getThunderTime());
        assertEquals(original.getThundering(), parsed.getThundering());
    }

    @Test
    void testLoadClearWeatherTime() {
        NbtCompound data = new NbtCompound();
        data.setInt("rain_time", 42);
        data.setInt("clear_weather_time", 99);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 1);

        Weather weather = Weather.fromNbt(nbt);

        assertEquals(42, weather.getRainTime());
        assertEquals(99, weather.getClearWeatherTime());
    }

    @Test
    void testLoadClearWeatherTimeWithoutRainTime() {
        NbtCompound data = new NbtCompound();
        data.setInt("clear_weather_time", 99);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 1);

        Weather weather = Weather.fromNbt(nbt);

        assertNull(weather.getRainTime());
        assertEquals(99, weather.getClearWeatherTime());
    }
}
