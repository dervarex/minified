---

title: Mixins

---

## 1. What are Mixins?

Mixins are a core technology used by the Quilt ecosystem to modify Minecraft's compiled bytecode as the game starts.
They allow mods to inject custom code, modify existing behavior, access private members, or replace parts of the game's implementation without directly editing Minecraft itself.

## 2. How and why Quilt uses Mixins

Since Minecraft is compiled and its classes cannot be modified directly, Quilt mods use the SpongePowered Mixin framework to patch the game's bytecode at runtime.
Mixins rely on Java annotations to specify exactly where and how code should be injected.

### Common Types of Mixins

<div class="fields-details" open>
  <div class="fields-grid">

<div class="field-card">
  <div class="field-header">
    <code>@Inject</code>
  </div>
  <p>Injects custom code at the beginning, end, or another specified location within an existing method.</p>
</div>

<div class="field-card">
  <div class="field-header">
    <code>@ModifyVariable</code> / <code>@ModifyArg</code>
  </div>
  <p>Intercepts and modifies local variables or method arguments before they are used by Minecraft.</p>
</div>

<div class="field-card">
  <div class="field-header">
    <code>@Redirect</code>
  </div>
  <p>Redirects a specific method call or field access to your own implementation.</p>
</div>

<div class="field-card">
  <div class="field-header">
    <code>@Accessor</code> / <code>@Invoker</code>
  </div>
  <p>Provides access to private or protected fields and methods without modifying their visibility.</p>
</div>

  </div>
</div>

## 3. Example

<Tabs>
  <TabItem label="Java">

```java
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        // Runs whenever the player jumps.
    }
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
@Mixin(PlayerEntity::class)
abstract class PlayerEntityMixin {
    @Inject(method = "jump", at = At("HEAD"))
    private fun onJump(ci: CallbackInfo) {
        // Runs whenever the player jumps.
    }
}
```

  </TabItem>
</Tabs>