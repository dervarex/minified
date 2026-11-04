## block.BlockStateFormat

`minified-worlds/src/main/java/com/dervarex/minified/worlds/block/BlockStateFormat.java`

- [ ] L42 `toNbt(BlockState)`: no description  
  `public NbtTag toNbt(BlockState state)`

## chunk.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/chunk/package-info.java`

- [ ] package `com.dervarex.minified.worlds.chunk` has no package description

## dimension.Dimension

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/Dimension.java`

- [ ] L26 `Dimension`: no description  
  `public sealed class Dimension permits Overworld, Nether, End`
- [ ] L35 `Dimension.Versioned`: no description, @param dataVersion (record component), @param data (record component)  
  `public record Versioned<T>(int dataVersion, T data) {}`
- [ ] L35 `Versioned(int,Object)`: @param dataVersion, @param data  
  `public record Versioned<T>(int dataVersion, T data) {}`
- [ ] L38 `Dimension(File,DimensionType)`: @param worldFolder, @param type  
  `public Dimension(File worldFolder, DimensionType type)`
- [ ] L45 `Dimension(File)`: @param worldFolder  
  `public Dimension(File worldFolder)`
- [ ] L54 `dimensionFolder(RegionCategory)`: no description, @param category, @return  
  `protected File dimensionFolder(RegionCategory category)`
- [ ] L95 `readChunkData(int,int)`: no description, @param chunkX, @param chunkZ, @return, @throws  
  `public NbtCompound readChunkData(int chunkX, int chunkZ) throws IOException`
- [ ] L99 `readPoiData(int,int)`: no description, @param chunkX, @param chunkZ, @return, @throws  
  `public NbtCompound readPoiData(int chunkX, int chunkZ) throws IOException`
- [ ] L103 `readEntityData(int,int)`: no description, @param chunkX, @param chunkZ, @return, @throws  
  `public NbtCompound readEntityData(int chunkX, int chunkZ) throws IOException`
- [ ] L112 `readChunk(int,int)`: @param chunkX, @param chunkZ, @return, @throws  
  `public Chunk readChunk(int chunkX, int chunkZ) throws IOException`
- [ ] L121 `readPoi(int,int)`: @param chunkX, @param chunkZ, @return, @throws  
  `public PoiData readPoi(int chunkX, int chunkZ) throws IOException`
- [ ] L130 `readEntities(int,int)`: @param chunkX, @param chunkZ, @return, @throws  
  `public EntityData readEntities(int chunkX, int chunkZ) throws IOException`
- [ ] L143 `saveChunkData(Chunk)`: @param chunk  
  `public void saveChunkData(Chunk chunk) throws IOException`
- [ ] L174 `savePoiData(int,int,NbtCompound)`: @param chunkX, @param chunkZ, @param poiData  
  `public void savePoiData(int chunkX, int chunkZ, NbtCompound poiData) throws IOException`
- [ ] L185 `savePoiData(int,int,PoiData)`: @param chunkX, @param chunkZ, @param poiData, @throws  
  `public void savePoiData(int chunkX, int chunkZ, PoiData poiData) throws IOException`
- [ ] L195 `saveEntityData(int,int,NbtCompound)`: @param chunkX, @param chunkZ, @param entityData  
  `public void saveEntityData(int chunkX, int chunkZ, NbtCompound entityData) throws IOException`
- [ ] L206 `saveEntityData(int,int,EntityData)`: @param chunkX, @param chunkZ, @param entityData, @throws  
  `public void saveEntityData(int chunkX, int chunkZ, EntityData entityData) throws IOException`
- [ ] L213 `close()`: @throws  
  `public void close() throws IOException`

## dimension.DimensionType

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/DimensionType.java`

- [ ] L7 `DimensionType`: no description  
  `public enum DimensionType`

## dimension.End

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/End.java`

- [ ] L21 `End(File)`: @param worldFolder  
  `public End(File worldFolder)`
- [ ] L38 `getEnderDragonFight()`: @return  
  `public Optional<EnderDragonFight> getEnderDragonFight()`

## dimension.Nether

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/Nether.java`

- [ ] L7 `Nether`: no description  
  `public final class Nether extends Dimension`
- [ ] L8 `Nether(File)`: @param worldFolder  
  `public Nether(File worldFolder)`

## dimension.Overworld

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/Overworld.java`

- [ ] L7 `Overworld`: no description  
  `public final class Overworld extends Dimension`
- [ ] L9 `Overworld(File)`: @param worldFolder  
  `public Overworld(File worldFolder)`

## dimension.data.ChunkTicket

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/data/ChunkTicket.java`

- [ ] L6 `ChunkTicket`: no description  
  `public class ChunkTicket`

## dimension.data.EnderDragonFight

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/data/EnderDragonFight.java`

- [ ] L15 `EnderDragonFight`: no description  
  `public class EnderDragonFight`
- [ ] L32 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static EnderDragonFight fromNbt(NbtCompound nbt)`
- [ ] L58 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## dimension.data.Raids

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/data/Raids.java`

- [ ] L6 `Raids`: no description  
  `public class Raids`

## dimension.data.WorldBorder

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/data/WorldBorder.java`

- [ ] L6 `WorldBorder`: no description  
  `public class WorldBorder`

## dimension.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/package-info.java`

- [ ] package `com.dervarex.minified.worlds.dimension` has no package description

## dimension.region.RegionCategory

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/region/RegionCategory.java`

- [ ] L6 `RegionCategory`: no description  
  `public enum RegionCategory`

## dimension.region.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/dimension/region/package-info.java`

- [ ] package `com.dervarex.minified.worlds.dimension.region` has no package description

## entity.Entity

`minified-worlds/src/main/java/com/dervarex/minified/worlds/entity/Entity.java`

- [ ] L17 `Entity(NbtCompound)`: @param raw  
  `public Entity(NbtCompound raw)`
- [ ] L21 `raw()`: no description, @return  
  `public NbtCompound raw() { return raw; }`
- [ ] L23 `id()`: no description, @return  
  `public String id() { return raw.getString("id"); }`
- [ ] L25 `uuid()`: no description, @return  
  `public UUID uuid()`
- [ ] L32 `setUuid(UUID)`: no description, @param uuid  
  `public void setUuid(UUID uuid)`
- [ ] L41 `pos()`: no description, @return  
  `public double[] pos() { return readDoubleTriplet("Pos"); }`
- [ ] L42 `setPos(double,double,double)`: no description, @param x, @param y, @param z  
  `public void setPos(double x, double y, double z) { writeDoubleTriplet("Pos", x, y, z); }`
- [ ] L44 `motion()`: no description, @return  
  `public double[] motion() { return readDoubleTriplet("Motion"); }`
- [ ] L45 `setMotion(double,double,double)`: no description, @param x, @param y, @param z  
  `public void setMotion(double x, double y, double z) { writeDoubleTriplet("Motion", x, y, z); }`
- [ ] L48 `rotation()`: @return  
  `public float[] rotation() { return readFloatPair("Rotation"); }`
- [ ] L49 `setRotation(float,float)`: no description, @param yaw, @param pitch  
  `public void setRotation(float yaw, float pitch) { writeFloatPair("Rotation", yaw, pitch); }`
- [ ] L51 `onGround()`: no description, @return  
  `public boolean onGround() { return raw.getBoolean("OnGround"); }`
- [ ] L52 `setOnGround(boolean)`: no description, @param value  
  `public void setOnGround(boolean value) { raw.setBoolean("OnGround", value); }`
- [ ] L54 `invulnerable()`: no description, @return  
  `public boolean invulnerable() { return raw.getBoolean("Invulnerable"); }`
- [ ] L55 `setInvulnerable(boolean)`: no description, @param value  
  `public void setInvulnerable(boolean value) { raw.setBoolean("Invulnerable", value); }`
- [ ] L57 `fallDistance()`: no description, @return  
  `public double fallDistance() { return raw.getDouble("fall_distance"); }`
- [ ] L58 `setFallDistance(double)`: no description, @param value  
  `public void setFallDistance(double value) { raw.setDouble("fall_distance", value); }`
- [ ] L60 `air()`: no description, @return  
  `public short air() { return raw.getShort("Air"); }`
- [ ] L61 `setAir(short)`: no description, @param value  
  `public void setAir(short value) { raw.setShort("Air", value); }`
- [ ] L63 `fire()`: no description, @return  
  `public short fire() { return raw.getShort("Fire"); }`
- [ ] L64 `setFire(short)`: no description, @param value  
  `public void setFire(short value) { raw.setShort("Fire", value); }`
- [ ] L66 `portalCooldown()`: no description, @return  
  `public int portalCooldown() { return raw.getInt("PortalCooldown"); }`
- [ ] L67 `setPortalCooldown(int)`: no description, @param value  
  `public void setPortalCooldown(int value) { raw.setInt("PortalCooldown", value); }`
- [ ] L69 `readDoubleTriplet(String)`: no description, @param key, @return  
  `protected double[] readDoubleTriplet(String key)`
- [ ] L78 `writeDoubleTriplet(String,double,double,double)`: no description, @param key, @param x, @param y, @param z  
  `protected void writeDoubleTriplet(String key, double x, double y, double z)`
- [ ] L86 `readFloatPair(String)`: no description, @param key, @return  
  `protected float[] readFloatPair(String key)`
- [ ] L94 `writeFloatPair(String,float,float)`: no description, @param key, @param a, @param b  
  `protected void writeFloatPair(String key, float a, float b)`

## entity.EntityData

`minified-worlds/src/main/java/com/dervarex/minified/worlds/entity/EntityData.java`

- [ ] L23 `EntityData(NbtCompound)`: @param root  
  `public EntityData(NbtCompound root)`
- [ ] L35 `raw()`: no description, @return  
  `public NbtCompound raw() { return root; }`
- [ ] L37 `entities()`: no description, @return  
  `public List<Entity> entities() { return List.copyOf(entities); }`
- [ ] L45 `addEntity(NbtCompound)`: @param entityData  
  `public Entity addEntity(NbtCompound entityData)`

## entity.LivingEntity

`minified-worlds/src/main/java/com/dervarex/minified/worlds/entity/LivingEntity.java`

- [ ] L12 `LivingEntity(NbtCompound)`: @param raw  
  `public LivingEntity(NbtCompound raw)`
- [ ] L16 `health()`: no description, @return  
  `public float health() { return raw.getFloat("Health"); }`
- [ ] L17 `setHealth(float)`: no description, @param value  
  `public void setHealth(float value) { raw.setFloat("Health", value); }`
- [ ] L19 `leftHanded()`: no description, @return  
  `public boolean leftHanded() { return raw.getBoolean("LeftHanded"); }`
- [ ] L20 `setLeftHanded(boolean)`: no description, @param value  
  `public void setLeftHanded(boolean value) { raw.setBoolean("LeftHanded", value); }`
- [ ] L22 `canPickUpLoot()`: no description, @return  
  `public boolean canPickUpLoot() { return raw.getBoolean("CanPickUpLoot"); }`
- [ ] L23 `setCanPickUpLoot(boolean)`: no description, @param value  
  `public void setCanPickUpLoot(boolean value) { raw.setBoolean("CanPickUpLoot", value); }`
- [ ] L25 `persistenceRequired()`: no description, @return  
  `public boolean persistenceRequired() { return raw.getBoolean("PersistenceRequired"); }`
- [ ] L26 `setPersistenceRequired(boolean)`: no description, @param value  
  `public void setPersistenceRequired(boolean value) { raw.setBoolean("PersistenceRequired", value); }`
- [ ] L28 `ageLocked()`: no description, @return  
  `public boolean ageLocked() { return raw.getBoolean("AgeLocked"); }`
- [ ] L29 `setAgeLocked(boolean)`: no description, @param value  
  `public void setAgeLocked(boolean value) { raw.setBoolean("AgeLocked", value); }`
- [ ] L31 `age()`: no description, @return  
  `public int age() { return raw.getInt("Age"); }`
- [ ] L32 `setAge(int)`: no description, @param value  
  `public void setAge(int value) { raw.setInt("Age", value); }`
- [ ] L34 `absorptionAmount()`: no description, @return  
  `public float absorptionAmount() { return raw.getFloat("AbsorptionAmount"); }`
- [ ] L35 `setAbsorptionAmount(float)`: no description, @param value  
  `public void setAbsorptionAmount(float value) { raw.setFloat("AbsorptionAmount", value); }`
- [ ] L38 `brain()`: @return  
  `public NbtCompound brain() { return raw.getCompound("Brain"); }`

## entity.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/entity/package-info.java`

- [ ] package `com.dervarex.minified.worlds.entity` has no package description

## exceptions.FailedToLoadWorldException

`minified-worlds/src/main/java/com/dervarex/minified/worlds/exceptions/FailedToLoadWorldException.java`

- [ ] L17 `FailedToLoadWorldException(String,Path,Throwable)`: @param message, @param path, @param cause  
  `public FailedToLoadWorldException(String message, Path path, Throwable cause)`

## exceptions.InvalidWorldException

`minified-worlds/src/main/java/com/dervarex/minified/worlds/exceptions/InvalidWorldException.java`

- [ ] L17 `InvalidWorldException(String,Path)`: @param message, @param worldDirectory  
  `public InvalidWorldException(String message, Path worldDirectory)`

## exceptions.WorldLockedException

`minified-worlds/src/main/java/com/dervarex/minified/worlds/exceptions/WorldLockedException.java`

- [ ] L18 `WorldLockedException(String,Path,Throwable)`: @param message, @param lockFile, @param cause  
  `public WorldLockedException(String message, Path lockFile, Throwable cause)`

## poi.PoiData

`minified-worlds/src/main/java/com/dervarex/minified/worlds/poi/PoiData.java`

- [ ] L19 `PoiData(NbtCompound)`: @param root  
  `public PoiData(NbtCompound root)`
- [ ] L38 `raw()`: no description, @return  
  `public NbtCompound raw() { return root; }`
- [ ] L41 `records()`: @return  
  `public List<PoiRecord> records()`
- [ ] L48 `records(int)`: @param sectionY, @return  
  `public List<PoiRecord> records(int sectionY)`
- [ ] L57 `addRecord(int,int,int,int,String,int)`: @param sectionY, @param x, @param y, @param z, @param type, @param freeTickets, @return  
  `public PoiRecord addRecord(int sectionY, int x, int y, int z, String type, int freeTickets)`

## poi.PoiRecord

`minified-worlds/src/main/java/com/dervarex/minified/worlds/poi/PoiRecord.java`

- [ ] L13 `PoiRecord(NbtCompound)`: @param raw  
  `public PoiRecord(NbtCompound raw)`
- [ ] L17 `raw()`: no description, @return  
  `public NbtCompound raw() { return raw; }`
- [ ] L19 `pos()`: no description, @return  
  `public int[] pos() { return raw.getIntArray("pos"); }`
- [ ] L20 `setPos(int,int,int)`: no description, @param x, @param y, @param z  
  `public void setPos(int x, int y, int z) { raw.setIntArray("pos", new int[]{x, y, z}); }`
- [ ] L22 `type()`: no description, @return  
  `public String type() { return raw.getString("type"); }`
- [ ] L23 `setType(String)`: no description, @param value  
  `public void setType(String value) { raw.setString("type", value); }`
- [ ] L25 `freeTickets()`: no description, @return  
  `public int freeTickets() { return raw.getInt("free_tickets"); }`
- [ ] L26 `setFreeTickets(int)`: no description, @param value  
  `public void setFreeTickets(int value) { raw.setInt("free_tickets", value); }`

## poi.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/poi/package-info.java`

- [ ] package `com.dervarex.minified.worlds.poi` has no package description

## save.SessionLock

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/SessionLock.java`

- [ ] L19 `SessionLock`: no description  
  `public class SessionLock`
- [ ] L25 `SessionLock(Path)`: @param path  
  `public SessionLock(Path path)`
- [ ] L51 `acquire(Path)`: @return  
  `public static Handle acquire(Path worldDirectory) throws IOException`

## save.WorldSave

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/WorldSave.java`

- [ ] L79 `WorldSave(Path)`: @param worldDirectory  
  `public WorldSave(Path worldDirectory)`
- [ ] L174 `getCustomBossEvents()`: no description, @return  
  `public Optional<CustomBossEvents> getCustomBossEvents()`
- [ ] L177 `getGameRules()`: no description, @return  
  `public Optional<GameRules> getGameRules()`
- [ ] L180 `getRandomSequences()`: no description, @return  
  `public Optional<RandomSequences> getRandomSequences()`
- [ ] L183 `getScoreboard()`: no description, @return  
  `public Optional<Scoreboard> getScoreboard()`
- [ ] L186 `getStopWatches()`: no description, @return  
  `public Optional<StopWatches> getStopWatches()`
- [ ] L189 `getWanderingTrader()`: no description, @return  
  `public Optional<WanderingTrader> getWanderingTrader()`
- [ ] L192 `getWeather()`: no description, @return  
  `public Optional<Weather> getWeather()`
- [ ] L195 `getWorldClocks()`: no description, @return  
  `public Optional<WorldClocks> getWorldClocks()`
- [ ] L198 `getWorldGenSettings()`: no description, @return  
  `public Optional<WorldGenSettings> getWorldGenSettings()`

## save.data.BossEvent

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/BossEvent.java`

- [ ] L11 `BossEvent`: no description  
  `public class BossEvent`
- [ ] L15 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static BossEvent fromNbt(NbtCompound nbt)`
- [ ] L26 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.data.CustomBossEvents

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/CustomBossEvents.java`

- [ ] L13 `CustomBossEvents`: no description  
  `public class CustomBossEvents`
- [ ] L18 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static CustomBossEvents fromNbt(NbtCompound nbt)`
- [ ] L31 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L42 `putEvent(String,BossEvent)`: no description, @param id, @param event  
  `public void putEvent(String id, BossEvent event)`
- [ ] L45 `removeEvent(String)`: no description, @param id  
  `public void removeEvent(String id)`

## save.data.GameRules

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/GameRules.java`

- [ ] L12 `GameRules`: no description  
  `public class GameRules`
- [ ] L18 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static GameRules fromNbt(NbtCompound nbt)`
- [ ] L35 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L49 `putIntegerGamerule(String,int)`: no description, @param key, @param value  
  `public void putIntegerGamerule(String key, int value)`
- [ ] L52 `removeIntegerGamerule(String)`: no description, @param key  
  `public void removeIntegerGamerule(String key)`
- [ ] L56 `putBooleanGamerule(String,boolean)`: no description, @param key, @param value  
  `public void putBooleanGamerule(String key, boolean value)`
- [ ] L59 `removeBooleanGamerule(String)`: no description, @param key  
  `public void removeBooleanGamerule(String key)`

## save.data.RandomSequences

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/RandomSequences.java`

- [ ] L13 `RandomSequences`: no description  
  `public class RandomSequences`
- [ ] L20 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static RandomSequences fromNbt(NbtCompound nbt)`
- [ ] L37 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L53 `putSequence(String,long[])`: no description, @param key, @param value  
  `public void putSequence(String key, long[] value)`
- [ ] L56 `removeSequence(String)`: no description, @param key  
  `public void removeSequence(String key)`

## save.data.Scoreboard

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/Scoreboard.java`

- [ ] L19 `Scoreboard`: no description  
  `public class Scoreboard`
- [ ] L28 `Scoreboard.Objective`: no description  
  `public static class Objective`
- [ ] L35 `fromNbt(NbtCompound)`: no description, @param nbt, @return, @param compound  
  `public static Objective fromNbt(NbtCompound compound)`
- [ ] L46 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L60 `Scoreboard.PlayerScore`: no description  
  `public static class PlayerScore`
- [ ] L66 `fromNbt(NbtCompound)`: no description, @param compound, @return  
  `public static PlayerScore fromNbt(NbtCompound compound)`
- [ ] L75 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L147 `putDisplaySlot(String,String)`: no description, @param key, @param value  
  `public void putDisplaySlot(String key, String value)`
- [ ] L151 `removeDisplaySlot(String)`: no description, @param key  
  `public void removeDisplaySlot(String key)`
- [ ] L155 `addObjective(Objective)`: no description, @param objective  
  `public void addObjective(Objective objective)`
- [ ] L159 `removeObjective(Objective)`: no description, @param objective  
  `public void removeObjective(Objective objective)`
- [ ] L163 `addPlayerScore(PlayerScore)`: no description, @param playerScore  
  `public void addPlayerScore(PlayerScore playerScore)`
- [ ] L167 `removePlayerScore(PlayerScore)`: no description, @param playerScore  
  `public void removePlayerScore(PlayerScore playerScore)`

## save.data.StopWatches

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/StopWatches.java`

- [ ] L13 `StopWatches`: no description  
  `public class StopWatches`
- [ ] L18 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static StopWatches fromNbt(NbtCompound nbt)`

## save.data.WanderingTrader

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/WanderingTrader.java`

- [ ] L12 `WanderingTrader`: no description  
  `public class WanderingTrader`
- [ ] L19 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static WanderingTrader fromNbt(NbtCompound nbt)`
- [ ] L35 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.data.Weather

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/Weather.java`

- [ ] L12 `Weather`: no description  
  `public class Weather`
- [ ] L25 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static Weather fromNbt(NbtCompound nbt)`
- [ ] L50 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.data.WorldClocks

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/WorldClocks.java`

- [ ] L13 `WorldClocks`: no description  
  `public class WorldClocks`
- [ ] L18 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static WorldClocks fromNbt(NbtCompound nbt)`
- [ ] L35 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.data.WorldGenSettings

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/WorldGenSettings.java`

- [ ] L14 `WorldGenSettings`: no description  
  `public class WorldGenSettings`
- [ ] L25 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static WorldGenSettings fromNbt(NbtCompound nbt)`
- [ ] L54 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L74 `WorldGenSettings.Dimension`: no description  
  `public static class Dimension`
- [ ] L80 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static Dimension fromNbt(NbtCompound nbt)`
- [ ] L91 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L105 `WorldGenSettings.Generator`: no description  
  `public static class Generator`
- [ ] L113 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static Generator fromNbt(NbtCompound nbt)`
- [ ] L127 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`
- [ ] L144 `WorldGenSettings.BiomeSource`: no description  
  `public static class BiomeSource`
- [ ] L150 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static BiomeSource fromNbt(NbtCompound nbt)`
- [ ] L161 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.data.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/data/package-info.java`

- [ ] package `com.dervarex.minified.worlds.save.data` has no package description

## save.level.Difficulty

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/level/Difficulty.java`

- [ ] L6 `Difficulty`: no description  
  `public enum Difficulty`

## save.level.Level

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/level/Level.java`

- [ ] L59 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static Level fromNbt(NbtCompound nbt)`
- [ ] L103 `toNbt()`: @return  
  `public NbtCompound toNbt()`

## save.level.Spawn

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/level/Spawn.java`

- [ ] L15 `Spawn`: no description  
  `public class Spawn`
- [ ] L44 `fromNbt(NbtCompound)`: no description, @param spawnNbt, @return  
  `public static Spawn fromNbt(NbtCompound spawnNbt)`
- [ ] L75 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.level.Version

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/level/Version.java`

- [ ] L15 `Version`: no description  
  `public class Version`
- [ ] L27 `fromNbt(NbtCompound)`: no description, @param versionNbt, @return  
  `public static Version fromNbt(NbtCompound versionNbt)`
- [ ] L45 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.level.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/level/package-info.java`

- [ ] package `com.dervarex.minified.worlds.save.level` has no package description

## save.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/package-info.java`

- [ ] package `com.dervarex.minified.worlds.save` has no package description

## save.playerdata.Abilities

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/Abilities.java`

- [ ] L12 `Abilities`: no description  
  `public class Abilities`
- [ ] L25 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static Abilities fromNbt(NbtCompound nbt)`
- [ ] L52 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.playerdata.Advancements

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/Advancements.java`

- [ ] L15 `Advancements`: no description  
  `public class Advancements`
- [ ] L20 `fromJson(JsonObject)`: no description, @param json, @return  
  `public static Advancements fromJson(JsonObject json)`
- [ ] L40 `toJson()`: no description, @return  
  `public JsonObject toJson()`
- [ ] L53 `Advancements.Advancement`: no description  
  `public static class Advancement`
- [ ] L57 `fromJson(JsonObject)`: no description, @param json, @return  
  `public static Advancement fromJson(JsonObject json)`
- [ ] L76 `toJson()`: no description, @return  
  `public JsonObject toJson()`
- [ ] L90 `getCriterionDate(String)`: no description, @param criterionKey, @return  
  `public String getCriterionDate(String criterionKey)`

## save.playerdata.Attributes

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/Attributes.java`

- [ ] L13 `Attributes`: no description  
  `public class Attributes`
- [ ] L16 `fromNbtList(NbtList)`: no description, @param nbt, @return  
  `public static Attributes fromNbtList(NbtList nbt)`
- [ ] L30 `toNbtList()`: no description, @return  
  `public NbtList toNbtList()`
- [ ] L46 `Attributes.Attribute`: no description  
  `public static class Attribute`

## save.playerdata.Inventory

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/Inventory.java`

- [ ] L16 `fromNbtList(NbtList)`: no description, @param nbt, @return  
  `public static Inventory fromNbtList(NbtList nbt)`
- [ ] L31 `toNbtList()`: no description, @return  
  `public NbtList toNbtList()`
- [ ] L48 `Inventory.InventoryItem`: no description  
  `public static class InventoryItem`

## save.playerdata.Player

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/Player.java`

- [ ] L116 `fromNbt(NbtCompound,JsonObject,JsonObject)`: no description, @param nbt, @param advancements, @param statistics, @return  
  `public static Player fromNbt(@NotNull NbtCompound nbt, @Nullable JsonObject advancements, @Nullable JsonObject statistics)`
- [ ] L252 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.playerdata.RecipeBook

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/RecipeBook.java`

- [ ] L14 `RecipeBook`: no description  
  `public class RecipeBook`
- [ ] L23 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static RecipeBook fromNbt(NbtCompound nbt)`
- [ ] L48 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.playerdata.Stats

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/Stats.java`

- [ ] L14 `Stats`: no description  
  `public class Stats`
- [ ] L19 `fromJson(JsonObject)`: no description, @param json, @return  
  `public static Stats fromJson(JsonObject json)`
- [ ] L51 `toJson()`: no description, @return  
  `public JsonObject toJson()`
- [ ] L71 `getStat(String,String)`: no description, @param category, @param statName, @return  
  `public int getStat(String category, String statName)`

## save.playerdata.WardenSpawnTracker

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/WardenSpawnTracker.java`

- [ ] L13 `WardenSpawnTracker`: no description  
  `public class WardenSpawnTracker`
- [ ] L25 `fromNbt(NbtCompound)`: no description, @param nbt, @return  
  `public static WardenSpawnTracker fromNbt(NbtCompound nbt)`
- [ ] L40 `toNbt()`: no description, @return  
  `public NbtCompound toNbt()`

## save.playerdata.package-info

`minified-worlds/src/main/java/com/dervarex/minified/worlds/save/playerdata/package-info.java`

- [ ] package `com.dervarex.minified.worlds.save.playerdata` has no package description

