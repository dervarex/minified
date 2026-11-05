package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.apiguardian.api.API;

/**
 * The weather of a world, from weather.dat
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class Weather {
    private Integer dataVersion;
    @Nullable
    private Boolean raining;
    @Nullable
    private Integer rainTime;
    @Nullable
    private Integer thunderTime;
    @Nullable
    private Integer clearWeatherTime;
    @Nullable
    private Boolean thundering;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the weather from NBT
     * @param nbt the content of weather.dat
     * @return the weather
     */
    public static Weather fromNbt(NbtCompound nbt) {
        Weather result = new Weather();
        result.raw = nbt.copy();
        result.dataVersion = nbt.getInt("DataVersion");

        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            if(data.has("raining")) {
                result.raining = data.getBoolean("raining");
            }
            if(data.has("rain_time")) {
                result.rainTime = data.getInt("rain_time");
            }
            if(data.has("thunder_time")) {
                result.thunderTime = data.getInt("thunder_time");
            }
            if(data.has("clear_weather_time")) {
                result.clearWeatherTime = data.getInt("clear_weather_time");
            }
            if(data.has("thundering")) {
                result.thundering = data.getBoolean("thundering");
            }
        }
        return result;
    }

    /** {@return the weather as NBT, like in weather.dat} */
    public NbtCompound toNbt() {
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");
        if (raining != null) {
            data.setBoolean("raining", raining);
        } else {
            data.remove("raining");
        }
        if (rainTime != null) {
            data.setInt("rain_time", rainTime);
        } else {
            data.remove("rain_time");
        }
        if (thunderTime != null) {
            data.setInt("thunder_time", thunderTime);
        } else {
            data.remove("thunder_time");
        }
        if (clearWeatherTime != null) {
            data.setInt("clear_weather_time", clearWeatherTime);
        } else {
            data.remove("clear_weather_time");
        }
        if (thundering != null) {
            data.setBoolean("thundering", thundering);
        } else {
            data.remove("thundering");
        }

        root.setInt("DataVersion", dataVersion);
        return root;
    }
}
