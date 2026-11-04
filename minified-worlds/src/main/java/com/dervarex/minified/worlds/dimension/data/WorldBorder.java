package com.dervarex.minified.worlds.dimension.data;

import org.apiguardian.api.API;

/**
 * The world border
 */
@API(status = API.Status.STABLE)
public class WorldBorder {
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