package com.dervarex.minified.worlds.world.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
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

    public static Weather fromNbt(NbtCompound nbt) {
        Weather result = new Weather();
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
                result.clearWeatherTime = data.getInt("rain_time");
            }
            if(data.has("thundering")) {
                result.thundering = data.getBoolean("thundering");
            }
        }
        return result;
    }

    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();
        if (raining != null) {
            data.setBoolean("raining", raining);
        }
        if (rainTime != null) {
            data.setInt("rain_time", rainTime);
        }
        if (thunderTime != null) {
            data.setInt("thunder_time", thunderTime);
        }
        if (clearWeatherTime != null) {
            data.setInt("clear_weather_time", clearWeatherTime);
        }
        if (thundering != null) {
            data.setBoolean("thundering", thundering);
        }

        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }
}
