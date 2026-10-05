---
title: Introduction to Minecraft Worlds
tableOfContents: false
stylesheet: worlds-introduction.css
---

:::caution
This Guide only applies for Minecraft 26.1 and above. So does minified-worlds. Versions below 26.1 are **not** supported.
This is because the World Structure has been reorganized in 26.1.
:::

A Minecraft World isn't just a level.dat and some .mca chunk files - it's more:

## World Structure

```
someworld/
├── level.dat                                  - the most important and well known .dat file, contains world metadata and settings
├── level.dat_old                              - backup of level.dat, gets created every time you save the world
├── session.lock                              - for preventing simultaneous access to a world
├── icon.png                                   - world icon
├── data/
│   └── minecraft/
│       ├── custom_boss_events.dat             - contains bossbar data
│       ├── game_rules.dat                     - contains all game rules
│       ├── random_sequences.dat               - contains random sequence data for blocks that need deterministic randomness, such as spawners, or visual randomness, such as fire or leaves.
│       ├── scheduled_events.dat               - contains data for functions scheduled using /schedule
│       ├── scoreboard.dat                     - contains scoreboard data
│       ├── stopwatches.dat                    - contains /stopwatch data, stopwatches are measured in real time instead of ticks and are not affected by lag
│       ├── wandering_trader.dat               - contains wandering trader spawn delay
│       ├── weather.dat                        - contains weather data
│       ├── world_clocks.dat                   - contains tick data for specific worlds
│       └── world_gen_settings.dat             - contains world generator settings like the seed or the preset
├── datapacks/
├── dimensions/
│   └── minecraft/
│       ├── overworld/
│       │   ├── data/
│       │   │   └── minecraft/
│       │   │       ├── chunk_tickets.dat      - contains chunks that are force-loaded (you can trigger this using /forceload add ~ ~)
│       │   │       ├── raids.dat              - contains raid data for the world
│       │   │       └── world_border.dat       - contains world border settings like the size, damage-per-block or the warning-blocks
│       │   ├── entities/
│       │   ├── poi/
│       │   └── region/
│       ├── the_nether/
│       │   ├── data/
│       │   │   └── minecraft/
│       │   │       ├── chunk_tickets.dat      - contains chunks that are force-loaded (you can trigger this using /forceload add ~ ~)
│       │   │       ├── raids.dat              - contains raid data for the world
│       │   │       └── world_border.dat       - contains world border settings like the size, damage-per-block or the warning-blocks
│       │   ├── entities/
│       │   ├── poi/
│       │   └── region/
│       └── the_end/
│           ├── data/
│           │   └── minecraft/
│           │       ├── chunk_tickets.dat      - contains chunks that are force-loaded (you can trigger this using /forceload add ~ ~)
│           │       ├── ender_dragon_fight.dat - contains ender dragon fight data, e.g. how often he has been killed, open gateways or the dragon uuid
│           │       ├── raids.dat              - contains raid data for the world
│           │       └── world_border.dat       - contains world border settings like the size, damage-per-block or the warning-blocks
│           ├── entities/
│           ├── poi/
│           └── region/
└── players/
    ├── advancements/
    │   └── <uuid>.json                        - contains advancements for the player with the given uuid
    ├── data/
    │   ├── <uuid>.dat                         - contains more player data, like the inventory, position or the XP Level
    │   └── <uuid>.dat_old                     - backup of the player data
    └── stats/
        └── <uuid>.json                        - contains player statistics
```

## Next steps

Now that you know how a world is laid out on disk, here's where to go next with `minified-worlds`:

<div class="fields-details" open>
  <div class="fields-grid">

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/opening-a-world">
      <div class="field-card">
        <div class="field-header">
          <code>Opening a World</code>
        </div>
        <p>Load a world folder and read its level.dat, spawn point and other core settings.</p>
      </div>
    </a>

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/dimensions-and-chunks">
      <div class="field-card">
        <div class="field-header">
          <code>Dimensions and Chunks</code>
        </div>
        <p>Understand how terrain is stored, and read or write chunks and blocks.</p>
      </div>
    </a>

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/entities-and-poi">
      <div class="field-card">
        <div class="field-header">
          <code>Entities and Points of Interest</code>
        </div>
        <p>Read and add the mobs, items and villager points of interest inside a chunk.</p>
      </div>
    </a>

  </div>
</div>
