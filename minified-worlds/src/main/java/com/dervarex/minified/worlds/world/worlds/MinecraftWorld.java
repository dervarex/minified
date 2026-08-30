package com.dervarex.minified.worlds.world.worlds;

public class MinecraftWorld {
    private Type type;
    private Versioned<ChunkTicket[]> chunkTickets;
    private Raids raids;
    private Versioned<WorldBorder> worldBorder;

    public record Versioned<T>(int dataVersion, T data) {}

    public enum Type {
        Overworld,
        Nether,
        End
    }

    public static class ChunkTicket {
        int[] chunkPos; // two integers, chunk x and chunk z (there's no chunk y since chunks range from -64 to the height limit)
        String type; // usually minecraft:forced
        int level;
    }

    public static class WorldBorder {
        int damagePerBlock;
        int centerZ;
        int centerX;
        int lerpTarget;
        int safeZone;
        int size;
        int lerpTime;
        int warningBlocks;
        int warningTime;
    }

    public static class Raids {
        int nextId;
        int tick;
    }
}